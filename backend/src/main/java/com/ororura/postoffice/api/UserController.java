package com.ororura.postoffice.api;

import com.ororura.postoffice.api.dto.UserResponse;
import com.ororura.postoffice.application.ContractReadService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final ContractReadService reader;

    public UserController(ContractReadService reader) {
        this.reader = reader;
    }

    @GetMapping
    public List<UserResponse> users() {
        return reader.users();
    }

    @GetMapping("/{address}")
    public UserResponse user(@PathVariable String address) {
        return reader.user(address).orElseThrow(UserNotFoundException::new);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class UserNotFoundException extends RuntimeException {}
}
