package controllers;

import services.*;

public class CheckoutController extends BaseController {
    private final OrderService orderService;
    private final CheckoutService checkoutService;

    protected CheckoutController(final CategoryService categoryService, final ProductService productService,
                                 final CartService cartService, final CustomerService customerService,
                                 final OrderService orderService, final CheckoutService checkoutService) {
        super(categoryService, productService, cartService, customerService);
        this.orderService = orderService;
        this.checkoutService = checkoutService;
    }

    public OrderService orderService() {
        return orderService;
    }

    public CheckoutService checkoutService() {
        return checkoutService;
    }

}
