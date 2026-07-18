package com.swordverse.server.auth.persistence.repository;

import com.swordverse.server.auth.persistence.entity.Session;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from Session session where session.id = :sessionId")
    Optional<Session> findByIdForUpdate(@Param("sessionId") UUID sessionId);
}
