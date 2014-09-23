package comparators;

import io.sphere.client.shop.model.Category;

import java.util.Comparator;
import java.util.Locale;

public final class ByNameCategoryComparator implements Comparator<Category> {
    private final Locale locale;

    public ByNameCategoryComparator(Locale locale) {
        this.locale = locale;
    }

    @Override
    public int compare(Category c1, Category c2) {
        return c1.getName(locale).compareTo(c2.getName(locale));
    }
}
