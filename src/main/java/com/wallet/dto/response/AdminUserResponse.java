package com.wallet.dto.response;

import com.wallet.entity.Role;
import com.wallet.entity.WalletStatus;

import java.time.Instant;

public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Long walletId;
    private WalletStatus walletStatus;
    private Instant createdAt;

    public AdminUserResponse() {
    }

    public AdminUserResponse(Long id, String name, String email, String phone, Role role, Long walletId, WalletStatus walletStatus, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.walletId = walletId;
        this.walletStatus = walletStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public WalletStatus getWalletStatus() {
        return walletStatus;
    }

    public void setWalletStatus(WalletStatus walletStatus) {
        this.walletStatus = walletStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
