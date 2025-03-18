package deso1.nguyenquangha_21a100100107;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private ArrayList<Food> foodList;

    private String imagePath = ""; // Stores selected image path
    private ImageView imgPreview; // Image preview in the dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadData(); // Load food list from database

        FloatingActionButton btnAddFood = findViewById(R.id.btnAddFood);

        btnAddFood.setOnClickListener(v -> showAddFoodDialog());
    }

    /**
     * Displays a dialog to add a new food item.
     */
    private void showAddFoodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize dialog UI elements
        EditText edtFoodName = dialogView.findViewById(R.id.edtFoodName);
        EditText edtFoodPrice = dialogView.findViewById(R.id.edtFoodPrice);
        imgPreview = dialogView.findViewById(R.id.imgPreview);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);
        Button btnSaveFood = dialogView.findViewById(R.id.btnSaveFood);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        btnUploadImage.setOnClickListener(v -> openGallery());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSaveFood.setOnClickListener(v -> {
            String name = edtFoodName.getText().toString().trim();
            String priceStr = edtFoodPrice.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);
            if (imagePath.isEmpty()) imagePath = "default_image"; // Use default if no image is selected

            // Insert new food item into database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("price", price);
            values.put("image", imagePath);

            long newRowId = db.insert("food", null, values);
            db.close();

            if (newRowId != -1) {
                Toast.makeText(this, "Food added!", Toast.LENGTH_SHORT).show();
                loadData();
                foodAdapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to add food!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Displays a dialog to edit an existing food item.
     */
    public void showEditFoodDialog(Food food) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_food, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Initialize dialog UI elements
        EditText edtFoodName = dialogView.findViewById(R.id.edtFoodName);
        EditText edtFoodPrice = dialogView.findViewById(R.id.edtFoodPrice);
        imgPreview = dialogView.findViewById(R.id.imgPreview);
        Button btnUploadImage = dialogView.findViewById(R.id.btnUploadImage);
        Button btnSaveFood = dialogView.findViewById(R.id.btnSaveFood);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        // Pre-fill fields with existing food data
        edtFoodName.setText(food.getName());
        edtFoodPrice.setText(String.valueOf(food.getPrice()));

        Glide.with(this)
                .load(food.getImage().equals("default_image") ? R.drawable.default_image : Uri.parse(food.getImage()))
                .placeholder(R.drawable.default_image)
                .error(R.drawable.default_image)
                .into(imgPreview);

        imagePath = food.getImage();

        btnUploadImage.setOnClickListener(v -> openGallery());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSaveFood.setText("Update");
        btnSaveFood.setOnClickListener(v -> {
            String name = edtFoodName.getText().toString().trim();
            String priceStr = edtFoodPrice.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);

            // Update food item in database
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("price", price);
            values.put("image", imagePath);

            db.update("food", values, "id=?", new String[]{String.valueOf(food.getId())});
            db.close();

            Toast.makeText(this, "Food updated!", Toast.LENGTH_SHORT).show();
            loadData();
            foodAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
    }

    /**
     * Opens the gallery for image selection.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    /**
     * Handles result from gallery image selection.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imagePath = selectedImageUri.toString();

                if (imgPreview != null) {
                    Glide.with(this)
                            .load(imagePath)
                            .placeholder(R.drawable.default_image)
                            .error(R.drawable.default_image)
                            .into(imgPreview);
                }
            }
        }
    }

    /**
     * Loads food list from the database into the RecyclerView.
     */
    private void loadData() {
        foodList = new ArrayList<>();
        Cursor cursor = dbHelper.getAllFood();
        while (cursor.moveToNext()) {
            foodList.add(new Food(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getString(3)));
        }
        cursor.close();
        foodAdapter = new FoodAdapter(this, foodList);
        recyclerView.setAdapter(foodAdapter);
    }

    /**
     * Deletes all food items from the database. IN CASE WE NEED
     */
    private void clearDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("food", null, null);
        db.close();
    }
}
