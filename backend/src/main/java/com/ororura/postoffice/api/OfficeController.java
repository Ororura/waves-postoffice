package com.ororura.postoffice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.ororura.postoffice.application.ContractReadService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/offices")
public class OfficeController {
    private final ContractReadService reader;

    public OfficeController(ContractReadService reader) {
        this.reader = reader;
    }

    @GetMapping
    public List<JsonNode> offices() {
        return reader.offices();
    }
}
