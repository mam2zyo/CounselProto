package io.notfound.counsel_back.security.repository;

import io.notfound.counsel_back.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
}