package com.wallet.dto.response;

import com.wallet.entity.Role;
import java.time.Instant;

public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Instant createdAt;

    public UserProfileResponse(Long id, String name, String email, String phone, Role role, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
