package com.emce.brokage.exception;

public class OrderStatusNotEligibleException extends RuntimeException {
    public OrderStatusNotEligibleException(String message) {
        super(message);
    }
}