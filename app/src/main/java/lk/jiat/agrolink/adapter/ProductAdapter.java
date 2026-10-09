package lk.jiat.agrolink.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.activity.CartActivity;
import lk.jiat.agrolink.activity.EditProductActivity;
import lk.jiat.agrolink.activity.ProductDetailActivity;
import lk.jiat.agrolink.model.Product;
import lk.jiat.agrolink.util.CartManager;
import lk.jiat.agrolink.util.UserManager;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private boolean isCart;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
        this.isCart = false;
    }

    public ProductAdapter(ArrayList<Product> productList, boolean isCart) {
        this.productList = productList;
        this.isCart = isCart;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, price, description;
        ImageView imgProduct;
        Button btnAdd;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtName);
            price = itemView.findViewById(R.id.txtPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnAdd = itemView.findViewById(R.id.btnAddToCart);
            description = itemView.findViewById(R.id.txtDescription);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.name.setText(product.getName());
        holder.price.setText("Rs. " + String.format("%.2f", product.getPrice()));

        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            holder.description.setText(product.getDescription());
        } else {
            holder.description.setText("No description available");
        }

        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.placeholder_image);
        }

        if (isCart || UserManager.isAdmin(holder.itemView.getContext())) {
            holder.btnAdd.setVisibility(View.GONE);
        } else {
            holder.btnAdd.setVisibility(View.VISIBLE);
            holder.btnAdd.setOnClickListener(v -> {
                CartManager.addToCart(product);
                Intent intent = new Intent(holder.itemView.getContext(), CartActivity.class);
                holder.itemView.getContext().startActivity(intent);
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (UserManager.isAdmin(v.getContext())) {
                // If Admin, go to Edit Product page
                Intent intent = new Intent(v.getContext(), EditProductActivity.class);
                intent.putExtra("product", product);
                v.getContext().startActivity(intent);

            } else {

                // If Customer, go to Product Detail page
                Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
                intent.putExtra("product", product);
                v.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
