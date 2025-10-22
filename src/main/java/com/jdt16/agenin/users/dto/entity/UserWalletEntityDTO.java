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
@Table(name = TableNameEntityUtility.TABLE_USER_WALLET)
public class UserWalletEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_WALLET_ID, nullable = false, updatable = false)
    private UUID userWalletEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_WALLET_USER_ID, nullable = false, updatable = false)
    private UUID userWalletEntityDTOUserId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_WALLET_AMOUNT, nullable = false)
    private BigDecimal userWalletEntityDTOAmount;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_WALLET_LAST_UPDATE, nullable = false)
    private LocalDateTime userWalletEntityDTOLastUpdate;
}
