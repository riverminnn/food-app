package deso1.nguyenquangha_21a100100107;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private Context context;
    private ArrayList<Food> foodList;

    public FoodAdapter(Context context, ArrayList<Food> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.tvFoodName.setText(food.getName());
        holder.tvFoodPrice.setText(String.format("%,.0f đồng", food.getPrice()));
        holder.imgEdit.setOnClickListener(v -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).showEditFoodDialog(food);
            }
        });


        // Load image with Glide
        if (food.getImage().equals("default_image")) {
            Glide.with(context)
                    .load(R.drawable.default_image)
                    .into(holder.imgFood);
        } else {
            Glide.with(context)
                    .load(Uri.parse(food.getImage()))  // Load from URI
                    .placeholder(R.drawable.default_image) // Show default while loading
                    .error(R.drawable.default_image) // Show default on error
                    .into(holder.imgFood);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvFoodPrice;
        ImageView imgFood, imgEdit;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            imgFood = itemView.findViewById(R.id.imgFood);
            imgEdit = itemView.findViewById(R.id.imgEdit);
        }
    }
}
