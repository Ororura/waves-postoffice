package com.ororura.utils;

import com.ororura.model.Parcel;

import static com.ororura.api.IPostContract.ParcelType.PARCEL;
import static com.ororura.api.IPostContract.ParcelType.LETTER;
import static com.ororura.api.IPostContract.ParcelType.BANDEROLKA;


public class CalculateTotalCost {
    public static double calculateTotalCost(Parcel parcel) {
        double baseCost = getBasedCost(parcel.getType());
        return baseCost * parcel.getWeight() + parcel.getDeclaredValue() * 0.1;
    }

    private static double getBasedCost(String classType ) {
        return switch (classType) {
            case LETTER -> 0.1;
            case BANDEROLKA -> 0.3;
            case PARCEL -> 0.5;
            default -> 0;
        };
    }
}
