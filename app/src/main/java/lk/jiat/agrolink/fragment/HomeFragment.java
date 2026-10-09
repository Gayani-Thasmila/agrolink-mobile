package lk.jiat.agrolink.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.activity.AdminActivity;
import lk.jiat.agrolink.activity.LoginActivity;
import lk.jiat.agrolink.activity.OrderActivity;
import lk.jiat.agrolink.activity.ProfileActivity;
import lk.jiat.agrolink.adapter.ProductAdapter;
import lk.jiat.agrolink.model.Product;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    RecyclerView recyclerView;
    ArrayList<Product> fullList;
    ArrayList<Product> filteredList;
    ProductAdapter adapter;

    TextView txtUser;
    ImageView btnProfile;
    Button btnOrders, btnAdminPanel;
    Button btnCatAll, btnCatVeg, btnCatFruit, btnCatGrain;
    EditText editSearch;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        txtUser = view.findViewById(R.id.txtUser);
        btnOrders = view.findViewById(R.id.btnOrders);
        btnAdminPanel = view.findViewById(R.id.btnAdminPanel);
        btnProfile = view.findViewById(R.id.btnProfile);
        editSearch = view.findViewById(R.id.editSearch);

        btnCatAll = view.findViewById(R.id.btnCatAll);
        btnCatVeg = view.findViewById(R.id.btnCatVeg);
        btnCatFruit = view.findViewById(R.id.btnCatFruit);
        btnCatGrain = view.findViewById(R.id.btnCatGrain);

        String userEmail = UserManager.getUser(getContext());
        txtUser.setText(userEmail != null ? "Welcome " + userEmail : "Welcome Guest");

        if (UserManager.isAdmin(getContext())) {
            btnAdminPanel.setVisibility(View.VISIBLE);
        } else {
            btnAdminPanel.setVisibility(View.GONE);
        }

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        fullList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ProductAdapter(filteredList, false);
        recyclerView.setAdapter(adapter);

        loadProducts();

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString(), ""); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCatAll.setOnClickListener(v -> filter("", ""));
        btnCatVeg.setOnClickListener(v -> filter("", "Vegetables"));
        btnCatFruit.setOnClickListener(v -> filter("", "Fruits"));
        btnCatGrain.setOnClickListener(v -> filter("", "Grains"));

        btnAdminPanel.setOnClickListener(v -> startActivity(new Intent(getActivity(), AdminActivity.class)));
        btnOrders.setOnClickListener(v -> startActivity(new Intent(getActivity(), OrderActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), ProfileActivity.class)));
    }

    private void filter(String nameQuery, String categoryQuery) {
        filteredList.clear();
        for (Product item : fullList) {
            String catName = (item.getCategory() != null) ? item.getCategory().getName() : "";
            boolean matchesName = item.getName().toLowerCase().contains(nameQuery.toLowerCase());
            boolean matchesCategory = categoryQuery.isEmpty() || catName.equalsIgnoreCase(categoryQuery);
            if (matchesName && matchesCategory) filteredList.add(item);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadProducts() {
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.getProducts("").enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullList.clear();
                    fullList.addAll(response.body());
                    filteredList.clear();
                    filteredList.addAll(fullList);
                    adapter.notifyDataSetChanged();
                } else {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Network Error: " + t.getMessage());
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
