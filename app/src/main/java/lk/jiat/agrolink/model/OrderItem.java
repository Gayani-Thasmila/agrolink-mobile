package lk.jiat.agrolink.model;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private int id;
    private int quantity;
    private double price;
    private Product product;

    public OrderItem() {}

    public OrderItem(int quantity, double price, Product product) {
        this.quantity = quantity;
        this.price = price;
        this.product = product;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
