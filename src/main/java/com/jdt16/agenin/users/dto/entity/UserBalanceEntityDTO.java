package com.jdt16.agenin.users.dto.entity;

import com.jdt16.agenin.users.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.users.utility.TableNameEntityUtility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = TableNameEntityUtility.TABLE_USER_BALANCE)
public class UserBalanceEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_BALANCE_ID, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private String userBalanceEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_BALANCE_USER_ID, nullable = false, updatable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID userBalanceEntityDTOUserId;

    @Column(
            name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_AMOUNT,
            nullable = false,
            precision = 19, scale = 4
    )
    private String userBalanceEntityDTOAmount;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_LAST_UPDATE, nullable = false)
    private String userBalanceEntityDTOLastUpdate;
}
