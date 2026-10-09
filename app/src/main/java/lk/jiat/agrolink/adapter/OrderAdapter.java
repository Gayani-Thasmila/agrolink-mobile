package lk.jiat.agrolink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.model.Order;
import lk.jiat.agrolink.model.OrderItem;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtOrderTotal, txtOrderItems, txtOrderStatus;
        ImageView imgOrderProduct;

        public ViewHolder(View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtOrderTotal = itemView.findViewById(R.id.txtOrderTotal);
            txtOrderItems = itemView.findViewById(R.id.txtOrderItems);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            imgOrderProduct = itemView.findViewById(R.id.imgOrderProduct);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.txtOrderId.setText("Order #" + order.getId());
        holder.txtOrderTotal.setText("Total: Rs. " + String.format("%.2f", order.getTotalPrice()));

        if (holder.txtOrderStatus != null) {
            holder.txtOrderStatus.setText("Status: " + order.getStatus());
        }

        StringBuilder itemsSummary = new StringBuilder();
        List<OrderItem> items = order.getOrderItems();

        if (items != null && !items.isEmpty()) {

            OrderItem firstItem = items.get(0);
            if (firstItem.getProduct() != null && firstItem.getProduct().getImageUrl() != null) {
                Picasso.get().load(firstItem.getProduct().getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(holder.imgOrderProduct);
            } else {
                holder.imgOrderProduct.setImageResource(R.drawable.placeholder_image);
            }


            for (OrderItem item : items) {
                if (item.getProduct() != null) {
                    itemsSummary.append(item.getProduct().getName())
                            .append(" x")
                            .append(item.getQuantity())
                            .append(", ");
                }
            }

            if (itemsSummary.length() > 2) {
                itemsSummary.setLength(itemsSummary.length() - 2);
            }
        } else {
            holder.imgOrderProduct.setImageResource(R.drawable.placeholder_image);
            itemsSummary.append("No items details available");
        }

        holder.txtOrderItems.setText(itemsSummary.toString());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
