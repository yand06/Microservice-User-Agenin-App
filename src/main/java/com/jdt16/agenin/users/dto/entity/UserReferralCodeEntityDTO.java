package com.jdt16.agenin.users.dto.entity;

import com.jdt16.agenin.users.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.users.utility.TableNameEntityUtility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = TableNameEntityUtility.TABLE_USER_REFERRAL_CODE,
        uniqueConstraints = @UniqueConstraint(columnNames = ColumnNameEntityUtility.COLUMN_ID_USER)
)
public class UserReferralCodeEntityDTO {

    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USER_REFERRAL_CODE_ID, nullable = false, updatable = false)
    private UUID userReferralEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_ID_USER, nullable = false, updatable = false)
    private UUID userReferralEntityDTOUserId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_REFERRAL_CODE, nullable = false)
    private String userReferralEntityDTOCode;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_REFERRAL_CODE_CREATED_AT, nullable = false)
    private LocalDateTime userReferralEntityDTOCreatedAt;
}
