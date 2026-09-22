package com.ororura.domain.pricing;

import com.ororura.domain.model.Parcel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculateTotalCostTest {

    @Test
    void shouldCalculateParcelCost() {
        Parcel parcel = new Parcel();

        parcel.setType("PARCEL");
        parcel.setWeight(2.5);
        parcel.setDeclaredValue(0);

        double cost =
                CalculateTotalCost.calculateTotalCost(parcel);

        assertEquals(1.25, cost);
    }

    @Test
    void shouldRejectNegativeWeight() {
        Parcel parcel = new Parcel();

        parcel.setType("PARCEL");
        parcel.setWeight(-10);

        assertThrows(
                IllegalArgumentException.class,
                () -> CalculateTotalCost.calculateTotalCost(parcel)
        );
    }

    @Test
    void shouldRejectUnknownParcelType() {
        Parcel parcel = new Parcel();

        parcel.setType("UNKNOWN");
        parcel.setWeight(5);

        assertThrows(
                IllegalArgumentException.class,
                () -> CalculateTotalCost.calculateTotalCost(parcel)
        );
    }
}
