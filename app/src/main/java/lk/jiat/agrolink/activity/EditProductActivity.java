package lk.jiat.agrolink.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lk.jiat.agrolink.BuildConfig;
import lk.jiat.agrolink.R;
import lk.jiat.agrolink.model.Category;
import lk.jiat.agrolink.model.Product;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.network.ImgBBApiService;
import lk.jiat.agrolink.util.UserManager;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProductActivity extends AppCompatActivity {

    private static final String TAG = "EditProductActivity";
    private static final int PICK_IMAGE_REQUEST = 2;

    private EditText editName, editPrice, editStock, editDescription;
    private Spinner spinnerCategory;
    private ImageView imgProduct;
    private Button btnUpdate, btnDelete, btnChangeImage;
    private Product product;
    private String token;

    private Uri newImageUri;

    private String[] categories = {
            "Vegetables",
            "Fruits",
            "Grains",
            "Others"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        editName = findViewById(R.id.editEditProductName);
        editPrice = findViewById(R.id.editEditProductPrice);
        editStock = findViewById(R.id.editEditProductStock);
        editDescription = findViewById(R.id.editEditProductDescription);

        spinnerCategory = findViewById(R.id.spinnerEditCategory);

        imgProduct = findViewById(R.id.imgEditProduct);

        btnUpdate = findViewById(R.id.btnUpdateProduct);
        btnDelete = findViewById(R.id.btnDeleteProduct);
        btnChangeImage = findViewById(R.id.btnSelectImage);

        token = UserManager.getToken(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        categories
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategory.setAdapter(adapter);

        product = getIntent().getParcelableExtra("product");

        if (product != null) {

            editName.setText(product.getName());

            editPrice.setText(
                    String.format(
                            "%.2f",
                            product.getPrice()
                    )
            );

            editStock.setText(
                    String.valueOf(
                            product.getStock()
                    )
            );

            editDescription.setText(
                    product.getDescription()
            );

            if (product.getImageUrl() != null
                    && !product.getImageUrl().isEmpty()) {

                Picasso.get()
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(imgProduct);
            }

            if (product.getCategory() != null) {

                for (int i = 0; i < categories.length; i++) {

                    if (categories[i].equalsIgnoreCase(
                            product.getCategory().getName()
                    )) {

                        spinnerCategory.setSelection(i);
                        break;
                    }
                }
            }
        }

        if (btnChangeImage != null) {

            btnChangeImage.setOnClickListener(
                    v -> openGallery()
            );
        }

        btnUpdate.setOnClickListener(v -> {

            if (newImageUri != null) {

                uploadImageToImgBB();

            } else {

                updateProductInBackend(
                        product.getImageUrl()
                );
            }
        });

        btnDelete.setOnClickListener(
                v -> confirmDelete()
        );
    }

    private void openGallery() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );

        startActivityForResult(
                intent,
                PICK_IMAGE_REQUEST
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            newImageUri = data.getData();

            imgProduct.setImageURI(
                    newImageUri
            );
        }
    }

    private void uploadImageToImgBB() {

        try {

            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(newImageUri);

            byte[] bytes =
                    getBytes(inputStream);

            RequestBody requestFile =
                    RequestBody.create(
                            bytes,
                            MediaType.parse("image/*")
                    );

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData(
                            "image",
                            "product.jpg",
                            requestFile
                    );

            ImgBBApiService imgBBApiService =
                    ApiClient.getImgBBClient()
                            .create(
                                    ImgBBApiService.class
                            );

            imgBBApiService.uploadImage(
                    BuildConfig.IMGBB_API_KEY,
                    body
            ).enqueue(
                    new Callback<ImgBBApiService.ImgBBResponse>() {

                        @Override
                        public void onResponse(
                                Call<ImgBBApiService.ImgBBResponse> call,
                                Response<ImgBBApiService.ImgBBResponse> response
                        ) {

                            if (response.isSuccessful()
                                    && response.body() != null
                                    && response.body().data != null) {

                                updateProductInBackend(
                                        response.body().data.url
                                );

                            } else {

                                Toast.makeText(
                                        EditProductActivity.this,
                                        "ImgBB Upload Failed!",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ImgBBApiService.ImgBBResponse> call,
                                Throwable t
                        ) {

                            Log.e(
                                    TAG,
                                    "ImgBB upload error",
                                    t
                            );

                            Toast.makeText(
                                    EditProductActivity.this,
                                    "Network Error!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "Error reading image",
                    e
            );

            Toast.makeText(
                    this,
                    "Error reading image!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private byte[] getBytes(
            InputStream inputStream
    ) throws Exception {

        ByteArrayOutputStream byteBuffer =
                new ByteArrayOutputStream();

        int bufferSize = 1024;

        byte[] buffer =
                new byte[bufferSize];

        int len;

        while ((len = inputStream.read(buffer)) != -1) {

            byteBuffer.write(
                    buffer,
                    0,
                    len
            );
        }

        return byteBuffer.toByteArray();
    }

    private void updateProductInBackend(
            String imageUrl
    ) {

        String name =
                editName.getText()
                        .toString()
                        .trim();

        String priceStr =
                editPrice.getText()
                        .toString()
                        .trim();

        String stockStr =
                editStock.getText()
                        .toString()
                        .trim();

        String description =
                editDescription.getText()
                        .toString()
                        .trim();

        String selectedCategory =
                spinnerCategory
                        .getSelectedItem()
                        .toString();

        if (name.isEmpty()
                || priceStr.isEmpty()
                || stockStr.isEmpty()) {

            Toast.makeText(
                    this,
                    "Fields cannot be empty!",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        product.setName(name);

        product.setPrice(
                Double.parseDouble(priceStr)
        );

        product.setStock(
                Integer.parseInt(stockStr)
        );

        product.setDescription(
                description
        );

        product.setImageUrl(
                imageUrl
        );

        int categoryId =
                spinnerCategory
                        .getSelectedItemPosition()
                        + 1;

        Category cat =
                product.getCategory();

        if (cat == null) {
            cat = new Category();
        }

        cat.setId(categoryId);
        cat.setName(selectedCategory);

        product.setCategory(cat);

        ApiService apiService =
                ApiClient.getClient()
                        .create(ApiService.class);

        apiService.updateAdminProduct(
                "Bearer " + token,
                product.getId(),
                product
        ).enqueue(
                new Callback<Product>() {

                    @Override
                    public void onResponse(
                            Call<Product> call,
                            Response<Product> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    EditProductActivity.this,
                                    "Updated Successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();

                        } else {

                            try {

                                if (response.errorBody() != null) {

                                    String errorStr =
                                            response.errorBody()
                                                    .string();

                                    Log.e(
                                            TAG,
                                            "Backend Error: "
                                                    + errorStr
                                    );
                                }

                            } catch (IOException e) {

                                Log.e(
                                        TAG,
                                        "Error reading backend error",
                                        e
                                );
                            }

                            Toast.makeText(
                                    EditProductActivity.this,
                                    "Update Failed! Check Logcat for details",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Product> call,
                            Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "Backend request error",
                                t
                        );

                        Toast.makeText(
                                EditProductActivity.this,
                                "Network Error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void confirmDelete() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure?")
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteProduct()
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void deleteProduct() {

        ApiService apiService =
                ApiClient.getClient()
                        .create(ApiService.class);

        apiService.deleteAdminProduct(
                "Bearer " + token,
                product.getId()
        ).enqueue(
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    EditProductActivity.this,
                                    "Deleted!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();

                        } else {

                            Toast.makeText(
                                    EditProductActivity.this,
                                    "Delete Failed! Code: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "Delete request error",
                                t
                        );

                        Toast.makeText(
                                EditProductActivity.this,
                                "Network Error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
