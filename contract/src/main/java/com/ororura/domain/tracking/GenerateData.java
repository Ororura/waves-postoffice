package com.ororura.domain.tracking;

import com.ororura.domain.model.Parcel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerateData {
  public static String generateTrackNumber(Parcel parcel) {
    String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
    return "RR" + date + parcel.getAddressFrom() + parcel.getAddressTo();
  }
}
