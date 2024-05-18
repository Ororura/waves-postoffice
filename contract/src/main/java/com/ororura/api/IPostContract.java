package com.ororura.api;

import com.ororura.model.MoneyTransfer;
import com.ororura.model.Parcel;
import com.ororura.model.User;
import com.wavesenterprise.sdk.contract.api.annotation.ContractAction;
import com.wavesenterprise.sdk.contract.api.annotation.ContractInit;
import com.wavesenterprise.sdk.contract.api.annotation.InvokeParam;

public interface IPostContract {
    @ContractInit
    void init();

    @ContractAction
    void createUser(@InvokeParam(name = "user") User user);

    @ContractAction
    void transferMoney(@InvokeParam(name = "money")MoneyTransfer moneyTransfer);

    @ContractAction
    void acceptTransfer(@InvokeParam(name = "id") int id);

    @ContractAction
    void deniedTransfer(@InvokeParam(name = "id")int id);

    @ContractAction
    void setPostmanEmployee(@InvokeParam(name = "user")String user, @InvokeParam(name = "status")boolean status);

    @ContractAction
    void changePersonalData(@InvokeParam(name = "user")User user);

    @ContractAction
    void sendPackage(@InvokeParam(name = "package")Parcel parcel);

    class PostOffice {
        public static final String POST_OFFICE = "POST_OFFICE";
        public static final String MAIN_POST_OFFICE = "MAIN_POST_OFFICE";
        public static final String SORTING_CENTER = "SORTING_CENTER";
    }

    class ConvertDate {
        public static final int DAY = 5;
    }

    class Role {
        public static final String USER = "USER";
        public static final String EMPLOYEE = "EMPLOYEE";
    }

    class ParcelType {
        public static final String LETTER = "LETTER";
        public static final String BANDEROLKA = "BANDEROLKA";
        public static final String PARCEL = "PARCEL";

    }

    class Keys {
        public static final String USERS_MAPPING = "USERS_MAPPING";
        public static final String TRANSFER_MONEY_MAPPING = "TRANSFER_MONEY_MAPPING";
        public static final String PARCEL_MAPPING = "PARCEL_MAPPING";

    }

    class DateSupply {
        public static final int LETTER = 5;
        public static final int BANDEROLKA = 10;
        public static final int PARCEL = 15;
    }
}
