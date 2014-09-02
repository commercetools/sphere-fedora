package models;

import io.sphere.client.shop.model.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShopCategory {
    private final Category category;

    ShopCategory(Category category) {
        this.category = category;
    }

    public static ShopCategory of(Category category) {
        return new ShopCategory(category);
    }

    public Category get() {
        return category;
    }

    public String getId() {
        return category.getId();
    }

    public String getName(Locale locale) {
        return category.getName(locale);
    }

    public String getSlug(Locale locale) {
        return category.getSlug(locale);
    }

    public String getDescription(Locale locale) {
        return category.getDescription(locale);
    }

    public int getLevel() {
        return category.getLevel();
    }

    public List<ShopCategory> getPath() {
        List<ShopCategory> categoriesInPath = new ArrayList<>();
        for (Category categoryInPath : category.getPathInTree()) {
            categoriesInPath.add(new ShopCategory(categoryInPath));
        }
        return categoriesInPath;
    }

    public List<ShopCategory> getChildren() {
        List<ShopCategory> children = new ArrayList<>();
        for (Category child : category.getChildren()) {
            children.add(new ShopCategory(child));
        }
        return children;
    }

    @Override
    public String toString() {
        return "ShopCategory{" +
                "category=" + category +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopCategory that = (ShopCategory) o;

        if (category != null ? !category.equals(that.category) : that.category != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return category != null ? category.hashCode() : 0;
    }
}
