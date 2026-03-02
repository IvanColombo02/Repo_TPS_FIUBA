package ar.uba.fi.ingsoft1.product_example.user;

import java.security.PublicKey;

public class UserConstants {
    public static final String USERS_ENDPOINT = "/users";
    public static final String ME_ENDPOINT = "/me";
    public static final String VERIFY_ENDPOINT = "/verify";
    public static final String RESET_PASSWORD_ENDPOINT = "/reset-password";
    public static final String FORGOT_PASSWORD_ENDPOINT = "/forgot-password";
    public static final String ROLE_ENDPOINT = "/role";

    public static final int MIN_PASSWORD_LENGHT = 6;
    public static final int MAX_PASSWORD_LENGTH = 50;
    
    public static final String SESSIONS_ENDPOINT = "/sessions";
    
    public static final String RESPONSE_OK = "200";
    public static final String RESPONSE_CREATED = "201";
    public static final String RESPONSE_BAD_REQUEST = "400";
    public static final String RESPONSE_UNAUTHORIZED = "401";
    public static final String RESPONSE_FORBIDDEN = "403";
    public static final String RESPONSE_NOT_FOUND = "404";
    public static final String RESPONSE_CONFLICT = "409";
    public static final String RESPONSE_FOUND = "302";
    
    public static final String ADMIN_ROLE = "ADMIN";
    public static final String USER_ROLE = "USER";
    public static final String EMPLOYEE_ROLE = "EMPLOYEE";
    
    public static final String REQUIRED_EMAIL_DOMAIN = "@fi.uba.ar";
    
    public static final String FRONTEND_BASE_URL = "https://grupo-11.tp1.ingsoft1.fiuba.ar";
    public static final String LOGIN_URL = FRONTEND_BASE_URL + "/login";
    public static final String VERIFY_URL = FRONTEND_BASE_URL + "/verify";
    
    public static final String QUERY_PARAM_MESSAGE = "message";
    public static final String QUERY_PARAM_TOKEN = "token";
    public static final String QUERY_PARAM_ERROR = "error";
    
    public static final String MESSAGE_EMAIL_VERIFIED = "email_verified";
    public static final String MESSAGE_INVALID = "invalid";
    
    public static final String PASSWORD_RESET_SUCCESS = "OK";
    public static final String PASSWORD_INVALID = "PASSWORD_INVALID";
    public static final String PASSWORD_SAME_AS_OLD = "PASSWORD_SAME_AS_OLD";
    public static final String TOKEN_INVALID = "TOKEN_INVALID";
    
    
    public static final String ERROR_NOT_AUTHENTICATED = "No autenticado";
    public static final String ERROR_UNAUTHORIZED_ADMIN = "No autorizado (requiere rol ADMIN)";
    public static final String ERROR_INVALID_DATA = "Datos inválidos (validaciones no cumplidas)";
    public static final String ERROR_USER_EXISTS = "Usuario ya existe (username o email duplicado)";
    public static final String ERROR_INVALID_TOKEN_OR_PASSWORD = "Token inválido o contraseña no cumple requisitos";
    public static final String ERROR_PASSWORD_SAME_AS_OLD = "La nueva contraseña es igual a la anterior";
    
    
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    private UserConstants() {
        // Utility class
    }
}

