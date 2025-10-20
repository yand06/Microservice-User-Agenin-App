package com.jdt16.agenin.users.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnNameEntityUtility {
    /* M_USERS */
    public static final String COLUMN_USERS_ID = "user_id";
    public static final String COLUMN_USERS_FULLNAME = "user_fullname";
    public static final String COLUMN_USERS_PHONE_NUMBER = "user_phone_number";
    public static final String COLUMN_USERS_EMAIL = "user_email";
    public static final String COLUMN_USERS_PASSWORD = "user_password";
    public static final String COLUMN_USERS_ROLE_ID = "role_id";
    public static final String COLUMN_USERS_ROLE_NAME = "role_name";
    public static final String COLUMN_USERS_CREATED_DATE = "user_created_date";
    public static final String COLUMN_USERS_UPDATED_DATE = "user_updated_date";

    /* T_USER_REFERRAL_CODE */
    public static final String COLUMN_USER_REFERRAL_CODE_ID = "user_referral_code_id";
    public static final String COLUMN_ID_USER = "id_user";
    public static final String COLUMN_USER_REFERRAL_CODE = "user_referral_code";
    public static final String COLUMN_USER_REFERRAL_CODE_CREATED_AT = "user_referral_code_created_at";

    /* M_ROLE */
    public static final String COLUMN_ROLE_ID = "role_id";
    public static final String COLUMN_ROLE_NAME = "role_name";
    public static final String COLUMN_ROLE_CREATED_DATE = "role_created_date";
    public static final String COLUMN_ROLE_UPDATED_DATE = "role_updated_date";

    /* T_USERS_REFERRAL */
    public static final String COLUMN_USERS_REFERRAL_ID = "users_referral_id";
    public static final String COLUMN_INVITEE_USER_EMAIL = "invitee_user_email";
    public static final String COLUMN_INVITEE_USER_PHONENUMBER = "invitee_user_phonenumber";
    public static final String COLUMN_INVITEE_USER_FULLNAME = "invitee_user_fullname";
    public static final String COLUMN_INVITEE_USER_ID = "invitee_user_id";
    public static final String COLUMN_REFERENCE_USER_ID = "reference_user_id";
    public static final String COLUMN_REFERENCE_USER_FULLNAME = "reference_user_fullname";
    public static final String COLUMN_REFERENCE_USER_PHONENUMBER = "reference_user_phonenumber";
    public static final String COLUMN_REFERENCE_USER_EMAIL = "reference_user_email";
    public static final String COLUMN_REFERRAL_CODE = "referral_code";

    /* M_USERS_BALANCE */
    public static final String COLUMN_USERS_BALANCE_ID = "user_balance_id";
    public static final String COLUMN_USERS_BALANCE_USER_ID = "id_user";
    public static final String COLUMN_USER_BALANCE_AMOUNT = "user_balance_amount";
    public static final String COLUMN_USER_BALANCE_LAST_UPDATE = "user_balance_last_update";

    /* M_COMMISSION */
    public static final String COLUMN_COMMISSIONS_ID = "commissions_id";
    public static final String COLUMN_COMMISSIONS_NAME = "commissions_name";
    public static final String COLUMN_COMMISSIONS_VALUE = "commissions_value";
    public static final String COLUMN_COMMISSIONS_SETUP = "commissions_setup";
    public static final String COLUMN_COMMISSIONS_PRODUCT_ID = "product_id";
    public static final String COLUMN_COMMISSIONS_PRODUCT_NAME = "product_name";
    public static final String COLUMN_COMMISSIONS_CREATED_DATE = "commissions_created_date";
    public static final String COLUMN_COMMISSIONS_UPDATED_DATE = "commissions_updated_date";

    public static final UUID USER_ID_ADMIN_VALUE = UUID.fromString("83aee4e6-cd95-418e-819b-569cec1d4809");

}
