package lk.jiat.agrolink.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.adapter.AdminOrderAdapter;
import lk.jiat.agrolink.model.Order;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminOrderAdapter adapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        recyclerView = findViewById(R.id.recyclerViewAdminOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        adapter = new AdminOrderAdapter(orderList, this::updateStatus);
        recyclerView.setAdapter(adapter);

        loadAllOrders();
    }

    private void loadAllOrders() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = UserManager.getToken(this);

        apiService.getAdminOrders("Bearer " + token).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList.clear();
                    orderList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ManageOrdersActivity.this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Toast.makeText(ManageOrdersActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatus(int orderId, String newStatus) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = UserManager.getToken(this);

        ApiService.UpdateStatusRequest request = new ApiService.UpdateStatusRequest(newStatus);

        apiService.updateOrderStatus("Bearer " + token, orderId, request).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManageOrdersActivity.this, "Order marked as PAID", Toast.LENGTH_SHORT).show();
                    loadAllOrders(); // Refresh list
                } else {
                    Toast.makeText(ManageOrdersActivity.this, "Update Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                Toast.makeText(ManageOrdersActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
