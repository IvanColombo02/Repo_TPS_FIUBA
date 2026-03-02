package ar.uba.fi.ingsoft1.product_example.order;

public class OrderConstants {
    public static final String ORDERS_ENDPOINT = "/orders";
    public static final String CALCULATE_DISCOUNTS_ENDPOINT = "/calculate-discounts";
    public static final String STATUS_ENDPOINT = "/status";
    public static final String ACTIVE_ENDPOINT = "/active";
    public static final int MIN_QUANTITY = 0;
    public static final double FREE_PRODUCT_THRESHOLD = 0.99;
    public static final double MIN_DISCOUNT_THRESHOLD = 0.01;
    public static final float DEFAULT_DISCOUNT = 0.0f;
    public static final double PERCENTAGE_DIVISOR = 100.0;
    public static final String RESPONSE_OK = "200";
    public static final String RESPONSE_CREATED = "201";
    public static final String RESPONSE_BAD_REQUEST = "400";
    public static final String RESPONSE_UNAUTHORIZED = "401";
    public static final String RESPONSE_FORBIDDEN = "403";
    public static final String RESPONSE_NOT_FOUND = "404";
    public static final String RESPONSE_CONFLICT = "409";
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String EMPLOYEE_ROLE = "EMPLOYEE";
    public static final String USER_ROLE = "USER";
    public static final String ERROR_CODE_INSUFFICIENT_STOCK = "INSUFFICIENT_STOCK";
    public static final String ERROR_CODE_BAD_REQUEST = "BAD_REQUEST";
    public static final String ERROR_CODE_CONFLICT = "CONFLICT";
    public static final String ERROR_USER_NOT_FOUND = "User not found with username: ";
    public static final String ERROR_UNSUPPORTED_PRINCIPAL = "Unsupported principal type: ";
    public static final String ERROR_NOT_AUTHENTICATED = "No autenticado";
    public static final String ERROR_UNAUTHORIZED_ADMIN_EMPLOYEE = "No autorizado (requiere rol ADMIN o EMPLOYEE)";
    
    private OrderConstants() {
        // Utility class
    }
}

