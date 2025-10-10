package com.jdt16.agenin.users.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ColumnNameEntityUtility {
    /* M_USERS */
    public static final String COLUMN_USERS_ID = "user_id";
    public static final String COLUMN_USERS_FULLNAME = "user_fullname";
    public static final String COLUMN_USERS_PHONE_NUMBER = "user_phone_number";
    public static final String COLUMN_USERS_EMAIL = "user_email";
    public static final String COLUMN_USERS_PASSWORD = "user_password";
    public static final String COLUMN_USERS_IS_ADMIN = "user_is_admin";
    public static final String COLUMN_USERS_IS_PARENT = "user_is_parent";
    public static final String COLUMN_USERS_CREATED_DATE = "user_created_date";
    public static final String COLUMN_USERS_UPDATED_DATE = "user_updated_date";
    public static final String COLUMN_USERS_STATUS = "user_status";

    /* USER_REFERRAL_CODE */
    public static final String COLUMN_USER_REFERRAL_CODE_ID = "user_referral_code_id";
    public static final String COLUMN_ID_USER = "id_user";
    public static final String COLUMN_USER_REFERRAL_CODE = "user_referral_code";
    public static final String COLUMN_USER_REFERRAL_CODE_CREATED_AT = "user_referral_code_created_at";
}
