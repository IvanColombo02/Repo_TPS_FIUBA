package ar.uba.fi.ingsoft1.product_example.config;

public class ConfigConstants {
    public static final String ERROR_ENDPOINT = "/error";
    public static final String SWAGGER_UI_ENDPOINT = "/swagger-ui/**";
    public static final String API_DOCS_ENDPOINT = "/v3/api-docs/**";
    public static final String ALL_PATHS = "/**";

    public static final String USERS_ENDPOINT = "/users";
    public static final String USERS_FORGOT_PASSWORD_ENDPOINT = "/users/forgot-password";
    public static final String USERS_RESET_PASSWORD_ENDPOINT = "/users/reset-password";
    public static final String USERS_VERIFY_ENDPOINT = "/users/verify";
    public static final String SESSIONS_ENDPOINT = "/sessions";

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";
    public static final String EMPLOYEE_ROLE = "EMPLOYEE";

    public static final String CORS_ALL_ORIGINS = "*";
    public static final String CORS_ALL_METHODS = "*";
    public static final String CORS_ALL_HEADERS = "*";

    public static final String JWT_CLAIM_ROLE = "role";
    public static final String JWT_CLAIM_SUBJECT = "sub";
    public static final String JWT_AUTHORIZATION_HEADER = "Authorization";
    public static final String JWT_BEARER_PREFIX = "Bearer ";


    public static final String BEARER_AUTH_SCHEME_KEY = "Bearer Authentication";
    public static final String BEARER_SCHEME = "bearer";
    public static final String JWT_BEARER_FORMAT = "JWT";

    public static final String RESPONSE_BAD_REQUEST = "400";
    public static final String RESPONSE_FORBIDDEN = "403";
    public static final String RESPONSE_NOT_FOUND = "404";
    public static final String RESPONSE_UNAUTHORIZED = "401";
    public static final String RESPONSE_4XX = "4xx";

    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    public static final String ERROR_MESSAGE_KEY = "message";

    public static final String QUERY_PARAM_FILTER = "filter";
    public static final String QUERY_PARAM_SORT = "sort";
    public static final String QUERY_PARAM_IN = "query";
}

