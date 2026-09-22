package com.ororura.application.service;

import com.ororura.application.context.ContractContext;
import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.Parcel;
import com.ororura.domain.model.ParcelStatus;
import com.ororura.domain.model.ParcelTransit;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.model.User;
import com.ororura.domain.pricing.ShippingCostCalculator;
import com.ororura.domain.repository.ParcelRepository;
import com.ororura.domain.repository.UserRepository;
import java.util.HashMap;

public final class ParcelService {
  private final ParcelRepository parcels;
  private final UserRepository users;
  private final UserService userService;
  private final PostOfficeService offices;
  private final ContractContext context;
  private final AccessPolicy access;

  public ParcelService(
      ParcelRepository parcels,
      UserRepository users,
      UserService userService,
      PostOfficeService offices,
      ContractContext context,
      AccessPolicy access) {
    this.parcels = parcels;
    this.users = users;
    this.userService = userService;
    this.offices = offices;
    this.context = context;
    this.access = access;
  }

  public void sendPackage(Parcel parcel) {
    if (parcel == null || parcel.getTrackNumber() == null || parcel.getTrackNumber().isBlank()) {
      throw new IllegalArgumentException("Укажите трек-номер");
    }
    offices.requireOffice(parcel.getNextOffice());
    User sender = userService.requireUser(context.caller());
    if (parcels.existsByTrackingNumber(parcel.getTrackNumber())) {
      throw new IllegalStateException("Трек-номер уже существует");
    }
    long cost = ShippingCostCalculator.calculateTotalCost(parcel);
    sender.debit(cost);
    parcel.assignSender(context.caller());
    parcel.assignShippingCost(cost);
    parcel.setStatus(ParcelStatus.ACCEPTED);
    users.save(sender);
    parcels.save(parcel);
  }

  public void checkoutParcel(String trackingNumber, int nextOfficeId) {
    User employee = userService.requireUser(context.caller());
    access.requireEmployee(employee);
    int currentOfficeId;
    try {
      currentOfficeId = Integer.parseInt(employee.getPostId().replaceFirst("^RR", ""));
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Некорректное отделение сотрудника", e);
    }
    HashMap<Integer, PostOffice> postOffices = offices.all();
    PostOffice current = postOffices.get(currentOfficeId);
    if (current == null || !postOffices.containsKey(nextOfficeId)) {
      throw new IllegalArgumentException("Отделение не найдено");
    }
    Parcel parcel =
        parcels
            .findByTrackingNumber(trackingNumber)
            .orElseThrow(
                () -> new IllegalArgumentException("Посылка не найдена: " + trackingNumber));
    if (parcel.getNextOffice() != currentOfficeId || currentOfficeId == nextOfficeId) {
      throw new IllegalStateException("Неверное направление передачи посылки");
    }
    ParcelTransit event = parcel.transferViaOffice(currentOfficeId, nextOfficeId, context.caller());
    current.recordTransit(event);
    parcels.save(parcel);
    offices.saveAll(postOffices);
  }
}
