package models;

public enum PaymentMethods {
    CREDITCARD("Credit Card", "creditcard"), ELV("ELV", "elv");
    private final String label;
    private final String key;

    PaymentMethods(final String label, final String key) {
        this.label = label;
        this.key = key;
    }

    public String label() {
        return label;
    }

    public String key() {
        return key;
    }
}
