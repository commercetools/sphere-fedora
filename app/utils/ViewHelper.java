package utils;

import java.math.BigDecimal;
import java.util.*;

import com.neovisionaries.i18n.CountryCode;
import controllers.routes;
import forms.cartForm.AddToCart;
import forms.customerForm.UpdateCustomer;
import io.sphere.client.model.Money;
import io.sphere.client.model.SearchResult;
import io.sphere.client.shop.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import play.Logger;
import play.Play;
import play.data.Form;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http;
import sphere.Sphere;

import static play.data.Form.form;
import static play.mvc.Http.Context.Implicit.lang;

public class ViewHelper {

	/**
	 * Returns the current Cart in session.
	 */
	public static Cart getCurrentCart() {
		return Sphere.getInstance().currentCart().fetch();
	}

    public static Customer getCurrentCustomer() {
        Customer customer = null;
        if (Sphere.getInstance().isLoggedIn()) {
            customer = Sphere.getInstance().currentCustomer().fetch();
        }
        return customer;
    }

    public static boolean isLoggedIn() {
        return Sphere.getInstance().isLoggedIn();
    }

    public static Address getShippingAddress(Cart cart) {
        if (cart.getShippingAddress() != null) {
            return cart.getShippingAddress();
        }
        Address address = new Address(CountryCode.DE);
        if (Sphere.getInstance().isLoggedIn()) {
            Customer customer = Sphere.getInstance().currentCustomer().fetch();
            address.setEmail(customer.getEmail());
            address.setFirstName(customer.getName().getFirstName());
            address.setLastName(customer.getName().getLastName());
            address.setTitle(customer.getName().getTitle());
        }
        return address;
    }

    public static Address getBillingAddress(Cart cart) {
        if (cart.getBillingAddress() != null) {
            return cart.getBillingAddress();
        }
        if (cart.getShippingAddress() != null) {
            return cart.getShippingAddress();
        }
        return new Address(CountryCode.DE);
    }

    public static Money getTotalPrice(Cart cart) {
        if (cart.getShippingInfo() != null) {
            return cart.getTotalPrice().plus(cart.getShippingInfo().getPrice());
        }
        return cart.getTotalPrice();
    }

	/**
	 * Returns the list of root categories
	 */
	public static List<Category> getRootCategories() {
        return Sphere.getInstance().categories().getRoots();
	}

    public static String getCategoryPath(Category category) {
        String path = "";
        int level = category.getPathInTree().size();
        if (level > 1) {
            // Add first 2 oldest ancestors separated by '-'
            List<Category> ancestors = category.getPathInTree().subList(0, level - 1);
            String ancestorsPath = ancestors.get(0).getSlug(lang().toLocale());
            if (ancestors.size() > 1) {
                ancestorsPath += "-" + ancestors.get(1).getSlug(lang().toLocale());
            }
            path += ancestorsPath + "/";
        }
        // Add current category
        path += category.getPathInTree().get(level - 1).getSlug(lang().toLocale());
        return path;
    }

    public static String getReturnUrl() {
        return Http.Context.current().session().get("returnUrl");
    }

    public static List<Lang> getLanguages() {
        return Lang.availables();
    }

    public static String capitalizeInitials(String text) {
        return WordUtils.capitalizeFully(text);
    }

    public static String abbreviate(String text, int maxWidth) {
        if (text == null) return "";
        return StringUtils.abbreviate(text, maxWidth);
    }

    public static String getCountryName(String code) {
        try {
            return CountryCode.getByCode(code).getName();
        } catch (Exception e) {
            return "";
        }
    }

    public static Category getAncestor(Category category) {
        return category.getPathInTree().get(0);
    }

    public static String getActive(String selected, String current) {
        if (selected.equals(current)) return "active";
        return "";
    }

	/**
	 * Compares the categories and returns the 'active' class if are the same.
	 * 
	 * @param category
     * @param currentCategory
	 * @return 'active' if categories are the same, otherwise an empty string.
	 */
	public static String getActiveClass(Category category, Category currentCategory) {
        String active = "";
        if (currentCategory != null && currentCategory.getPathInTree().contains(category)) {
            active = "active";
        }
		return active;
	}

    public static String getActivePrice(String price) {
        String selected = getQuery("price");
        if (price.equals(selected)) return "active";
        return "";
    }

    public static String getActiveSort(String sort) {
        String selected = getQuery("sort");
        if (sort.equals(selected)) return "selected";
        return "";
    }

    public static String getActiveShow(int pageSize) {
        String selected = getQuery("show");
        if (selected.equals(String.valueOf(pageSize))) return "selected";
        return "";
    }

    public static BigDecimal getPercentage(double amount) {
        return BigDecimal.valueOf(amount * 100).stripTrailingZeros();
    }

    public static boolean isSet(Object object) {
        return object != null;
    }

    /**
	 * Check whether the given product has more than one attribute value
	 * 
	 * @param product
     * @param attributeName
	 * @return true if the product has more than one attribute value, false otherwise
	 */
	public static boolean hasMoreAttributeValues(Product product, String attributeName) {
        return product.getVariants().getAvailableAttributes(attributeName).size() > 1;
    }

    /**
     * Check whether the given Product has more than one 'color' attribute
     *
     * @param product
     * @return true if the product has more than one color, false otherwise
     */
    public static boolean hasMoreColors(Product product) {
        return hasMoreAttributeValues(product, "color");
    }

    /**
     * Check whether the given Product has more than one 'size' attribute
     *
     * @param product
     * @return true if the product has more than one size, false otherwise
     */
    public static boolean hasMoreSizes(Product product) {
        return hasMoreAttributeValues(product, "size");
    }

    public static Money getShippingCost() {
        // TODO Implement correct shipping cost
        return new Money(BigDecimal.valueOf(10), "EUR");
    }

    public static Call getCategoryUrl(Category category) {
        return getCategoryUrl(category, 1);
    }

    public static Call getCategoryUrl(Category category, int page) {
        return routes.Categories.select(getCategoryPath(category), page, 12, "", "");
    }

    public static Call getProductUrl(Product product, Variant variant) {
        return routes.Products.select(product.getSlug(), variant.getId());
    }

    public static Call getProductUrl(Product product, Variant variant, Category category) {
        return routes.Products.select(product.getSlug(), variant.getId());
    }

    /* Get possible variant sizes for a particular variant */
    public static List<String> getPossibleSizes(Product product, Variant variant) {
        List<Variant> variants = getPossibleVariants(product, variant, "size");
        List<String> sizes = new ArrayList<String>();
        for (Variant matchedVariant : variants) {
            sizes.add(matchedVariant.getString("size"));
        }
        return sizes;
    }

    /* Get variants with matching attributes but with different selected attribute
    * This method can be simplified if fixed and variable attributes are known beforehand */
    public static List<Variant> getPossibleVariants(Product product, Variant variant, String selectedAttribute) {
        List<Variant> matchingVariantList = new ArrayList<Variant>();
        List<Attribute> desiredAttributes = new ArrayList<Attribute>();
        // Get all other attributes with more than one different value
        for (Attribute attribute : variant.getAttributes()) {
            if (!selectedAttribute.equals(attribute.getName()) && hasMoreAttributeValues(product, attribute.getName())) {
                desiredAttributes.add(attribute);
            }
        }
        // Get variants matching all these other attributes but different selected attribute
        VariantList variantList = product.getVariants().byAttributes(desiredAttributes);
        for (Attribute attr : product.getVariants().getAvailableAttributes(selectedAttribute)) {
            if (variantList.byAttributes(attr).size() < 1) {
                matchingVariantList.add((product.getVariants().byAttributes(attr).first()).orNull());
            } else {
                matchingVariantList.add((variantList.byAttributes(attr).first()).orNull());
            }
        }
        matchingVariantList.removeAll(Collections.singleton(null));
        return matchingVariantList;
    }

    public static String getQuery(String query) {
        String value = Http.Context.current().request().getQueryString(query);
        if (value == null) return "";
        return value;
    }

    public static String buildQuery() {
        return buildQuery("", "");
    }

    public static String buildQuery(String key, String value) {
        String queryString = "?";
        if (!value.isEmpty()) {
            queryString += key + "=" + value;
        }
        for (Map.Entry<String,String[]> entry : Http.Context.current().request().queryString().entrySet()) {
            if (entry.getKey().equals(key)) continue;
            for (String val : entry.getValue()) {
                queryString += "&" + entry.getKey() + "=" + val;
            }
        }
        return queryString;
    }

}
