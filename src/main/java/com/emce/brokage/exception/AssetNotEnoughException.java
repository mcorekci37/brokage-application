package com.emce.brokage.exception;

public class AssetNotEnoughException extends RuntimeException {
    public AssetNotEnoughException(String message) {
        super(message);
    }
}