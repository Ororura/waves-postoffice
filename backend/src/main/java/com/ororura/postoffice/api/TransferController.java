package com.ororura.postoffice.api;

import com.ororura.postoffice.api.dto.TransferResponse;
import com.ororura.postoffice.application.ContractReadService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {
    private final ContractReadService reader;

    public TransferController(ContractReadService reader) {
        this.reader = reader;
    }

    @GetMapping
    public List<TransferResponse> transfers() {
        return reader.transfers();
    }
}
