package com.jdt16.agenin.users.service.implementation.module;

import com.jdt16.agenin.users.components.generator.ReferralCodeGenerator;
import com.jdt16.agenin.users.components.handler.UserAuthJWT;
import com.jdt16.agenin.users.configuration.security.SecurityConfig;
import com.jdt16.agenin.users.dto.entity.UserEntityDTO;
import com.jdt16.agenin.users.dto.entity.UserReferralCodeEntityDTO;
import com.jdt16.agenin.users.dto.entity.UserRoleEntityDTO;
import com.jdt16.agenin.users.dto.entity.UsersReferralEntityDTO;
import com.jdt16.agenin.users.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.*;
import com.jdt16.agenin.users.model.repositories.*;
import com.jdt16.agenin.users.service.interfacing.module.UserService;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
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
    private final MUserBalanceRepositories userBalanceRepositories;
    private final SecurityConfig securityConfig = new SecurityConfig();
    private final ReferralCodeGenerator referralCodeGenerator = new ReferralCodeGenerator();
    private final UserAuthJWT userAuthJWT;
    private UserEntityDTO userEntityDTO = new UserEntityDTO();
    private UserReferralCodeEntityDTO userReferralCodeEntityDTO = new UserReferralCodeEntityDTO();

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

        userEntityDTO = UserEntityDTO.builder()
                .userEntityDTOId(newUserEntityDTO.getUserEntityDTOId())
                .userEntityDTOFullName(newUserEntityDTO.getUserEntityDTOFullName())
                .userEntityDTOEmail(newUserEntityDTO.getUserEntityDTOEmail())
                .userEntityDTOPhoneNumber(newUserEntityDTO.getUserEntityDTOPhoneNumber()).build();
        saveUsersReferral();
        return RestApiResponse.builder()
                .restAPIResponseCode(HttpStatus.OK.value())
                .restAPIResponseMessage("User successfully registered")
                .restAPIResponseResults(getUserResponse(newUserEntityDTO))
                .build();
    }

    private UserRoleEntityDTO findRoleForRegistration(@Nullable String referralCode) {
        final boolean hasReferral = referralCode != null && !referralCode.isBlank();
        final String roleName = hasReferral ? "SUB-AGENT" : "AGENT";
        return userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase(roleName)
                .orElseThrow(() -> new CoreThrowHandlerException(
                        "Role '%s' Not yet prepared, please seed the M_ROLE data first.".formatted(roleName)));
    }

    private void referralCodeValidation(UserRequest userRequest) {
        String referralCode = trimToNull(userRequest.getUserEntityDTOReferralCode());
        if (referralCode != null) {
            if (!tUserReferralCodeRepositories.existsByUserReferralEntityDTOCodeIgnoreCase(referralCode)) {
                throw new CoreThrowHandlerException("Referral Code not found");
            }
            userReferralCodeEntityDTO = tUserReferralCodeRepositories
                    .findByUserReferralEntityDTOCodeIgnoreCase(referralCode)
                    .orElseThrow(() -> new CoreThrowHandlerException("Referral Code not found"));
        } else {
            userReferralCodeEntityDTO = null;
        }
    }

    private void saveUsersReferral() {
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

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    @Override
    public RestApiResponse<Object> generateReferralCode(UUID userId) {
        if (!userRepositories.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        if (tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)) {
            throw new IllegalStateException("The user already has a referral code..");
        }
        String referralCode = referralCodeGenerator.generateReferralCode();
        if (referralCode == null || referralCode.isEmpty()) {
            throw new IllegalStateException("Referral referralCode generation failed");
        }
        UserReferralCodeEntityDTO referralCodeEntityDTO = UserReferralCodeEntityDTO.builder()
                .userReferralEntityDTOId(UUID.randomUUID())
                .userReferralEntityDTOUserId(userId)
                .userReferralEntityDTOCode(referralCode)
                .userReferralEntityDTOCreatedAt(LocalDateTime.now())
                .build();
        tUserReferralCodeRepositories.save(referralCodeEntityDTO);
        UserReferralCodeResponse referralCodeResponse = UserReferralCodeResponse.builder()
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
        Optional<UserEntityDTO> userOpt = isEmailLike(identifier)
                ? userRepositories.findByUserEntityDTOEmailIgnoreCase(identifier)
                : userRepositories.findByUserEntityDTOPhoneNumber(identifier);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Email or phone number not found");
        }
        UserEntityDTO user = userOpt.get();

        boolean ok = securityConfig.passwordEncoder().matches(
                userLoginRequest.getUserPassword(),
                user.getUserEntityDTOPassword()
        );
        String accessToken = userAuthJWT.generateAuthToken(user, 3600);
        if (!ok) {
            throw new IllegalStateException("Invalid Password");
        }
        UserLoginResponse userLoginResponse = UserLoginResponse.builder()
                .userLoginResponseToken(accessToken)
                .userEntityDTOId(user.getUserEntityDTOId())
                .userEntityDTOFullName(user.getUserEntityDTOFullName())
                .userEntityDTOEmail(user.getUserEntityDTOEmail())
                .userEntityDTOPhoneNumber(user.getUserEntityDTOPhoneNumber())
                .userEntityDTORoleName(user.getUserEntityDTORoleName())
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
        List<UsersDownlineResponse> data = usersDownline.stream().map(usersReferralEntityDTO -> {
            BigDecimal commissionValue = userBalanceRepositories
                    .findBalanceAmountByUserId(usersReferralEntityDTO.getUsersReferralEntityDTOInviteeUserId())
                    .orElse(BigDecimal.ZERO);
            return UsersDownlineResponse.builder()
                    .usersReferralEntityDTOInviteeUserId(usersReferralEntityDTO.getUsersReferralEntityDTOInviteeUserId())
                    .usersReferralEntityDTOInviteeUserFullName(
                            usersReferralEntityDTO.getUsersReferralEntityDTOInviteeUserFullName())
                    .usersReferralEntityDTOInviteeUserPhoneNumber(
                            usersReferralEntityDTO.getUsersReferralEntityDTOInviteeUserPhoneNumber())
                    .usersReferralEntityDTOInviteeUserEmail(
                            usersReferralEntityDTO.getUsersReferralEntityDTOInviteeUserEmail())
                    .usersReferralEntityDTOInviteeCommissionValue(commissionValue)
                    .build();
        }).collect(Collectors.toList());
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
                .orElseThrow(() -> new CoreThrowHandlerException("Referral code note found"));
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
}


