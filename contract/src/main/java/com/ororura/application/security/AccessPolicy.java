package com.ororura.application.security;
import com.ororura.application.context.ContractContext;
import com.ororura.domain.model.User;
import com.ororura.domain.model.UserRole;
import com.ororura.domain.repository.ContractMetadataRepository;
public final class AccessPolicy {
    private final ContractMetadataRepository metadata;
    private final ContractContext context;
    public AccessPolicy(ContractMetadataRepository metadata, ContractContext context) {
        this.metadata = metadata; this.context = context;
    }
    public void requireOwner() {
        String owner = metadata.findOwner().orElseThrow(() ->
            new IllegalStateException("Владелец контракта не задан"));
        if (!owner.equals(context.caller())) {
            throw new SecurityException("Только администратор может выполнить операцию");
        }
    }
    public void requireEmployee(User user) {
        if (!UserRole.EMPLOYEE.equals(user.getRole()) || user.getPostId() == null) {
            throw new SecurityException("Действие доступно сотруднику отделения");
        }
    }
}
