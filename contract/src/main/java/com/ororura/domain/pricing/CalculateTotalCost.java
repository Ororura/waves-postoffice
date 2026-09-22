package com.ororura.domain.pricing;

import static com.ororura.domain.model.ParcelType.*;

import com.ororura.domain.model.Parcel;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** All amounts are in hundredths of the existing logical token. */
public final class CalculateTotalCost {
  private CalculateTotalCost() {}

  public static long calculateTotalCost(Parcel parcel) {
    if (parcel == null
        || parcel.getType() == null
        || !Double.isFinite(parcel.getWeight())
        || parcel.getWeight() <= 0
        || parcel.getDeclaredValue() < 0) {
      throw new IllegalArgumentException("Некорректный вес или объявленная стоимость");
    }
    BigDecimal rate =
        switch (parcel.getType()) {
          case LETTER -> new BigDecimal("0.1");
          case BANDEROLKA -> new BigDecimal("0.3");
          case PARCEL -> new BigDecimal("0.5");
          default -> throw new IllegalArgumentException("Неизвестный тип отправления");
        };
    BigDecimal minor =
        rate.multiply(BigDecimal.valueOf(parcel.getWeight()))
            .multiply(new BigDecimal("100"))
            .add(BigDecimal.valueOf(parcel.getDeclaredValue()).multiply(new BigDecimal("0.1")));
    long value = minor.setScale(0, RoundingMode.HALF_UP).longValueExact();
    if (value <= 0)
      throw new IllegalArgumentException("Стоимость доставки менее минимальной единицы");
    return value;
  }
}
