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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = TableNameEntityUtility.TABLE_COMMISSION)
public class CommissionEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_ID, nullable = false, updatable = false)
    private UUID commissionsEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_NAME, nullable = false)
    private String commissionsEntityDTOName;

    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_VALUE, nullable = false, precision = 19, scale = 4)
    private BigDecimal commissionsEntityDTOValue;

    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_SETUP, nullable = false)
    private String commissionsEntityDTOSetup;

    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_PRODUCT_ID, nullable = false)
    private UUID commissionsEntityDTOProductId;

    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_PRODUCT_NAME, nullable = false)
    private String commissionsEntityDTOProductName;

    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_CREATED_DATE, nullable = false)
    private LocalDateTime commissionsEntityDTOCreatedDate;

    @Column(name = ColumnNameEntityUtility.COLUMN_COMMISSIONS_UPDATED_DATE, nullable = false)
    private LocalDateTime commissionsEntityDTOUpdatedDate;
}
