package com.ororura.postoffice.api;

import com.ororura.postoffice.infrastructure.blockchain.SignerNotConfiguredException;
import com.ororura.postoffice.infrastructure.blockchain.WavesTransactionRejectedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.ororura.postoffice.api.dto.ApiError;
import com.ororura.postoffice.infrastructure.blockchain.InvalidNodeResponseException;
import com.ororura.postoffice.infrastructure.blockchain.MissingContractIdException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> invalidRequest(Exception exception) {
        return ResponseEntity.badRequest()
                .body(new ApiError("INVALID_REQUEST", "Check request fields and data types"));
    }

    @ExceptionHandler(SignerNotConfiguredException.class)
    public ResponseEntity<ApiError> signerNotConfigured(SignerNotConfiguredException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("SIGNER_NOT_CONFIGURED", "Sandbox signer is not configured"));
    }

    @ExceptionHandler(WavesTransactionRejectedException.class)
    public ResponseEntity<ApiError> transactionRejected(WavesTransactionRejectedException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError("TRANSACTION_REJECTED", exception.getMessage()));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiError> nodeUnavailable(ResourceAccessException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("NODE_UNAVAILABLE", "Waves Enterprise node is unavailable"));
    }

    @ExceptionHandler(MissingContractIdException.class)
    public ResponseEntity<ApiError> missingContractId(MissingContractIdException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("CONTRACT_NOT_CONFIGURED", "WE_CONTRACT_ID is not configured"));
    }

    @ExceptionHandler({RestClientResponseException.class, InvalidNodeResponseException.class})
    public ResponseEntity<ApiError> badUpstream(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("NODE_ERROR", "Waves Enterprise returned an invalid or unsuccessful response"));
    }
}
