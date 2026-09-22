package com.ororura.postoffice.api;

import com.ororura.postoffice.api.dto.ActorRequest;
import com.ororura.postoffice.api.dto.CreateTransferRequest;
import com.ororura.postoffice.api.dto.CreditRequest;
import com.ororura.postoffice.api.dto.RegisterUserRequest;
import com.ororura.postoffice.api.dto.TransactionAccepted;
import com.ororura.postoffice.api.dto.UpdateProfileRequest;
import com.ororura.postoffice.application.ContractWriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Local sandbox API only: actor selects a configured node key, NOT an authenticated identity. */
@RestController
@RequestMapping("/api/v1")
public class ContractWriteController {
    private final ContractWriteService service;

    public ContractWriteController(ContractWriteService service) {
        this.service = service;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionAccepted register(@Valid @RequestBody RegisterUserRequest request) {
        return service.register(request);
    }

    @PatchMapping("/users/me")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionAccepted profile(@Valid @RequestBody UpdateProfileRequest request) {
        return service.updateProfile(request);
    }

    @PostMapping("/users/{address}/credit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionAccepted credit(@PathVariable String address, @Valid @RequestBody CreditRequest request) {
        return service.credit(address, request);
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionAccepted transfer(@Valid @RequestBody CreateTransferRequest request) {
        return service.transfer(request);
    }

    @PostMapping("/transfers/{id}/accept")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionAccepted accept(@PathVariable int id, @Valid @RequestBody ActorRequest request) {
        if (id < 0) {
            throw new IllegalArgumentException("Transfer ID must not be negative");
        }
        return service.accept(id, request);
    }

    @PostMapping("/transfers/{id}/reject")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionAccepted reject(@PathVariable int id, @Valid @RequestBody ActorRequest request) {
        if (id < 0) {
            throw new IllegalArgumentException("Transfer ID must not be negative");
        }
        return service.reject(id, request);
    }
}
