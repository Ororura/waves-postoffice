package com.ororura.postoffice.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.postoffice.api.dto.Actor;
import com.ororura.postoffice.api.dto.CreateTransferRequest;
import com.ororura.postoffice.infrastructure.blockchain.ContractParameter;
import com.ororura.postoffice.infrastructure.blockchain.WavesTransactionClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContractWriteServiceTest {
    @Test
    void encodesTransferDtoAsJsonString() throws Exception {
        WavesTransactionClient client = mock(WavesTransactionClient.class);
        ObjectMapper mapper = new ObjectMapper();
        ContractWriteService service = new ContractWriteService(client, mapper);
        service.transfer(new CreateTransferRequest(Actor.OWNER, "recipient", 100));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ContractParameter>> params = ArgumentCaptor.forClass(List.class);
        verify(client).call(eq(Actor.OWNER), eq("transferMoney"), params.capture());
        ContractParameter money = params.getValue().get(0);
        assertEquals("string", money.type());
        assertEquals("money", money.key());
        assertEquals("recipient", mapper.readTree((String) money.value()).path("to").asText());
        assertEquals(100L, mapper.readTree((String) money.value()).path("amount").asLong());
    }
}
