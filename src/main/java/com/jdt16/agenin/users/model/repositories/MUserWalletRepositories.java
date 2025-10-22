package com.jdt16.agenin.users.model.repositories;

import com.jdt16.agenin.users.dto.entity.UserWalletEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MUserWalletRepositories extends JpaRepository<UserWalletEntityDTO, UUID> {
    Optional<UserWalletEntityDTO> findByUserWalletEntityDTOUserId(UUID id);
}
