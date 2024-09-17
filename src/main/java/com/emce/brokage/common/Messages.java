package com.emce.brokage.common;

public class Messages {
    public static final String TOKEN_EXPIRED_MSG = "Token is expired or invalid";
    public static final String EMAIL_ALREADY_EXISTS_MSG = "Email already exists! mail: %s";
    public static final String TOKEN_NOT_VALID = "Token cannot be validated";
    public static final String ACCESS_DENIED_FOR_USER_MSG = "You do not have permission to update the user with id %s";

    public static final String USER_NOT_FOUND_MSG = "User not found with email %s";
    public static final String USER_ID_NOT_FOUND_MSG = "User not found with id %s";
    public static final String ERROR_AUTHENTICATION_FAILED = "Error: Authentication failed.";
    public static final String ERROR_USER_NOT_FOUND = "Error: User not found. JWT token is invalid.";
    public static final String ORDER_NOT_FOUND_MSG = "Order not found with id %d";
    public static final String ONLY_PENDING_ORDERS_CAN_BE_CANCELED_OR_MATCHED_MSG = "Only pending orders can be canceled or matched";
    public static final String ASSET_S_HAS_NOT_ENOUGH_SIZE = "Asset %s has not enough size.";
    public static final String ASSET_NOT_FOUND_FOR_ASSET_NAME_S_MSG = "Asset not found for assetName: %s";

}
