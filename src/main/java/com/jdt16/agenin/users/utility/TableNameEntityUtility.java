package com.jdt16.agenin.users.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableNameEntityUtility {
    public static final String TABLE_USERS = "M_USERS";
    public static final String TABLE_ROLE = "M_ROLE";
    public static final String TABLE_USER_REFERRAL_CODE = "T_USER_REFERRAL_CODE";
    public static final String TABLE_USERS_REFERRAL = "T_USERS_REFERRAL";
    public static final String TABLE_USER_BALANCE = "M_USER_BALANCE";
    public static final String TABLE_USER_BALANCE_HISTORICAL = "T_USERS_BALANCE_HISTORICAL";
    public static final String TABLE_PRODUCTS = "M_PRODUCTS";
    public static final String TABLE_COMMISSION = "M_COMMISSION";
    public static final String TABLE_TRANSACTION = "M_TRANSACTION";
    public static final String TABLE_TRANSACTION_OPEN_BANK_ACCOUNT = "T_TRANSACTION_OPEN_BANK_ACCOUNT";
    public static final String TABLE_AUDIT_LOGS = "AUDIT_LOGS";
    public static final String TABLE_USER_WALLET = "M_USER_WALLET";
}
