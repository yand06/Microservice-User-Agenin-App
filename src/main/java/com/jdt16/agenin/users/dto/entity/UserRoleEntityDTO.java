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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = TableNameEntityUtility.TABLE_ROLE)
public class UserRoleEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_ROLE_ID, nullable = false, updatable = false)
    private UUID userRoleEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_ROLE_NAME, nullable = false)
    private String userRoleEntityDTOName;

    @Column(name = ColumnNameEntityUtility.COLUMN_ROLE_CREATED_DATE, unique = true, nullable = false)
    private LocalDateTime userRoleEntityDTOCreatedDate;

    @Column(name = ColumnNameEntityUtility.COLUMN_ROLE_UPDATED_DATE, unique = true, nullable = false)
    private LocalDateTime userRoleEntityDTOUpdatedDate;
}
