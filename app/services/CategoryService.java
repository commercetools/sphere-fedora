package services;

import com.google.common.base.Optional;
import models.ShopCategory;

import java.util.List;
import java.util.Locale;

public interface CategoryService {

    /**
     * Gets the category with the provided ID.
     * @param categoryId internal identifier of the category.
     * @return the category with this ID, or absent if it does not exist.
     */
    Optional<ShopCategory> getById(String categoryId);

    /**
     * Gets the category with the provided slug.
     * @param locale the selected locale corresponding to the category slug.
     * @param categorySlug external and human-readable identifier of the category.
     * @return the category with this slug, or absent if it does not exist.
     */
    Optional<ShopCategory> getBySlug(Locale locale, String categorySlug);

    /**
     * Gets the root categories of the project.
     * @return the list of root categories.
     */
    List<ShopCategory> getRoots();

    /**
     * Fetches all categories from the backend again, refreshing the cached categories with latest version.
     */
    void refresh();
}
