package com.jdt16.agenin.users.model.repositories;

import com.jdt16.agenin.users.dto.entity.UserBalanceHistoricalEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface TUsersBalanceHistoricalRepositories extends JpaRepository<UserBalanceHistoricalEntityDTO, UUID> {

    /**
     * Menghitung total komisi yang didapat parent dari transaksi yang dilakukan child tertentu
     * Menggunakan native query untuk performa optimal
     *
     * @param parentBalanceId user_balance_id dari parent (dari M_USER_BALANCE)
     * @param childUserId     id_user dari child yang melakukan transaksi (dari M_TRANSACTION)
     * @return total balance_amount (komisi) yang didapat parent dari child
     */
    @Query(value = "SELECT COALESCE(SUM(h.balance_amount), 0) " +
            "FROM \"T_USERS_BALANCE_HISTORICAL\" h " +
            "INNER JOIN \"M_TRANSACTION\" t ON h.transaction_id = t.transaction_id " +
            "WHERE h.user_balance_id = :parentBalanceId " +
            "AND t.id_user = :childUserId",
            nativeQuery = true)
    BigDecimal getTotalCommissionFromChild(
            @Param("parentBalanceId") UUID parentBalanceId,
            @Param("childUserId") UUID childUserId
    );
}