package com.ororura.utils;

import com.ororura.model.Parcel;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static com.ororura.api.IPostContract.ParcelType.*;

public final class CalculateTotalCost {
    private CalculateTotalCost() {}

    public static double calculateTotalCost(Parcel parcel) {
        if (parcel == null || parcel.getType() == null || !Double.isFinite(parcel.getWeight()) || parcel.getWeight() <= 0
                || !Double.isFinite(parcel.getDeclaredValue()) || parcel.getDeclaredValue() < 0) {
            throw new IllegalArgumentException("Некорректный вес или объявленная стоимость");
        }
        BigDecimal rate = switch (parcel.getType()) {
            case LETTER -> new BigDecimal("0.1");
            case BANDEROLKA -> new BigDecimal("0.3");
            case PARCEL -> new BigDecimal("0.5");
            default -> throw new IllegalArgumentException("Неизвестный тип отправления");
        };
        return rate.multiply(BigDecimal.valueOf(parcel.getWeight()))
                .add(BigDecimal.valueOf(parcel.getDeclaredValue())
                        .multiply(new BigDecimal("0.1")))
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
