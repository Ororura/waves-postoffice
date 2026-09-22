package com.ororura.postoffice.infrastructure.blockchain;

public record ContractParameter(String type, String key, Object value) {
    public static ContractParameter string(String key, String value) {
        return new ContractParameter("string", key, value);
    }

    public static ContractParameter integer(String key, long value) {
        return new ContractParameter("integer", key, value);
    }
}
