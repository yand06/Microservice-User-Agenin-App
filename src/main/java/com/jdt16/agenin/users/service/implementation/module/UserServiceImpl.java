package com.jdt16.agenin.users.service.implementation.module;

import com.jdt16.agenin.users.components.generator.ReferralCodeGenerator;
import com.jdt16.agenin.users.components.handler.UserAuthJWT;
import com.jdt16.agenin.users.configuration.security.SecurityConfig;
import com.jdt16.agenin.users.dto.entity.*;
import com.jdt16.agenin.users.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.users.dto.request.UserAdminUpdateCommissionsRequest;
import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.*;
import com.jdt16.agenin.users.model.repositories.*;
import com.jdt16.agenin.users.service.interfacing.module.UserService;
import com.jdt16.agenin.users.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.users.utility.RequestContextUtil;
import com.jdt16.agenin.users.utility.TableNameEntityUtility;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final MUserRepositories userRepositories;
    private final TUserReferralCodeRepositories tUserReferralCodeRepositories;
    private final MUserRoleRepositories userRoleRepositories;
    private final TUsersReferralRepositories tUsersReferralRepositories;
    private final MUserBalanceRepositories mUserBalanceRepositories;
    private final TUsersBalanceHistoricalRepositories tUsersBalanceHistoricalRepositories;
    private final MUserWalletRepositories mUserWalletRepositories;
    private final MCommissionRepositories mCommissionRepositories;
    private final AuditLogProducerServiceImpl auditLogProducerServiceImpl;
    private final SecurityConfig securityConfig = new SecurityConfig();
    private final ReferralCodeGenerator referralCodeGenerator = new ReferralCodeGenerator();
    private final UserAuthJWT userAuthJWT;
    private final String ROLE_AGENT = "AGENT";
    private final String ROLE_SUB_AGENT = "SUB_AGENT";

    @Override
    @Transactional
    public RestApiResponse<Object> saveUser(UserRequest userRequest) {
        userRepositories.findByUserEntityDTOEmailIgnoreCase(userRequest.getUserEntityDTOEmail())
                .ifPresent(userEntityDTO -> {
                    throw new CoreThrowHandlerException("The user already exists with the email address: " + userRequest.getUserEntityDTOEmail());
                });
        userRepositories.findByUserEntityDTOPhoneNumber(userRequest.getUserEntityDTOPhoneNumber())
                .ifPresent(userEntityDTO -> {
                    throw new CoreThrowHandlerException("Users already exist with phone numbers: " + userRequest.getUserEntityDTOPhoneNumber());
                });

        UserReferralCodeEntityDTO userReferralCodeEntityDTO = this.userReferralCodeEntityDTO;
        referralCodeValidation(userRequest);

        UserRoleEntityDTO userRoleEntityDTO = findRoleForRegistration(userRequest.getUserEntityDTOReferralCode());

        UserEntityDTO newUserEntityDTO = UserEntityDTO.builder()
                .userEntityDTOId(UUID.randomUUID())
                .userEntityDTOFullName(userRequest.getUserEntityDTOFullName())
                .userEntityDTOEmail(userRequest.getUserEntityDTOEmail())
                .userEntityDTOPhoneNumber(userRequest.getUserEntityDTOPhoneNumber())
                .userEntityDTOPassword(securityConfig.passwordEncoder().encode(userRequest.getUserEntityDTOPassword()))
                .userEntityDTORoleId(userRoleEntityDTO.getUserRoleEntityDTOId())
                .userEntityDTORoleName(userRoleEntityDTO.getUserRoleEntityDTOName())
                .userEntityDTOCreatedDate(LocalDateTime.now())
                .userEntityDTOUpdatedDate(LocalDateTime.now())
                .build();

        userRepositories.save(newUserEntityDTO);
        saveUsersReferral(newUserEntityDTO, userReferralCodeEntityDTO);
        createUserBalance(newUserEntityDTO.getUserEntityDTOId());
        createUserWallet(newUserEntityDTO.getUserEntityDTOId());

        Map<String, Object> newData = buildUserDataMap(newUserEntityDTO);
        auditLogProducerServiceImpl.logCreate(
                TableNameEntityUtility.TABLE_USERS,
                newUserEntityDTO.getUserEntityDTOId(),
                newData,
                newUserEntityDTO.getUserEntityDTOId(),
                newUserEntityDTO.getUserEntityDTOFullName(),
                newUserEntityDTO.getUserEntityDTORoleId(),
                newUserEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );

        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("User successfully registered")
                .restAPIResponseResults(getUserResponse(newUserEntityDTO))
                .build();
    }

    private UserRoleEntityDTO findRoleForRegistration(@Nullable String referralCode) {
        final boolean hasReferral = referralCode != null && !referralCode.isBlank();
        final String roleName = hasReferral ? ROLE_SUB_AGENT : ROLE_AGENT;
        return userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase(roleName)
                .orElseThrow(() -> new CoreThrowHandlerException(
                        "Role '%s' Not yet prepared, please seed the M_ROLE data first.".formatted(roleName)));
    }

    private UserReferralCodeEntityDTO userReferralCodeEntityDTO;

    private void referralCodeValidation(UserRequest userRequest) {
        String userReferralCode = trimToNull(userRequest.getUserEntityDTOReferralCode());
        if (userReferralCode != null) {
            if (!tUserReferralCodeRepositories.existsByUserReferralEntityDTOCodeIgnoreCase(userReferralCode)) {
                throw new CoreThrowHandlerException("Referral Code not found");
            }
            userReferralCodeEntityDTO = tUserReferralCodeRepositories
                    .findByUserReferralEntityDTOCodeIgnoreCase(userReferralCode)
                    .orElseThrow(() -> new CoreThrowHandlerException("Referral Code not found"));
        } else {
            userReferralCodeEntityDTO = null;
        }
    }

    private void saveUsersReferral(UserEntityDTO userEntityDTO, UserReferralCodeEntityDTO userReferralCodeEntityDTO) {
        if (userReferralCodeEntityDTO == null) {
            return;
        }
        UserEntityDTO referenceUser = userRepositories.findById(userReferralCodeEntityDTO.getUserReferralEntityDTOUserId())
                .orElseThrow(() -> new CoreThrowHandlerException("Referral code owner not found"));

        UsersReferralEntityDTO usersReferralEntityDTO = UsersReferralEntityDTO.builder()
                .usersReferralEntityDTOId(UUID.randomUUID())
                .usersReferralEntityDTOInviteeUserId(userEntityDTO.getUserEntityDTOId())
                .usersReferralEntityDTOInviteeUserFullName(userEntityDTO.getUserEntityDTOFullName())
                .usersReferralEntityDTOInviteeUserPhoneNumber(userEntityDTO.getUserEntityDTOPhoneNumber())
                .usersReferralEntityDTOInviteeUserEmail(userEntityDTO.getUserEntityDTOEmail())
                .usersReferralEntityDTOReferenceUserId(referenceUser.getUserEntityDTOId())
                .usersReferralEntityDTOReferenceUserFullName(referenceUser.getUserEntityDTOFullName())
                .usersReferralEntityDTOReferenceUserPhoneNumber(referenceUser.getUserEntityDTOPhoneNumber())
                .usersReferralEntityDTOReferenceUserEmail(referenceUser.getUserEntityDTOEmail())
                .usersReferralEntityDTOReferralCode(userReferralCodeEntityDTO.getUserReferralEntityDTOCode())
                .build();

        tUsersReferralRepositories.save(usersReferralEntityDTO);
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trim = value.trim();
        return trim.isEmpty() ? null : trim;
    }

    private void createUserBalance(UUID userId) {
        UserBalanceEntityDTO userBalanceEntityDTO = UserBalanceEntityDTO.builder()
                .userBalanceEntityDTOId(UUID.randomUUID())
                .userBalanceEntityDTOUserId(userId)
                .userBalanceEntityDTOAmount(BigDecimal.valueOf(0))
                .userBalanceEntityDTOLastUpdate(LocalDateTime.now())
                .build();

        mUserBalanceRepositories.save(userBalanceEntityDTO);
    }

    private void createUserWallet(UUID userId) {
        UserWalletEntityDTO userWalletEntityDTO = UserWalletEntityDTO.builder()
                .userWalletEntityDTOId(UUID.randomUUID())
                .userWalletEntityDTOUserId(userId)
                .userWalletEntityDTOAmount(BigDecimal.valueOf(0))
                .userWalletEntityDTOLastUpdate(LocalDateTime.now())
                .build();

        mUserWalletRepositories.save(userWalletEntityDTO);
    }

    @Transactional
    @Override
    public RestApiResponse<Object> generateReferralCode(UUID userId) {
        UserEntityDTO userEntityDTO = userRepositories.findById(userId)
                .orElseThrow(() -> new CoreThrowHandlerException("User not found with id: " + userId));

        if (tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)) {
            throw new CoreThrowHandlerException("The user already has a referral code..");
        }

        String referralCode = referralCodeGenerator.generateReferralCode();
        if (referralCode == null || referralCode.isEmpty()) {
            throw new CoreThrowHandlerException("Referral referralCode generation failed");
        }

        UserReferralCodeEntityDTO referralCodeEntityDTO = UserReferralCodeEntityDTO.builder()
                .userReferralEntityDTOId(UUID.randomUUID())
                .userReferralEntityDTOUserId(userId)
                .userReferralEntityDTOCode(referralCode)
                .userReferralEntityDTOCreatedAt(LocalDateTime.now())
                .build();

        tUserReferralCodeRepositories.save(referralCodeEntityDTO);

        Map<String, Object> oldData;
        Map<String, Object> newData;

        if (userEntityDTO.getUserEntityDTORoleName().equals(ROLE_SUB_AGENT)) {
            oldData = Map.of("roleName", ROLE_SUB_AGENT);
            userEntityDTO.setUserEntityDTORoleName(ROLE_AGENT);
            userRepositories.save(userEntityDTO);
            newData = Map.of("roleName", userEntityDTO.getUserEntityDTORoleName());

            auditLogProducerServiceImpl.logUpdate(
                    TableNameEntityUtility.TABLE_USERS,
                    userEntityDTO.getUserEntityDTOId(),
                    oldData,
                    newData,
                    userId,
                    userEntityDTO.getUserEntityDTOFullName(),
                    userEntityDTO.getUserEntityDTORoleId(),
                    userEntityDTO.getUserEntityDTORoleName(),
                    RequestContextUtil.getUserAgent(),
                    RequestContextUtil.getClientIpAddress()
            );
        }

        Map<String, Object> referralData = Map.of(
                "referralCodeId", referralCodeEntityDTO.getUserReferralEntityDTOId().toString(),
                "userId", userId.toString(),
                "code", referralCode,
                "createdAt", referralCodeEntityDTO.getUserReferralEntityDTOCreatedAt().toString()
        );

        auditLogProducerServiceImpl.logCreate(
                TableNameEntityUtility.TABLE_USER_REFERRAL_CODE,
                referralCodeEntityDTO.getUserReferralEntityDTOId(),
                referralData,
                userId,
                userEntityDTO.getUserEntityDTOFullName(),
                userEntityDTO.getUserEntityDTORoleId(),
                userEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );

        UserReferralCodeResponse referralCodeResponse = UserReferralCodeResponse.builder()
                .userReferralEntityDTOId(referralCodeEntityDTO.getUserReferralEntityDTOId())
                .userReferralEntityDTOCode(referralCode)
                .userReferralEntityDTOCreatedAt(LocalDateTime.now())
                .build();

        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("Referral referralCode generated successfully")
                .restAPIResponseResults(referralCodeResponse)
                .build();
    }

    @Override
    public RestApiResponse<Object> login(UserLoginRequest userLoginRequest) {
        String identifier = userLoginRequest.getUserIdentifier();
        Optional<UserEntityDTO> userEntityDTOOptional = isEmailLike(identifier)
                ? userRepositories.findByUserEntityDTOEmailIgnoreCase(identifier)
                : userRepositories.findByUserEntityDTOPhoneNumber(identifier);

        if (userEntityDTOOptional.isEmpty()) {
            logLoginFailed(
                    identifier
            );
            throw new IllegalStateException("Email or phone number not found");
        }

        UserEntityDTO userEntityDTO = userEntityDTOOptional.get();
        boolean ok = securityConfig.passwordEncoder().matches(
                userLoginRequest.getUserPassword(),
                userEntityDTO.getUserEntityDTOPassword()
        );

        if (!ok) {
            logLoginFailed(
                    identifier,
                    "Invalid password attempt for user: " + userEntityDTO.getUserEntityDTOFullName(),
                    userEntityDTO.getUserEntityDTOId(),
                    userEntityDTO.getUserEntityDTOFullName(),
                    userEntityDTO.getUserEntityDTORoleId(),
                    userEntityDTO.getUserEntityDTORoleName()
            );
            throw new IllegalStateException("Invalid Password");
        }

        String accessToken = userAuthJWT.generateAuthToken(userEntityDTO, 3600);

        Map<String, Object> loginData = Map.of(
                "userId", userEntityDTO.getUserEntityDTOId().toString(),
                "loginAt", LocalDateTime.now().toString(),
                "loginMethod", isEmailLike(identifier) ? "EMAIL" : "PHONE",
                "status", "SUCCESS"
        );

        auditLogProducerServiceImpl.logCreate(
                TableNameEntityUtility.TABLE_USERS,
                UUID.randomUUID(),
                loginData,
                userEntityDTO.getUserEntityDTOId(),
                userEntityDTO.getUserEntityDTOFullName(),
                userEntityDTO.getUserEntityDTORoleId(),
                userEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );

        UserLoginResponse userLoginResponse = UserLoginResponse.builder()
                .userLoginResponseToken(accessToken)
                .userEntityDTOId(userEntityDTO.getUserEntityDTOId())
                .userEntityDTOFullName(userEntityDTO.getUserEntityDTOFullName())
                .userEntityDTOEmail(userEntityDTO.getUserEntityDTOEmail())
                .userEntityDTOPhoneNumber(userEntityDTO.getUserEntityDTOPhoneNumber())
                .userEntityDTORoleName(userEntityDTO.getUserEntityDTORoleName())
                .build();

        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("User login successful")
                .restAPIResponseResults(userLoginResponse)
                .build();
    }

    @Override
    public RestApiResponse<Object> getUserProfile(UUID userId) {
        UserEntityDTO userEntityDTO = userRepositories.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        UserProfileResponse userProfileResponse = UserProfileResponse.builder()
                .userEntityDTOId(userEntityDTO.getUserEntityDTOId())
                .userEntityDTOFullName(userEntityDTO.getUserEntityDTOFullName())
                .userEntityDTOEmail(userEntityDTO.getUserEntityDTOEmail())
                .userEntityDTOPhoneNumber(userEntityDTO.getUserEntityDTOPhoneNumber())
                .userEntityDTORoleName(userEntityDTO.getUserEntityDTORoleName())
                .build();

        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("User profile retrieved successfully")
                .restAPIResponseResults(userProfileResponse)
                .build();
    }

    @Override
    public RestApiResponse<Object> getUserDownline(UUID referenceUserId) {
        List<UsersReferralEntityDTO> usersDownline = tUsersReferralRepositories
                .findAllByUsersReferralEntityDTOReferenceUserId(referenceUserId);

        if (usersDownline.isEmpty()) {
            throw new CoreThrowHandlerException("Downline not found");
        }

        UUID parentBalanceId = mUserBalanceRepositories
                .findBalanceIdByUserId(referenceUserId)
                .orElseThrow(() -> new CoreThrowHandlerException("Parent balance not found"));

        List<UsersDownlineResponse> data = usersDownline.stream()
                .map(referral -> {
                    UUID childUserId = referral.getUsersReferralEntityDTOInviteeUserId();

                    BigDecimal totalCommission = tUsersBalanceHistoricalRepositories
                            .getTotalCommissionFromChild(parentBalanceId, childUserId);

                    return UsersDownlineResponse.builder()
                            .usersReferralEntityDTOInviteeUserId(childUserId)
                            .usersReferralEntityDTOInviteeUserFullName(
                                    referral.getUsersReferralEntityDTOInviteeUserFullName())
                            .usersReferralEntityDTOInviteeUserPhoneNumber(
                                    referral.getUsersReferralEntityDTOInviteeUserPhoneNumber())
                            .usersReferralEntityDTOInviteeUserEmail(
                                    referral.getUsersReferralEntityDTOInviteeUserEmail())
                            .usersReferralEntityDTOInviteeCommissionValue(totalCommission)
                            .build();
                })
                .collect(Collectors.toList());

        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("Downline retrieved successfully")
                .restAPIResponseResults(data)
                .build();
    }

    @Override
    public RestApiResponse<Object> getReferralCode(UUID userId) {
        UserReferralCodeEntityDTO userReferralCodeEntityDTO = tUserReferralCodeRepositories
                .findByUserReferralEntityDTOUserId(userId)
                .orElseThrow(() -> new CoreThrowHandlerException("Referral code not found"));

        UserReferralCodeResponse userReferralCodeResponse = UserReferralCodeResponse.builder()
                .userReferralEntityDTOId(userReferralCodeEntityDTO.getUserReferralEntityDTOId())
                .userReferralEntityDTOCode(userReferralCodeEntityDTO.getUserReferralEntityDTOCode())
                .userReferralEntityDTOCreatedAt(userReferralCodeEntityDTO.getUserReferralEntityDTOCreatedAt())
                .build();

        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("Referral code retrieved successfully")
                .restAPIResponseResults(userReferralCodeResponse)
                .build();
    }

    @Override
    public RestApiResponse<Object> updateCommissions(UUID productId, UserAdminUpdateCommissionsRequest adminUpdateCommissionsRequest) {
        CommissionEntityDTO commissionsEntityDTO = mCommissionRepositories.findByCommissionsEntityDTOProductId(productId)
                .orElseThrow(() -> new CoreThrowHandlerException("Product not found"));

        UserEntityDTO userEntityDTO = userRepositories.findByUserEntityDTOId(ColumnNameEntityUtility.USER_ID_ADMIN_VALUE)
                .orElseThrow(() -> new CoreThrowHandlerException("User ADMIN not found"));

        if (adminUpdateCommissionsRequest.getCommissionsEntityDTOValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new CoreThrowHandlerException("Commissions value cannot be less than 0");
        }

        Map<String, Object> oldData = Map.of(
                "value", commissionsEntityDTO.getCommissionsEntityDTOValue().toString(),
                "updatedDate", commissionsEntityDTO.getCommissionsEntityDTOUpdatedDate().toString()
        );

        commissionsEntityDTO.setCommissionsEntityDTOValue(adminUpdateCommissionsRequest.getCommissionsEntityDTOValue());
        commissionsEntityDTO.setCommissionsEntityDTOUpdatedDate(LocalDateTime.now());
        mCommissionRepositories.save(commissionsEntityDTO);

        Map<String, Object> newData = Map.of(
                "value", commissionsEntityDTO.getCommissionsEntityDTOValue().toString(),
                "updatedDate", commissionsEntityDTO.getCommissionsEntityDTOUpdatedDate().toString()
        );

        auditLogProducerServiceImpl.logUpdate(
                TableNameEntityUtility.TABLE_COMMISSION,
                productId,
                oldData,
                newData,
                userEntityDTO.getUserEntityDTOId(),
                userEntityDTO.getUserEntityDTOFullName(),
                userEntityDTO.getUserEntityDTORoleId(),
                userEntityDTO.getUserEntityDTORoleName(),
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );

        UserAdminUpdateCommissionsResponse userAdminUpdateCommissionsResponse = UserAdminUpdateCommissionsResponse.builder()
                .updateCommissionsEntityDTORoleName(userEntityDTO.getUserEntityDTORoleName())
                .updateCommissionsEntityDTOUserFullName(userEntityDTO.getUserEntityDTOFullName())
                .updateCommissionsEntityDTOProductName(commissionsEntityDTO.getCommissionsEntityDTOProductName())
                .updateCommissionsEntityDTOValue(commissionsEntityDTO.getCommissionsEntityDTOValue())
                .updateCommissionsEntityDTOCreatedDate(commissionsEntityDTO.getCommissionsEntityDTOCreatedDate())
                .updateCommissionsEntityDTOUpdatedDate(commissionsEntityDTO.getCommissionsEntityDTOUpdatedDate())
                .build();

        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("Commissions updated successfully")
                .restAPIResponseResults(userAdminUpdateCommissionsResponse)
                .build();
    }

    private boolean isEmailLike(String input) {
        return input != null && input.contains("@");
    }

    private static UserResponse getUserResponse(UserEntityDTO userEntityDTO) {
        return UserResponse.builder()
                .userEntityDTOId(userEntityDTO.getUserEntityDTOId())
                .userEntityDTOFullName(userEntityDTO.getUserEntityDTOFullName())
                .userEntityDTOEmail(userEntityDTO.getUserEntityDTOEmail())
                .userEntityDTOPhoneNumber(userEntityDTO.getUserEntityDTOPhoneNumber())
                .userEntityDTORoleName(userEntityDTO.getUserEntityDTORoleName())
                .userEntityDTOCreatedDate(userEntityDTO.getUserEntityDTOCreatedDate())
                .build();
    }

    private Map<String, Object> buildUserDataMap(UserEntityDTO user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getUserEntityDTOId().toString());
        data.put("fullName", user.getUserEntityDTOFullName());
        data.put("email", user.getUserEntityDTOEmail());
        data.put("phoneNumber", user.getUserEntityDTOPhoneNumber());
        data.put("roleId", user.getUserEntityDTORoleId().toString());
        data.put("roleName", user.getUserEntityDTORoleName());
        data.put("createdDate", user.getUserEntityDTOCreatedDate().toString());
        return data;
    }

    private void logLoginFailed(String identifier) {
        Map<String, Object> loginFailedData = Map.of(
                "identifier", identifier,
                "failureReason", "USER_NOT_FOUND",
                "details", "Email or phone number not found",
                "loginAt", LocalDateTime.now().toString(),
                "loginMethod", isEmailLike(identifier) ? "EMAIL" : "PHONE",
                "status", "FAILED"
        );

        auditLogProducerServiceImpl.logCreate(
                TableNameEntityUtility.TABLE_USERS,
                UUID.randomUUID(),
                loginFailedData,
                null,
                "UNKNOWN",
                null,
                "UNKNOWN",
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );
    }

    private void logLoginFailed(
            String identifier,
            String details,
            UUID userId,
            String userFullName,
            UUID roleId,
            String roleName) {

        Map<String, Object> loginFailedData = Map.of(
                "identifier", identifier,
                "userId", userId != null ? userId.toString() : "UNKNOWN",
                "failureReason", "INVALID_PASSWORD",
                "details", details,
                "loginAt", LocalDateTime.now().toString(),
                "loginMethod", isEmailLike(identifier) ? "EMAIL" : "PHONE",
                "status", "FAILED"
        );

        auditLogProducerServiceImpl.logCreate(
                TableNameEntityUtility.TABLE_USERS,
                UUID.randomUUID(),
                loginFailedData,
                userId,
                userFullName,
                roleId,
                roleName,
                RequestContextUtil.getUserAgent(),
                RequestContextUtil.getClientIpAddress()
        );
    }
}
