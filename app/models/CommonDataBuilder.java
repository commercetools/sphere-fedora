package models;

import java.util.List;

import play.i18n.Lang;

public final class CommonDataBuilder {
    private UserContext userContext;
    private List<Lang> availableLang;

    private CommonDataBuilder(final UserContext userContext, final List<Lang> availableLang) {
        this.userContext = userContext;
        this.availableLang = availableLang;
    }

    public static CommonDataBuilder of(final UserContext userContext, final List<Lang> availableLang) {
        return new CommonDataBuilder(userContext, availableLang);
    }

    public CommonData build() {
        return new CommonData(userContext, availableLang);
    }
}
