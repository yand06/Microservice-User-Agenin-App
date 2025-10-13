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
@Table(name = TableNameEntityUtility.TABLE_USERS)
public class UserEntityDTO {

    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_ID, nullable = false, updatable = false)
    private UUID userEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_FULLNAME, nullable = false)
    private String userEntityDTOFullName;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_PHONE_NUMBER, unique = true, nullable = false)
    private String userEntityDTOPhoneNumber;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_EMAIL, unique = true, nullable = false)
    private String userEntityDTOEmail;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_PASSWORD, nullable = false)
    private String userEntityDTOPassword;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_ROLE_ID, nullable = false)
    private UUID userEntityDTORoleId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_ROLE_NAME, nullable = false)
    private String userEntityDTORoleName;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_CREATED_DATE, nullable = false)
    private LocalDateTime userEntityDTOCreatedDate;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_UPDATED_DATE, nullable = false)
    private LocalDateTime userEntityDTOUpdatedDate;
}
