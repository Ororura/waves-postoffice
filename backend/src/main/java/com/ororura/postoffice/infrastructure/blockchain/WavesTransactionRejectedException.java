package com.ororura.postoffice.infrastructure.blockchain;

public class WavesTransactionRejectedException extends RuntimeException {
    public WavesTransactionRejectedException(String message) {
        super(message);
    }
}
