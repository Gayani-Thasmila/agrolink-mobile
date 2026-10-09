package lk.jiat.agrolink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.adapter.ProductAdapter;
import lk.jiat.agrolink.model.Product;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageProductsActivity extends AppCompatActivity {

    private static final String TAG = "ManageProductsActivity";
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ArrayList<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        recyclerView = findViewById(R.id.recyclerViewManageProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, true);
        recyclerView.setAdapter(adapter);

        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    private void loadProducts() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = UserManager.getToken(this);

        apiService.getProducts("Bearer " + token).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productList.clear();
                    productList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(ManageProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
