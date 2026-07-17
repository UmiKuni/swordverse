package com.swordverse.server.auth.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.swordverse.server.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "sessions")
@Entity
public class SessionEntity extends BaseEntity {
    @Column(name = "user_id")
    private UUID userId;

    private LocalDateTime accessTokenExpiresAt;

    private LocalDateTime refreshTokenExpiresAt;

    private LocalDateTime revokedAt;
}
