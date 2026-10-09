package lk.jiat.agrolink.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.model.Product;

public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ImageView imgProduct = findViewById(R.id.imgProductDetail);
        TextView txtName = findViewById(R.id.txtNameDetail);
        TextView txtDescription = findViewById(R.id.txtDescriptionDetail);
        TextView txtPrice = findViewById(R.id.txtPriceDetail);

        // Get product details from Intent
        Product product = getIntent().getParcelableExtra("product");

        if (product != null) {
            txtName.setText(product.getName());
            txtDescription.setText(product.getDescription());
            txtPrice.setText("Rs. " + product.getPrice());

            // Load image using Picasso
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
