package lk.jiat.agrolink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.model.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Runnable onCartChanged;

    public CartAdapter(List<CartItem> cartItems, Runnable onCartChanged) {
        this.cartItems = cartItems;
        this.onCartChanged = onCartChanged;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtPrice, txtQuantity;
        Button btnPlus, btnMinus, btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.img_cart_product);
            txtName = itemView.findViewById(R.id.txtCartName);
            txtPrice = itemView.findViewById(R.id.txtCartPrice);
            txtQuantity = itemView.findViewById(R.id.txtQuantity);

            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.txtName.setText(item.getProduct().getName());
        holder.txtPrice.setText("Rs. " + String.format("%.2f", item.getProduct().getPrice()));
        holder.txtQuantity.setText(String.valueOf(item.getQuantity()));

        String imageUrl = item.getProduct().getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.placeholder_image);
        }

        holder.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(holder.getAdapterPosition());
            if (onCartChanged != null) onCartChanged.run();
        });

        holder.btnMinus.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(currentPosition);
            } else {
                cartItems.remove(currentPosition);
                notifyItemRemoved(currentPosition);
            }
            if (onCartChanged != null) onCartChanged.run();
        });

        holder.btnRemove.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                cartItems.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                if (onCartChanged != null) onCartChanged.run();
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }
}
