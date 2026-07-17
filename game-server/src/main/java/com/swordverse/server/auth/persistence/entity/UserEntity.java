package com.swordverse.server.auth.persistence.entity;

import com.swordverse.server.common.persistence.AuditedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends AuditedEntity {
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(name = "display_name", length = 100) // If null, show username instead
    private String displayName;

    public UserEntity(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
