package com.ororura.postoffice.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.ororura.postoffice.infrastructure.blockchain.WavesNodeClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contract")
public class ContractController {
    private final WavesNodeClient node;

    public ContractController(WavesNodeClient node) {
        this.node = node;
    }

    @GetMapping("/info")
    public JsonNode info() {
        return node.contractInfo();
    }

    @GetMapping("/state")
    public JsonNode state() {
        return node.contractState();
    }
}
