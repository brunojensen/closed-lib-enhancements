package sample;

import solutions.kilian.category.Category;
import solutions.kilian.product.Product;

public class Main {

    public static void main(String[] args) {

        final Product withCategory = Product.create().withCategory(Category.create().withName("Name"));
        System.out.println(withCategory.getCategory().getName());
    }

}
