package com.ororura.application.service;

import com.ororura.application.context.ContractContext;
import com.ororura.application.security.AccessPolicy;
import com.ororura.domain.model.AcceptedParcel;
import com.ororura.domain.model.Parcel;
import com.ororura.domain.model.PostOffice;
import com.ororura.domain.model.User;
import com.ororura.domain.pricing.CalculateTotalCost;
import com.ororura.domain.repository.ParcelRepository;
import com.ororura.domain.repository.UserRepository;
import java.util.HashMap;
import java.util.List;

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
    List<Parcel> all = parcels.findAll();
    if (parcels.existsByTrackingNumber(parcel.getTrackNumber())) {
      throw new IllegalStateException("Трек-номер уже существует");
    }
    double cost = CalculateTotalCost.calculateTotalCost(parcel);
    if (!Double.isFinite(cost) || cost <= 0) {
      throw new IllegalArgumentException("Некорректная стоимость доставки");
    }
    sender.debit(cost);
    parcel.assignSender(context.caller());
    parcel.assignShippingCost(cost);
    all.add(parcel);
    users.save(sender);
    parcels.saveAll(all);
  }

  public void checkoutParcel(int parcelId, int nextOfficeId) {
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
    List<Parcel> allParcels = parcels.findAll();
    if (parcelId < 0 || parcelId >= allParcels.size()) {
      throw new IllegalArgumentException("Посылка не найдена: " + parcelId);
    }
    Parcel parcel = allParcels.get(parcelId);
    parcel.routeToOffice(nextOfficeId);
    current.acceptParcel(new AcceptedParcel(parcel, employee));
    parcels.saveAll(allParcels);
    offices.saveAll(postOffices);
  }
}
