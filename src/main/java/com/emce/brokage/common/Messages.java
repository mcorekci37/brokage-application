package com.emce.brokage.common;

public class Messages {
    public static final String YOU_CAN_ONLY_S_FROM_THIS_ENDPOINT_MSG = "You can only %s from this endpoint";
    public static final String CUSTOMER_ID_IN_PATH_AND_BODY_NOT_MATCH_MSG = "customerId in path and body doesn't match";
    public static final String TOKEN_EXPIRED_MSG = "Token is expired or invalid";
    public static final String EMAIL_ALREADY_EXISTS_MSG = "Email already exists! mail: %s";
    public static final String TOKEN_NOT_VALID_MSG = "Token cannot be validated";
    public static final String ACCESS_DENIED_FOR_USER_MSG = "You do not have permission to update the user with id %s";

    public static final String USER_NOT_FOUND_MSG = "User not found with email %s";
    public static final String USER_ID_NOT_FOUND_MSG = "User not found with id %s";
    public static final String ERROR_AUTHENTICATION_FAILED_MSG = "Error: Authentication failed.";
    public static final String ERROR_USER_NOT_FOUND_MSG = "Error: User not found. JWT token is invalid.";
    public static final String ORDER_NOT_FOUND_MSG = "Order not found with id %d";
    public static final String ONLY_PENDING_ORDERS_CAN_BE_CANCELED_OR_MATCHED_MSG = "Only pending orders can be canceled or matched";
    public static final String ASSET_S_HAS_NOT_ENOUGH_SIZE_MSG = "Asset %s has not enough size.";
    public static final String ASSET_NOT_FOUND_FOR_ASSET_NAME_S_MSG = "Asset not found for assetName: %s";

}
