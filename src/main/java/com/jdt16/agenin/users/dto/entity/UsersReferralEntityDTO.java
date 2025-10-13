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
@Table(name = TableNameEntityUtility.TABLE_USERS_REFERRAL)
public class UsersReferralEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_REFERRAL_ID, nullable = false, updatable = false)
    private UUID usersReferralEntityDTOId;

    @Column(name = ColumnNameEntityUtility.COLUMN_INVITEE_USER_ID, nullable = false)
    private UUID usersReferralEntityDTOInviteeUserId;

    @Column(name = ColumnNameEntityUtility.COLUMN_INVITEE_USER_FULLNAME, nullable = false)
    private String usersReferralEntityDTOInviteeUserFullName;

    @Column(name = ColumnNameEntityUtility.COLUMN_INVITEE_USER_PHONENUMBER, nullable = false)
    private String usersReferralEntityDTOInviteeUserPhoneNumber;

    @Column(name = ColumnNameEntityUtility.COLUMN_INVITEE_USER_EMAIL, nullable = false)
    private String usersReferralEntityDTOInviteeUserEmail;

    @Column(name = ColumnNameEntityUtility.COLUMN_REFERENCE_USER_ID, nullable = false)
    private UUID usersReferralEntityDTOReferenceUserId;

    @Column(name = ColumnNameEntityUtility.COLUMN_REFERENCE_USER_FULLNAME, nullable = false)
    private String usersReferralEntityDTOReferenceUserFullName;

    @Column(name = ColumnNameEntityUtility.COLUMN_REFERENCE_USER_PHONENUMBER, nullable = false)
    private String usersReferralEntityDTOReferenceUserPhoneNumber;

    @Column(name = ColumnNameEntityUtility.COLUMN_REFERENCE_USER_EMAIL, nullable = false)
    private String usersReferralEntityDTOReferenceUserEmail;

    @Column(name = ColumnNameEntityUtility.COLUMN_REFERRAL_CODE, nullable = false)
    private String usersReferralEntityDTOReferralCode;
}

