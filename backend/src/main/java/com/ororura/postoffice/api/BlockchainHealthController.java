package com.ororura.postoffice.api;

import com.ororura.postoffice.api.dto.HealthResponse;
import com.ororura.postoffice.infrastructure.blockchain.WavesNodeClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class BlockchainHealthController {
    private final WavesNodeClient node;

    public BlockchainHealthController(WavesNodeClient node) {
        this.node = node;
    }

    @GetMapping("/blockchain")
    public HealthResponse blockchain() {
        return new HealthResponse("UP", node.height());
    }
}
