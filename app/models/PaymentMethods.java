package models;

public enum PaymentMethods {
    VISA("VISA", "visa"), MASTERCARD("Mastercard", "mastercard"), PAYPAL("PayPal", "paypal");
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
