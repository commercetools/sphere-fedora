package models;

import io.sphere.client.shop.model.LineItem;

public class ShopLineItem {
    private final LineItem lineItem;
    private final ShopProduct product;

    ShopLineItem(LineItem lineItem, ShopProduct product) {
        this.lineItem = lineItem;
        this.product = product;
    }

    public static ShopLineItem of(LineItem lineItem, ShopProduct product) {
        return new ShopLineItem(lineItem, product);
    }

    public LineItem get() {
        return lineItem;
    }

    public ShopProduct getProduct() {
        return product;
    }
}
