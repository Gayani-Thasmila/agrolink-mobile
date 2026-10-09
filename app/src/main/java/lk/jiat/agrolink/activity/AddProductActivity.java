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
import androidx.appcompat.app.AppCompatActivity;

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

public class AddProductActivity extends AppCompatActivity {

    private static final String TAG = "AddProductActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imgSelectProduct;
    private EditText editName, editPrice, editStock, editDescription;
    private Spinner spinnerCategory;
    private Button btnSelectImage, btnSaveProduct;
    private Uri imageUri;

    private String[] categories = {
            "Vegetables",
            "Fruits",
            "Grains",
            "Others"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        imgSelectProduct = findViewById(R.id.imgSelectProduct);
        editName = findViewById(R.id.editProductName);
        editPrice = findViewById(R.id.editProductPrice);
        editStock = findViewById(R.id.editProductStock);
        editDescription = findViewById(R.id.editProductDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCategory.setAdapter(adapter);

        btnSelectImage.setOnClickListener(v -> openGallery());

        btnSaveProduct.setOnClickListener(v -> {
            if (validateForm()) {
                uploadImageToImgBB();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            imageUri = data.getData();
            imgSelectProduct.setImageURI(imageUri);
        }
    }

    private boolean validateForm() {

        if (imageUri == null) {
            Toast.makeText(
                    this,
                    "Please select an image",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        if (editName.getText().toString().trim().isEmpty()
                || editPrice.getText().toString().trim().isEmpty()
                || editStock.getText().toString().trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Fields cannot be empty!",
                    Toast.LENGTH_SHORT
            ).show();

            return false;
        }

        return true;
    }

    private void uploadImageToImgBB() {

        try {

            InputStream inputStream =
                    getContentResolver().openInputStream(imageUri);

            byte[] bytes = getBytes(inputStream);

            RequestBody requestFile = RequestBody.create(
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
                            .create(ImgBBApiService.class);

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

                                saveProductToBackend(
                                        response.body().data.url
                                );

                            } else {

                                Toast.makeText(
                                        AddProductActivity.this,
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
                                    AddProductActivity.this,
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

    private byte[] getBytes(InputStream inputStream) throws Exception {

        ByteArrayOutputStream byteBuffer =
                new ByteArrayOutputStream();

        int bufferSize = 1024;

        byte[] buffer = new byte[bufferSize];

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

    private void saveProductToBackend(String imageUrl) {

        String name =
                editName.getText().toString().trim();

        String priceStr =
                editPrice.getText().toString().trim();

        String stockStr =
                editStock.getText().toString().trim();

        String description =
                editDescription.getText().toString().trim();

        int categoryId =
                spinnerCategory.getSelectedItemPosition() + 1;

        Category categoryObj =
                new Category();

        categoryObj.setId(categoryId);

        categoryObj.setName(
                spinnerCategory
                        .getSelectedItem()
                        .toString()
        );

        Product product =
                new Product(
                        0,
                        name,
                        description,
                        Double.parseDouble(priceStr),
                        Integer.parseInt(stockStr),
                        imageUrl,
                        categoryObj
                );

        ApiService apiService =
                ApiClient.getClient()
                        .create(ApiService.class);

        String token =
                UserManager.getToken(this);

        apiService.addAdminProduct(
                "Bearer " + token,
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
                                    AddProductActivity.this,
                                    "Product Saved!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();

                        } else {

                            try {

                                if (response.errorBody() != null) {

                                    Log.e(
                                            TAG,
                                            "Backend Error: "
                                                    + response.errorBody().string()
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
                                    AddProductActivity.this,
                                    "Backend Error: Check Logcat",
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
                                AddProductActivity.this,
                                "Network Error!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }
}
