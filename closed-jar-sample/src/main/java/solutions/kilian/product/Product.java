package solutions.kilian.product;

import solutions.kilian.category.Category;

public class Product {

    private double price;
    private Category category;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }


}
