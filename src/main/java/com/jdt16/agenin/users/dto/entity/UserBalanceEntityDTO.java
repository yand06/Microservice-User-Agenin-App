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
    private String userEntityDTOUserBalanceId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_ID, nullable = false, updatable = false)
    private UUID userEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_AMOUNT, nullable = false)
    private String userEntityDTOBalanceAmount;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_LAST_UPDATE, nullable = false)
    private String userEntityDTOBalanceLastUpdate;
}
