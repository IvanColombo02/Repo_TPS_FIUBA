package ar.uba.fi.ingsoft1.product_example.items.ingredients;

public class IngredientConstants {
    public static final String INGREDIENTS_ENDPOINT = "/ingredients";
    public static final String STOCK_ENDPOINT = "/stock";

    public static final int MIN_STOCK_QUANTITY = 0;

    public static final String RESPONSE_OK = "200";
    public static final String RESPONSE_CREATED = "201";
    public static final String RESPONSE_BAD_REQUEST = "400";
    public static final String RESPONSE_UNAUTHORIZED = "401";
    public static final String RESPONSE_FORBIDDEN = "403";
    public static final String RESPONSE_NOT_FOUND = "404";
    public static final String RESPONSE_NOT_MODIFIED = "304";

    public static final String ERROR_DUPLICATE_NAME = "No se puede crear el ingrediente: está presente uno con el mismo nombre";
    public static final String ERROR_ONLY_COMPONENT = "No se puede eliminar el ingrediente: es el único componente de al menos un producto.";
    public static final String ERROR_UNAUTHORIZED_ADMIN = "No autorizado (requiere rol ADMIN)";
    public static final String ERROR_NOT_AUTHENTICATED = "No autenticado";
    public static final String ERROR_INVALID_DATA = "Datos inválidos";

}

