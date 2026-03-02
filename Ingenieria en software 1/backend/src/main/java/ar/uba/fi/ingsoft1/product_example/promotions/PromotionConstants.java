package ar.uba.fi.ingsoft1.product_example.promotions;

public class PromotionConstants {
    public static final String PROMOTIONS_ENDPOINT = "/promotions";
    public static final String ACTIVE_ENDPOINT = "/active";
    public static final String PRIORITIES_ENDPOINT = "/priorities";

    public static final int MIN_PRIORITY = 1;
    public static final int DEFAULT_PRIORITY = 0;
    public static final int UNLIMITED_PAGE_SIZE = Integer.MAX_VALUE;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final double PERCENTAGE_DIVISOR = 100.0;
    public static final double FLOATING_POINT_TOLERANCE = 0.01;
    public static final int DEFAULT_FREE_PRODUCT_QUANTITY = 1;

    public static final String RESPONSE_OK = "200";
    public static final String RESPONSE_CREATED = "201";
    public static final String RESPONSE_BAD_REQUEST = "400";
    public static final String RESPONSE_UNAUTHORIZED = "401";
    public static final String RESPONSE_FORBIDDEN = "403";
    public static final String RESPONSE_NOT_FOUND = "404";
    public static final String RESPONSE_NOT_MODIFIED = "304";

    public static final String ADMIN_ROLE = "ADMIN";

    public static final String DEFAULT_DESCRIPTION = "Description long enough for validation";

    public static final String VALID_EXPRESSION_TOTAL_AMOUNT = "{\"condition\":{\"type\":\"totalAmount\",\"operator\":\">=\",\"value\":1000},\"action\":{\"type\":\"percentageDiscount\",\"targetType\":\"ORDER\",\"percentage\":10}}";

    public static final String ERROR_FROM_DATE_AFTER_TO_DATE = "fromDate must be before toDate";
    public static final String ERROR_FROM_DATE_AFTER_EXISTING_TO_DATE = "fromDate must be before existing toDate";
    public static final String ERROR_TO_DATE_BEFORE_EXISTING_FROM_DATE = "toDate must be after existing fromDate";
    public static final String ERROR_INVALID_DATE_FORMAT = "Formato de fecha inválido";
    public static final String ERROR_DUPLICATE_IDS = "no pueden repetirse";
    public static final String ERROR_NON_EXISTENT_IDS = "inexistentes";
    public static final String ERROR_UNAUTHORIZED_ADMIN = "No autorizado (requiere rol ADMIN)";
    public static final String ERROR_NOT_AUTHENTICATED = "No autenticado";
    public static final String ERROR_INVALID_DATA = "Datos inválidos";
    
    private PromotionConstants() {
        // Utility class
    }
}

