package ar.uba.fi.ingsoft1.product_example.items.products;

public class ProductConstants {
    public static final String PRODUCTS_ENDPOINT = "/products";

    public static final int MIN_STOCK_QUANTITY = 0;

    public static final String RESPONSE_OK = "200";
    public static final String RESPONSE_CREATED = "201";
    public static final String RESPONSE_BAD_REQUEST = "400";
    public static final String RESPONSE_UNAUTHORIZED = "401";
    public static final String RESPONSE_FORBIDDEN = "403";
    public static final String RESPONSE_NOT_FOUND = "404";
    public static final String RESPONSE_NOT_MODIFIED = "304";

    public static final String ERROR_DUPLICATE_NAME = "No se puede crear el producto: está presente uno con el mismo nombre";
    public static final String ERROR_UNAUTHORIZED_ADMIN = "No autorizado (requiere rol ADMIN)";
    public static final String ERROR_NOT_AUTHENTICATED = "No autenticado";
    public static final String ERROR_INVALID_DATA = "Datos inválidos";

}

