package com.ororura.postoffice.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.postoffice.infrastructure.blockchain.WavesNodeClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContractReadServiceTest {
    @Test
    void decodesExistingUserAndTransfer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WavesNodeClient node = mock(WavesNodeClient.class);
        String state = """
                [
                  {"key":"USERS_MAPPING_3Pabc", "type":"string", "value":"{\\"name\\":\\"Egor\\",\\"homeAddress\\":\\"SPB\\",\\"blockchainAddress\\":\\"3Pabc\\",\\"balance\\":650,\\"role\\":\\"USER\\",\\"postId\\":null}"},
                  {"key":"TRANSFER_V2_1", "type":"string", "value":"{\\"from\\":\\"3Pabc\\",\\"to\\":\\"3Nabc\\",\\"amount\\":100,\\"lifeTime\\":0,\\"status\\":\\"ACCEPTED\\"}"},
                  {"key":"TRANSFER_COUNTER_V2_NEXT", "type":"integer", "value":2}
                ]
                """;
        when(node.contractState()).thenReturn(mapper.readTree(state));
        ContractReadService reader = new ContractReadService(node, mapper);
        assertEquals(650L, reader.users().get(0).balance());
        assertEquals("Egor", reader.user("3Pabc").orElseThrow().name());
        assertEquals(1, reader.transfers().get(0).id());
        assertEquals("ACCEPTED", reader.transfers().get(0).status());
    }
}
