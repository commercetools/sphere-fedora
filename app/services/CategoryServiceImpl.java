package services;

import com.google.common.base.Optional;
import io.sphere.client.shop.model.Category;
import models.ShopCategory;
import sphere.Sphere;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Singleton
public class CategoryServiceImpl implements CategoryService {
    private final Sphere sphere;

    @Inject
    public CategoryServiceImpl(final Sphere sphere) {
        this.sphere = sphere;
    }

    @Override
    public Optional<ShopCategory> getById(final String categoryId) {
        Category category = sphere.categories().getById(categoryId);
        if (category != null) {
            ShopCategory fetchedCategory = ShopCategory.of(category);
            return Optional.of(fetchedCategory);
        } else {
            return Optional.absent();
        }
    }

    @Override
    public Optional<ShopCategory> getBySlug(final Locale locale, final String categorySlug) {
        Category category = sphere.categories().getBySlug(categorySlug, locale);
        if (category != null) {
            ShopCategory fetchedCategory = ShopCategory.of(category);
            return Optional.of(fetchedCategory);
        } else {
            return Optional.absent();
        }
    }

    @Override
    public List<ShopCategory> getRoots() {
        List<ShopCategory> roots = new ArrayList<ShopCategory>();
        for (Category root : sphere.categories().getRoots()) {
            roots.add(ShopCategory.of(root));
        }
        return roots;
    }

    @Override
    public void refresh() {
        sphere.categories().rebuildAsync();
    }
}
