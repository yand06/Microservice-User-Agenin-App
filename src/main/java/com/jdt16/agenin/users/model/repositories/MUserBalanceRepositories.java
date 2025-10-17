package com.jdt16.agenin.users.model.repositories;

import com.jdt16.agenin.users.dto.entity.UserBalanceEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MUserBalanceRepositories extends JpaRepository<UserBalanceEntityDTO, UUID> {
    @Query("SELECT u.userBalanceEntityDTOAmount FROM UserBalanceEntityDTO u WHERE u.userBalanceEntityDTOUserId = :userBalanceEntityDTOUserId")
    Optional<BigDecimal> findBalanceAmountByUserId(@Param("userBalanceEntityDTOUserId") UUID userId);
}
