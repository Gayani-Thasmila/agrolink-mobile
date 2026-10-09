package lk.jiat.agrolink.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class Product implements Parcelable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;

    // Using Object to handle different price formats from backend
    private Object price;
    private int stock;

    @SerializedName(value = "imageUrl", alternate = {"image_url"})
    private String imageUrl;

    // category is a Category object to match the backend JSON structure
    @SerializedName(value = "category", alternate = {"category_name", "categoryName"})
    private Category category;

    public Product() {
    }

    public Product(int id, String name, String description, double price, int stock, String imageUrl, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    protected Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        // Price handled via Object during deserialization
        price = in.readDouble();
        stock = in.readInt();
        imageUrl = in.readString();
        category = in.readParcelable(Category.class.getClassLoader());
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() {
        if (price instanceof Double) {
            return (Double) price;
        } else if (price instanceof Integer) {
            return ((Integer) price).doubleValue();
        } else if (price instanceof Long) {
            return ((Long) price).doubleValue();
        } else if (price instanceof String) {
            try {
                return Double.parseDouble((String) price);
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public void setPrice(Object price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(getPrice());
        dest.writeInt(stock);
        dest.writeString(imageUrl);
        dest.writeParcelable(category, flags);
    }
}
