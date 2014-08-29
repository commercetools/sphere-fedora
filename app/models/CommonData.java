package models;

import java.util.List;

import play.i18n.Lang;

/**
 * A container for stuff needed in almost every template.
 */
public class CommonData {
    private final UserContext userContext;
    private final List<Lang> availableLang;

    CommonData(final UserContext userContext, List<Lang> availableLang) {
        this.userContext = userContext;
        this.availableLang = availableLang;
    }

    public UserContext context() {
        return userContext;
    }

    public List<Lang> availableLang() {
        return availableLang;
    }
}
