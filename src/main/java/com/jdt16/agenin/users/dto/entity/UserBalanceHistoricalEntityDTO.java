package com.jdt16.agenin.users.dto.entity;

import com.jdt16.agenin.users.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.users.utility.TableNameEntityUtility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = TableNameEntityUtility.TABLE_USER_BALANCE_HISTORICAL)
public class UserBalanceHistoricalEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_BALANCE_HISTORICAL_ID, nullable = false, updatable = false)
    private UUID userBalanceHistoricalEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_BALANCE_HISTORICAL_AMOUNT, nullable = false, updatable = false)
    private BigDecimal userBalanceHistoricalEntityDTOAmount;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_HISTORICAL_TRANSACTION_ID, nullable = false)
    private UUID userBalanceHistoricalEntityDTOTransactionId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_HISTORICAL_CREATED_DATE, nullable = false)
    private LocalDateTime userBalanceHistoricalEntityDTOCreatedDate;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_HISTORICAL_USER_BALANCE_ID, nullable = false)
    private UUID userBalanceHistoricalEntityDTOUserBalanceId;
}
