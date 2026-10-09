package lk.jiat.agrolink.util;

import java.util.ArrayList;

import lk.jiat.agrolink.model.CartItem;
import lk.jiat.agrolink.model.Product;

public class CartManager {

    private static final ArrayList<CartItem> cartList = new ArrayList<>();

    public static void addToCart(Product product) {

        for (CartItem item : cartList) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                return;
            }
        }

        cartList.add(new CartItem(product, 1));
    }

    public static ArrayList<CartItem> getCart() {
        return cartList;
    }

    public static void increaseQuantity(CartItem item) {
        item.setQuantity(item.getQuantity() + 1);
    }

    public static void decreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cartList.remove(item);
        }
    }

    public static void removeFromCart(CartItem item) {
        cartList.remove(item);
    }

    public static void clearCart() {
        cartList.clear();
    }
}