package models;

import io.sphere.client.model.VersionedId;
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

    public static List<ShopCategory> ofList(List<Category> categories) {
        List<ShopCategory> shopCategories = new ArrayList<ShopCategory>();
        for (Category category : categories) {
              shopCategories.add(new ShopCategory(category));
        }
        return shopCategories;
    }

    public Category get() {
        return category;
    }

    public String getId() {
        return category.getId();
    }

    public VersionedId getVersionedId() {
        return category.getIdAndVersion();
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

    public boolean hasChildren() {
        return !category.getChildren().isEmpty();
    }

    public List<ShopCategory> getPath() {
        List<ShopCategory> categoriesInPath = new ArrayList<ShopCategory>();
        for (Category categoryInPath : category.getPathInTree()) {
            categoriesInPath.add(new ShopCategory(categoryInPath));
        }
        return categoriesInPath;
    }

    public ShopCategory getRootAncestor() {
        List<ShopCategory> categoriesInPath = getPath();
        if (categoriesInPath.isEmpty()) {
            return this;
        } else {
            return getPath().get(0);
        }
    }

    public List<ShopCategory> getChildren() {
        List<ShopCategory> children = new ArrayList<ShopCategory>();
        for (Category child : category.getChildren()) {
            children.add(new ShopCategory(child));
        }
        return children;
    }

    public boolean hasAsAncestor(ShopCategory otherCategory) {
        return !this.equals(otherCategory) && getPath().contains(otherCategory);
    }

    public boolean hasInPath(ShopCategory otherCategory) {
        return getPath().contains(otherCategory);
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
        VersionedId versionedId = this.getVersionedId();
        VersionedId thatVersionedId = that.getVersionedId();

        if (!versionedId.getId().equals(thatVersionedId.getId())) return false;
        if (versionedId.getVersion() != thatVersionedId.getVersion()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return category.getIdAndVersion().getId().hashCode();
    }
}
