package com.jdt16.agenin.users.model.repositories;

import com.jdt16.agenin.users.dto.entity.UserReferralCodeEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserReferralCodeRepositories extends JpaRepository<UserReferralCodeEntityDTO, UUID> {
    boolean existsByUserReferralEntityDTOUserId(UUID userId);

    Optional<UserReferralCodeEntityDTO> findByUserReferralEntityDTOUserId(UUID userId);

    boolean existsByUserReferralEntityDTOCodeIgnoreCase(String userReferralCode);

    Optional<UserReferralCodeEntityDTO> findByUserReferralEntityDTOCodeIgnoreCase(String userReferralCode);
}
