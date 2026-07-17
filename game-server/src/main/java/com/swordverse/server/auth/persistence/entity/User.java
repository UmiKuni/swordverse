package com.swordverse.server.auth.persistence.entity;

import java.util.Objects;

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
public class User extends AuditedEntity {
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(name = "display_name", length = 100) // If null, show username instead
    private String displayName;

    public User(String username, String passwordHash, String displayName) {
        this.username = checkUsername(username);
        this.passwordHash = checkPasswordHash(passwordHash);
        this.displayName = checkDisplayName(displayName);
    }

    public void updateDisplayName(String displayName) {
        this.displayName = checkDisplayName(displayName);
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = checkPasswordHash(passwordHash);
    }

    private static String checkPasswordHash(String passwordHash) {
        Objects.requireNonNull(
                passwordHash,
                "Password hash must not be null");

        if (passwordHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Password hash must not be blank");
        }

        return passwordHash;
    }

    private String checkUsername(String username) {
        Objects.requireNonNull(username, "Username must not be null");

        if (username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be blank");
        }

        if (username.length() < 3 || username.length() > 50) {
            throw new IllegalArgumentException(
                    "Username must contain between 3 and 50 characters");
        }

        return username;
    }

    private static String checkDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }

        if (displayName.isBlank()) {
            throw new IllegalArgumentException(
                    "Display name must not be blank");
        }

        if (displayName.length() > 100) {
            throw new IllegalArgumentException(
                    "Display name must not exceed 100 characters");
        }

        return displayName;
    }

    public String getEffectiveDisplayName() {
        return displayName != null ? displayName : username;
    }
}
