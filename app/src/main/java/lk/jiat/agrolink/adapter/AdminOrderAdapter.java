package lk.jiat.agrolink.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.activity.MapActivity;
import lk.jiat.agrolink.model.Order;
import lk.jiat.agrolink.model.OrderItem;
import lk.jiat.agrolink.model.User;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private static final String TAG = "AdminOrderAdapter";
    private List<Order> orderList;
    private OnStatusUpdateListener listener;

    public interface OnStatusUpdateListener {
        void onUpdate(int orderId, String newStatus);
    }

    public AdminOrderAdapter(List<Order> orderList, OnStatusUpdateListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtOrderTotal, txtOrderStatus, txtOrderUser, txtOrderContact, txtOrderAddress, txtOrderItems;
        ImageView imgOrderProduct;
        Button btnOrderAction, btnViewOnMap;

        public ViewHolder(View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderIdAdmin);
            txtOrderTotal = itemView.findViewById(R.id.txtOrderTotalAdmin);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatusAdmin);
            txtOrderUser = itemView.findViewById(R.id.txtOrderUserAdmin);
            txtOrderContact = itemView.findViewById(R.id.txtOrderContactAdmin);
            txtOrderAddress = itemView.findViewById(R.id.txtOrderAddressAdmin);
            txtOrderItems = itemView.findViewById(R.id.txtOrderItemsAdmin);
            imgOrderProduct = itemView.findViewById(R.id.imgOrderProductAdmin);
            btnOrderAction = itemView.findViewById(R.id.btnOrderAction);
            btnViewOnMap = itemView.findViewById(R.id.btnViewOnMap);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.txtOrderId.setText("Order #" + order.getId());
        holder.txtOrderTotal.setText("Total: Rs. " + String.format("%.2f", order.getTotalPrice()));
        holder.txtOrderStatus.setText("Status: " + order.getStatus());

        User user = order.getUser();
        if (user != null) {
            holder.txtOrderUser.setText("Email: " + user.getEmail());
            holder.txtOrderContact.setText("Contact: " + (user.getPhone() != null ? user.getPhone() : "N/A"));
            holder.txtOrderAddress.setText("Address: " + (user.getAddress() != null ? user.getAddress() : "N/A"));
        }

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            StringBuilder itemSummary = new StringBuilder();
            OrderItem firstItem = order.getOrderItems().get(0);
            if (firstItem.getProduct() != null && firstItem.getProduct().getImageUrl() != null) {
                Picasso.get().load(firstItem.getProduct().getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(holder.imgOrderProduct);
            }
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct() != null) {
                    itemSummary.append(item.getProduct().getName()).append(" x").append(item.getQuantity()).append("\n");
                }
            }
            holder.txtOrderItems.setText(itemSummary.toString().trim());
        }


        if (holder.btnViewOnMap != null) {
            holder.btnViewOnMap.setOnClickListener(v -> {
                Double lat = order.getLatitude();
                Double lon = order.getLongitude();


                Log.d("MapDebug", "Order ID: " + order.getId() + " | Lat: " + lat + " | Lon: " + lon);

                if (lat != null && lon != null && lat != 0.0 && lon != 0.0) {
                    Intent intent = new Intent(v.getContext(), lk.jiat.agrolink.activity.MapActivity.class);
                    intent.putExtra("view_lat", lat);
                    intent.putExtra("view_lon", lon);
                    v.getContext().startActivity(intent);
                } else if (user != null && user.getLatitude() != null && user.getLatitude() != 0.0) {
                    Log.d("MapDebug", "Fallback to User Location | Lat: " + user.getLatitude());
                    Intent intent = new Intent(v.getContext(), lk.jiat.agrolink.activity.MapActivity.class);
                    intent.putExtra("view_lat", user.getLatitude());
                    intent.putExtra("view_lon", user.getLongitude());
                    v.getContext().startActivity(intent);
                    Toast.makeText(v.getContext(), "Showing User's Profile Location", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(v.getContext(), "No location data found in Order or User Profile", Toast.LENGTH_LONG).show();
                }
            });
        }

        String currentStatus = order.getStatus();
        if ("PAID".equalsIgnoreCase(currentStatus) || "PENDING".equalsIgnoreCase(currentStatus)) {
            holder.btnOrderAction.setVisibility(View.VISIBLE);
            holder.btnOrderAction.setText("SHIP");
            holder.btnOrderAction.setBackgroundColor(Color.parseColor("#FF9800"));
            holder.btnOrderAction.setOnClickListener(v -> listener.onUpdate(order.getId(), "SHIPPED"));
        } else if ("SHIPPED".equalsIgnoreCase(currentStatus)) {
            holder.btnOrderAction.setVisibility(View.VISIBLE);
            holder.btnOrderAction.setText("DELIVER");
            holder.btnOrderAction.setBackgroundColor(Color.parseColor("#4CAF50"));
            holder.btnOrderAction.setOnClickListener(v -> listener.onUpdate(order.getId(), "DELIVERED"));
        } else {
            holder.btnOrderAction.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }
}
