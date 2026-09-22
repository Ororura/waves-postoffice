package com.ororura.api;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ororura.api.dto.*;
import com.ororura.domain.model.ParcelType;
import org.junit.jupiter.api.Test;

class ApiDtoSerializationTest {
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void parsesMinorUnitAmountAndParcelType() throws Exception {
    CreateTransferRequest transfer =
        mapper.readValue("{\"to\":\"bob\",\"amount\":125}", CreateTransferRequest.class);
    assertEquals(125, transfer.getAmount());
    CreateParcelRequest parcel =
        mapper.readValue(
            "{\"trackNumber\":\"RR1\",\"type\":\"PARCEL\",\"weight\":2.5,\"declaredValue\":100}",
            CreateParcelRequest.class);
    assertEquals(ParcelType.PARCEL, parcel.getType());
    assertEquals(100, parcel.getDeclaredValue());
  }
}
