package com.jdt16.agenin.users.service;

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
import com.jdt16.agenin.users.service.implementation.module.AuditLogProducerServiceImpl;
import com.jdt16.agenin.users.service.implementation.module.UserServiceImpl;
import com.jdt16.agenin.users.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.users.utility.TableNameEntityUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private MCommissionRepositories commissionRepositories;
    @Mock
    private MUserRepositories userRepositories;
    @Mock
    private MUserRoleRepositories userRoleRepositories;
    @Mock
    private TUserReferralCodeRepositories tUserReferralCodeRepositories;
    @Mock
    private TUsersReferralRepositories tUsersReferralRepositories;
    @Mock
    private MUserBalanceRepositories userBalanceRepositories;
    @Mock
    private MUserWalletRepositories userWalletRepositories;


    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ReferralCodeGenerator referralCodeGenerator;
    @Mock
    private UserAuthJWT userAuthJWT;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SecurityConfig securityConfig;

    @Mock
    private AuditLogProducerServiceImpl auditLogProducerServiceImpl;

    @InjectMocks
    private UserServiceImpl userService;

    private static UserRequest makeUserRequest(String email, String phone, String fullName, String pwd, String referral) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUserEntityDTOEmail(email);
        userRequest.setUserEntityDTOPhoneNumber(phone);
        userRequest.setUserEntityDTOFullName(fullName);
        userRequest.setUserEntityDTOPassword(pwd);
        userRequest.setUserEntityDTOReferralCode(referral);
        return userRequest;
    }

    private static UserLoginRequest makeLogin(String identifier, String password) {
        UserLoginRequest userLoginRequest = new UserLoginRequest();
        userLoginRequest.setUserIdentifier(identifier);
        userLoginRequest.setUserPassword(password);
        return userLoginRequest;
    }

    private static UserEntityDTO makeUser(UUID id, String fullName, String email, String phone, String role, String hashedPwd) {
        UserEntityDTO userEntityDTO = new UserEntityDTO();
        userEntityDTO.setUserEntityDTOId(id);
        userEntityDTO.setUserEntityDTOFullName(fullName);
        userEntityDTO.setUserEntityDTOEmail(email);
        userEntityDTO.setUserEntityDTOPhoneNumber(phone);
        userEntityDTO.setUserEntityDTORoleName(role);
        userEntityDTO.setUserEntityDTOPassword(hashedPwd);
        return userEntityDTO;
    }

    private static UserRoleEntityDTO makeRole(String name) {
        UserRoleEntityDTO userRoleEntityDTO = new UserRoleEntityDTO();
        userRoleEntityDTO.setUserRoleEntityDTOId(UUID.randomUUID());
        userRoleEntityDTO.setUserRoleEntityDTOName(name);
        return userRoleEntityDTO;
    }

    private static UsersReferralEntityDTO makeDownline(UUID inviteeId, String name, String phone, String email, UUID refId) {
        UsersReferralEntityDTO usersReferralEntityDTO = new UsersReferralEntityDTO();
        usersReferralEntityDTO.setUsersReferralEntityDTOId(UUID.randomUUID());
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserId(inviteeId);
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserFullName(name);
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserPhoneNumber(phone);
        usersReferralEntityDTO.setUsersReferralEntityDTOInviteeUserEmail(email);
        usersReferralEntityDTO.setUsersReferralEntityDTOReferenceUserId(refId);
        usersReferralEntityDTO.setUsersReferralEntityDTOReferralCode("REF-XYZ");
        return usersReferralEntityDTO;
    }

    private static UserReferralCodeEntityDTO makeReferralEntity(UUID userId, String code, LocalDateTime createdAt) {
        UserReferralCodeEntityDTO userReferralCodeEntityDTO = new UserReferralCodeEntityDTO();
        userReferralCodeEntityDTO.setUserReferralEntityDTOId(UUID.randomUUID());
        userReferralCodeEntityDTO.setUserReferralEntityDTOUserId(userId);
        userReferralCodeEntityDTO.setUserReferralEntityDTOCode(code);
        userReferralCodeEntityDTO.setUserReferralEntityDTOCreatedAt(createdAt);
        return userReferralCodeEntityDTO;
    }

    private static CommissionEntityDTO makeCommission(UUID id, String productName, UUID productId, BigDecimal value, LocalDateTime created, LocalDateTime updated) {
        CommissionEntityDTO commissionEntityDTO = new CommissionEntityDTO();
        commissionEntityDTO.setCommissionsEntityDTOId(id);
        commissionEntityDTO.setCommissionsEntityDTOProductName(productName);
        commissionEntityDTO.setCommissionsEntityDTOProductId(productId);
        commissionEntityDTO.setCommissionsEntityDTOValue(value);
        commissionEntityDTO.setCommissionsEntityDTOCreatedDate(created);
        commissionEntityDTO.setCommissionsEntityDTOUpdatedDate(updated);
        return commissionEntityDTO;
    }

    private static final String ROLE_SUB_AGENT = "SUB_AGENT";
    private static final String ROLE_AGENT = "AGENT";

    private void commonBoot() {
        when(securityConfig.passwordEncoder()).thenReturn(passwordEncoder);
        doNothing().when(auditLogProducerServiceImpl).logCreate(
                anyString(), any(), anyMap(), any(), anyString(), any(), anyString(), anyString(), anyString()
        );
        when(userAuthJWT.generateAuthToken(any(UserEntityDTO.class), anyInt()))
                .thenReturn("jwt-token-dummy");
    }


    @Nested
    @DisplayName("Register")
    class Register {
        @Nested
        @DisplayName("Positive Case")
        class RegisterPositiveCase {
            @Test
            @DisplayName("200 - without referral → role AGENT")
            void register_withoutReferral_success() {
                commonBoot();

                UserRequest userRequest = makeUserRequest("new.user@mail.com", "08123456789", "New User", "Passw0rd!", null);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("new.user@mail.com"))
                        .thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08123456789"))
                        .thenReturn(Optional.empty());
                when(userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase("AGENT"))
                        .thenReturn(Optional.of(makeRole("AGENT")));
                when(passwordEncoder.encode(anyString())).thenReturn("hashed");

                ArgumentCaptor<UserEntityDTO> userCap = ArgumentCaptor.forClass(UserEntityDTO.class);
                when(userRepositories.save(userCap.capture())).thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> restApiResponse = userService.saveUser(userRequest);

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("User successfully registered", restApiResponse.getRestAPIResponseMessage());
                verify(tUsersReferralRepositories, never()).save(any());

                UserEntityDTO saved = userCap.getValue();
                assertEquals("New User", saved.getUserEntityDTOFullName());
                assertEquals("AGENT", saved.getUserEntityDTORoleName());
            }

            @Test
            @DisplayName("200 - referral → role SUB_AGENT")
            void register_withReferral_success() {
                commonBoot();

                String code = "REF-12345";
                UserRequest userRequest = makeUserRequest("child@mail.com", "08111111111", "Child User", "S3cret!", code);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("child@mail.com"))
                        .thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08111111111"))
                        .thenReturn(Optional.empty());
                when(userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase("SUB_AGENT"))
                        .thenReturn(Optional.of(makeRole("SUB_AGENT")));
                when(passwordEncoder.encode("S3cret!")).thenReturn("hashed");

                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOCodeIgnoreCase(code)).thenReturn(true);

                UUID parentUserId = UUID.randomUUID();
                when(tUserReferralCodeRepositories.findByUserReferralEntityDTOCodeIgnoreCase(code))
                        .thenReturn(Optional.of(makeReferralEntity(parentUserId, code, LocalDateTime.now())));

                UserEntityDTO parent = makeUser(parentUserId, "Parent User", "parent@mail.com", "0800000000", null, null);

                when(userRepositories.findByUserEntityDTOId(parentUserId)).thenReturn(Optional.of(parent));

                ArgumentCaptor<UserEntityDTO> userCap = ArgumentCaptor.forClass(UserEntityDTO.class);
                when(userRepositories.save(userCap.capture())).thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> restApiResponse = userService.saveUser(userRequest);

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("User successfully registered", restApiResponse.getRestAPIResponseMessage());

                verify(tUsersReferralRepositories, never()).save(any(UsersReferralEntityDTO.class));

                assertEquals("SUB_AGENT", userCap.getValue().getUserEntityDTORoleName());
            }

            @Test
            @DisplayName("200 - referral code valid but missing owner → user saved SUB_AGENT")
            void register_referralOwnerMissing() {
                commonBoot();

                String code = "REF-OK";
                UserRequest userRequest = makeUserRequest("user@mail.com", "08123", "User", "pw", code);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("user@mail.com"))
                        .thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08123"))
                        .thenReturn(Optional.empty());

                lenient().when(userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase("SUB_AGENT"))
                        .thenReturn(Optional.of(makeRole("SUB_AGENT")));
                lenient().when(passwordEncoder.encode("pw")).thenReturn("hashed");

                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOCodeIgnoreCase(code))
                        .thenReturn(true);

                UUID fakeOwner = UUID.randomUUID();
                when(tUserReferralCodeRepositories.findByUserReferralEntityDTOCodeIgnoreCase(code))
                        .thenReturn(Optional.of(makeReferralEntity(fakeOwner, code, LocalDateTime.now())));

                when(userRepositories.findByUserEntityDTOId(fakeOwner))
                        .thenReturn(Optional.empty());

                ArgumentCaptor<UserEntityDTO> userCap = ArgumentCaptor.forClass(UserEntityDTO.class);
                when(userRepositories.save(userCap.capture())).thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> restApiResponse = userService.saveUser(userRequest);

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("User successfully registered", restApiResponse.getRestAPIResponseMessage());
                verify(userRepositories, times(1)).save(any(UserEntityDTO.class));
                verify(tUsersReferralRepositories, never()).save(any());

                assertEquals("SUB_AGENT", userCap.getValue().getUserEntityDTORoleName());
            }
        }

        @Nested
        @DisplayName("Negative Case")
        class RegisterNegativeCase {
            @Test
            @DisplayName("409 - duplicate email")
            void register_duplicateEmail() {
                commonBoot();

                UserRequest userRequest = makeUserRequest("dup@mail.com", "08123", "Dup", "pw", null);
                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("dup@mail.com"))
                        .thenReturn(Optional.of(new UserEntityDTO()));

                CoreThrowHandlerException coreThrowHandlerException = assertThrows(CoreThrowHandlerException.class, () -> userService.saveUser(userRequest));
                assertTrue(coreThrowHandlerException.getMessage().toLowerCase().contains("already exists"));
                verify(userRepositories, never()).save(any());
            }

            @Test
            @DisplayName("409 - duplicate phone")
            void register_duplicatePhone() {
                commonBoot();

                UserRequest userRequest = makeUserRequest("unique@mail.com", "08123456789", "DupPhone", "pw", null);
                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("unique@mail.com")).thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08123456789")).thenReturn(Optional.of(new UserEntityDTO()));

                CoreThrowHandlerException coreThrowHandlerException = assertThrows(CoreThrowHandlerException.class, () -> userService.saveUser(userRequest));
                assertTrue(coreThrowHandlerException.getMessage().toLowerCase().contains("users already exist"));
                verify(userRepositories, never()).save(any());
            }

            @Test
            @DisplayName("400 - role AGENT not prepared in DB")
            void register_roleNotPrepared() {
                commonBoot();

                UserRequest userRequest = makeUserRequest("user@mail.com", "08123", "User", "pw", null);
                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("user@mail.com")).thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08123")).thenReturn(Optional.empty());
                when(userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase("AGENT")).thenReturn(Optional.empty());

                CoreThrowHandlerException coreThrowHandlerException = assertThrows(CoreThrowHandlerException.class, () -> userService.saveUser(userRequest));
                assertTrue(coreThrowHandlerException.getMessage().contains("Role 'AGENT'"));
            }
        }
    }

    @Nested
    @DisplayName("Login")
    class Login {
        @Nested
        @DisplayName("Positive Case")
        class LoginPositiveCase {
            @Test
            @DisplayName("200 - success (email)")
            void login_withEmail_success() {
                commonBoot();

                String email = "user@mail.com";
                String raw = "Secret123!";

                var realEncoder = new BCryptPasswordEncoder();
                when(securityConfig.passwordEncoder()).thenReturn(realEncoder);
                String hashed = realEncoder.encode(raw);

                UUID id = UUID.randomUUID();
                UserEntityDTO userEntityDTO = makeUser(id, "User One", email, "0812xxxx", "AGENT", hashed);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase(email))
                        .thenReturn(Optional.of(userEntityDTO));

                when(userAuthJWT.generateAuthToken(any(UserEntityDTO.class), eq(3600)))
                        .thenReturn("jwt-token-123");

                RestApiResponse<Object> restApiResponse = userService.login(makeLogin(email, raw));

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("User login successful", restApiResponse.getRestAPIResponseMessage());
                UserLoginResponse userLoginResponse = (UserLoginResponse) restApiResponse.getRestAPIResponseResults();
                assertEquals("jwt-token-123", userLoginResponse.getUserLoginResponseToken());
                assertEquals(id, userLoginResponse.getUserEntityDTOId());
                assertEquals("User One", userLoginResponse.getUserEntityDTOFullName());
                assertEquals(email, userLoginResponse.getUserEntityDTOEmail());
                assertEquals("0812xxxx", userLoginResponse.getUserEntityDTOPhoneNumber());
                assertEquals("AGENT", userLoginResponse.getUserEntityDTORoleName());
            }


            @Test
            @DisplayName("200 - success (phone)")
            void login_withPhone_success() {
                commonBoot();

                String phone = "08123456789";
                String raw = "Passw0rd!";

                var realEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                when(securityConfig.passwordEncoder()).thenReturn(realEncoder);
                String hashed = realEncoder.encode(raw);

                UUID id = UUID.randomUUID();
                UserEntityDTO userEntityDTO = makeUser(id, "Phone User", "phone@mail.com", phone, "SUB_AGENT", hashed);

                when(userRepositories.findByUserEntityDTOPhoneNumber(phone))
                        .thenReturn(Optional.of(userEntityDTO));

                when(userAuthJWT.generateAuthToken(any(UserEntityDTO.class), eq(3600)))
                        .thenReturn("jwt-xyz");

                RestApiResponse<Object> restApiResponse = userService.login(makeLogin(phone, raw));

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("User login successful", restApiResponse.getRestAPIResponseMessage());
                UserLoginResponse userLoginResponse = (UserLoginResponse) restApiResponse.getRestAPIResponseResults();
                assertEquals("jwt-xyz", userLoginResponse.getUserLoginResponseToken());
                assertEquals(id, userLoginResponse.getUserEntityDTOId());
                assertEquals("Phone User", userLoginResponse.getUserEntityDTOFullName());
                assertEquals("phone@mail.com", userLoginResponse.getUserEntityDTOEmail());
                assertEquals(phone, userLoginResponse.getUserEntityDTOPhoneNumber());
                assertEquals("SUB_AGENT", userLoginResponse.getUserEntityDTORoleName());
            }
        }

        @Nested
        @DisplayName("Negative Case")
        class LoginNegativeCase {
            @Test
            @DisplayName("404 - user not found")
            void login_userNotFound() {
                commonBoot();

                String identifier = "notfound@mail.com";
                when(userRepositories.findByUserEntityDTOEmailIgnoreCase(identifier)).thenReturn(Optional.empty());

                IllegalStateException ex = assertThrows(IllegalStateException.class,
                        () -> userService.login(makeLogin(identifier, "whatever")));
                assertTrue(ex.getMessage().contains("Email or phone number not found"));
            }

            @Test
            @DisplayName("401 - password invalid")
            void login_invalidPassword() {
                commonBoot();

                String email = "user@mail.com";
                String raw = "wrong";
                String hashed = "$2a$10$hash...";
                UUID id = UUID.randomUUID();

                UserEntityDTO userEntityDTO = makeUser(id, "User", email, "0812", "AGENT", hashed);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase(email)).thenReturn(Optional.of(userEntityDTO));
                when(securityConfig.passwordEncoder().matches(raw, hashed)).thenReturn(false);

                IllegalStateException ex = assertThrows(IllegalStateException.class,
                        () -> userService.login(makeLogin(email, raw)));
                assertTrue(ex.getMessage().contains("Invalid Password"));
            }
        }
    }

    @Nested
    @DisplayName("generateReferralCode")
    class GenerateReferralCode {

        @Nested
        @DisplayName("Positive Case")
        class GenerateReferralCodePositiveCase {

            @Test
            @DisplayName("200 - success generate and upgrade SUB_AGENT -> AGENT")
            void generateReferralCode_success_withRoleUpgrade() {
                UUID userId = UUID.randomUUID();

                UserEntityDTO userEntityDTO = new UserEntityDTO();
                userEntityDTO.setUserEntityDTOId(userId);
                userEntityDTO.setUserEntityDTOFullName("Sub Agent One");
                userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
                userEntityDTO.setUserEntityDTORoleName(ROLE_SUB_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(userEntityDTO));
                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)).thenReturn(false);

                ArgumentCaptor<UserReferralCodeEntityDTO> userReferralCodeEntityDTOArgumentCaptor = ArgumentCaptor.forClass(UserReferralCodeEntityDTO.class);
                when(tUserReferralCodeRepositories.save(userReferralCodeEntityDTOArgumentCaptor.capture()))
                        .thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> restApiResponse = userService.generateReferralCode(userId);

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("Referral referralCode generated successfully", restApiResponse.getRestAPIResponseMessage());
                UserReferralCodeResponse userReferralCodeResponse = (UserReferralCodeResponse) restApiResponse.getRestAPIResponseResults();
                assertNotNull(userReferralCodeResponse.getUserReferralEntityDTOId());
                assertNotNull(userReferralCodeResponse.getUserReferralEntityDTOCreatedAt());

                UserReferralCodeEntityDTO saved = userReferralCodeEntityDTOArgumentCaptor.getValue();
                String actualCode = saved.getUserReferralEntityDTOCode();

                assertEquals(actualCode, userReferralCodeResponse.getUserReferralEntityDTOCode());
                assertNotNull(actualCode);
                assertFalse(actualCode.isBlank());
                assertTrue(actualCode.matches("[A-Z0-9]{6,12}"));

                assertEquals(ROLE_AGENT, userEntityDTO.getUserEntityDTORoleName());
                verify(userRepositories).save(userEntityDTO);
                verify(auditLogProducerServiceImpl, times(1)).logUpdate(
                        eq(TableNameEntityUtility.TABLE_USERS),
                        eq(userId), anyMap(), anyMap(),
                        eq(userId), eq("Sub Agent One"),
                        eq(userEntityDTO.getUserEntityDTORoleId()), eq(ROLE_AGENT),
                        anyString(), anyString()
                );
                verify(auditLogProducerServiceImpl, times(1)).logCreate(
                        eq(TableNameEntityUtility.TABLE_USER_REFERRAL_CODE),
                        eq(saved.getUserReferralEntityDTOId()), anyMap(),
                        eq(userId), eq("Sub Agent One"),
                        eq(userEntityDTO.getUserEntityDTORoleId()), eq(ROLE_AGENT),
                        anyString(), anyString()
                );
            }


            @Test
            @DisplayName("200 - success generate without upgrade role (AGENT)")
            void generateReferralCode_success_withoutRoleUpgrade() {
                UUID userId = UUID.randomUUID();

                UserEntityDTO userEntityDTO = new UserEntityDTO();
                userEntityDTO.setUserEntityDTOId(userId);
                userEntityDTO.setUserEntityDTOFullName("Agent Two");
                userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
                userEntityDTO.setUserEntityDTORoleName(ROLE_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(userEntityDTO));
                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)).thenReturn(false);

                ArgumentCaptor<UserReferralCodeEntityDTO> userReferralCodeEntityDTOArgumentCaptor = ArgumentCaptor.forClass(UserReferralCodeEntityDTO.class);
                when(tUserReferralCodeRepositories.save(userReferralCodeEntityDTOArgumentCaptor.capture()))
                        .thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> restApiResponse = userService.generateReferralCode(userId);

                UserReferralCodeEntityDTO saved = userReferralCodeEntityDTOArgumentCaptor.getValue();
                String actualCode = saved.getUserReferralEntityDTOCode();

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("Referral referralCode generated successfully", restApiResponse.getRestAPIResponseMessage());
                UserReferralCodeResponse userReferralCodeResponse = (UserReferralCodeResponse) restApiResponse.getRestAPIResponseResults();
                assertNotNull(userReferralCodeResponse.getUserReferralEntityDTOId());
                assertNotNull(userReferralCodeResponse.getUserReferralEntityDTOCreatedAt());

                assertEquals(actualCode, userReferralCodeResponse.getUserReferralEntityDTOCode());
                assertNotNull(actualCode);
                assertFalse(actualCode.isBlank());
                assertTrue(actualCode.matches("[A-Z0-9]{6,12}"));

                assertEquals(ROLE_AGENT, userEntityDTO.getUserEntityDTORoleName());
                verify(userRepositories, never()).save(any());

                verify(auditLogProducerServiceImpl, never()).logUpdate(
                        eq(TableNameEntityUtility.TABLE_USERS),
                        any(), anyMap(), anyMap(), any(), anyString(), any(), anyString(), anyString(), anyString()
                );
                verify(auditLogProducerServiceImpl, times(1)).logCreate(
                        eq(TableNameEntityUtility.TABLE_USER_REFERRAL_CODE),
                        eq(saved.getUserReferralEntityDTOId()),
                        anyMap(),
                        eq(userId),
                        eq("Agent Two"),
                        eq(userEntityDTO.getUserEntityDTORoleId()),
                        eq(ROLE_AGENT),
                        anyString(),
                        anyString()
                );
            }
        }

        @Nested
        @DisplayName("Negative Case")
        class GenerateReferralCodeNegativeCase {
            @Test
            @DisplayName("404 - user not found")
            void generateReferralCode_userNotFound() {
                UUID userId = UUID.randomUUID();
                when(userRepositories.findById(userId)).thenReturn(Optional.empty());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.generateReferralCode(userId));

                assertTrue(ex.getMessage().contains("User not found with id: " + userId));
                verifyNoInteractions(tUserReferralCodeRepositories, referralCodeGenerator, auditLogProducerServiceImpl);
            }

            @Test
            @DisplayName("409 - user already has referral code")
            void generateReferralCode_alreadyHasReferral() {
                UUID userId = UUID.randomUUID();

                UserEntityDTO userEntityDTO = new UserEntityDTO();
                userEntityDTO.setUserEntityDTOId(userId);
                userEntityDTO.setUserEntityDTOFullName("Has Code");
                userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
                userEntityDTO.setUserEntityDTORoleName(ROLE_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(userEntityDTO));
                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)).thenReturn(true);

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.generateReferralCode(userId));

                assertTrue(ex.getMessage().contains("The user already has a referral code.."));
                verify(referralCodeGenerator, never()).generateReferralCode();
                verify(tUserReferralCodeRepositories, never()).save(any());
                verify(auditLogProducerServiceImpl, never()).logCreate(any(), any(), anyMap(), any(), anyString(), any(), anyString(), anyString(), anyString());
                verify(auditLogProducerServiceImpl, never()).logUpdate(any(), any(), anyMap(), anyMap(), any(), anyString(), any(), anyString(), anyString(), anyString());
            }

            @Test
            @DisplayName("500 Generator null/empty")
            void generateReferralCode_generatorReturnsEmpty_noThrow() {
                UUID userId = UUID.randomUUID();
                UserEntityDTO userEntityDTO = new UserEntityDTO();
                userEntityDTO.setUserEntityDTOId(userId);
                userEntityDTO.setUserEntityDTOFullName("No Code");
                userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
                userEntityDTO.setUserEntityDTORoleName(ROLE_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(userEntityDTO));
                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)).thenReturn(false);
                when(referralCodeGenerator.generateReferralCode()).thenReturn("");

                userService.generateReferralCode(userId);
            }
        }
    }

    @Nested
    @DisplayName("Get Referral Code")
    class GetReferralCode {
        @Nested
        @DisplayName("Positive Case")
        class GetReferralCodePositiveCase {
            @Test
            @DisplayName("200 - success")
            void getReferral_success() {
                commonBoot();

                UUID userId = UUID.randomUUID();
                UserReferralCodeEntityDTO userReferralCodeEntityDTO = makeReferralEntity(userId, "REF-XYZ123", LocalDateTime.now());
                when(tUserReferralCodeRepositories.findByUserReferralEntityDTOUserId(userId))
                        .thenReturn(Optional.of(userReferralCodeEntityDTO));

                RestApiResponse<Object> restApiResponse = userService.getReferralCode(userId);
                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("Referral code retrieved successfully", restApiResponse.getRestAPIResponseMessage());

                UserReferralCodeResponse userReferralCodeResponse = (UserReferralCodeResponse) restApiResponse.getRestAPIResponseResults();
                assertEquals(userReferralCodeEntityDTO.getUserReferralEntityDTOId(), userReferralCodeResponse.getUserReferralEntityDTOId());
                assertEquals("REF-XYZ123", userReferralCodeResponse.getUserReferralEntityDTOCode());
                assertNotNull(userReferralCodeResponse.getUserReferralEntityDTOCreatedAt());
            }
        }

        @Nested
        @DisplayName("Negative Case")
        class GetReferralNegativeCase {
            @Test
            @DisplayName("404 - referral code not found")
            void getReferral_notFound() {
                commonBoot();

                UUID userId = UUID.randomUUID();
                when(tUserReferralCodeRepositories.findByUserReferralEntityDTOUserId(userId))
                        .thenReturn(Optional.empty());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.getReferralCode(userId));
                assertTrue(ex.getMessage().contains("Referral code not found"));
            }
        }
    }

    @Nested
    @DisplayName("Get User Profile")
    class GetUserProfile {
        @Nested
        @DisplayName("Positive Case")
        class GetUserProfilePositiveCase {
            @Test
            @DisplayName("200 - success")
            void getProfile_success() {
                commonBoot();

                UUID userId = UUID.randomUUID();
                UserEntityDTO userEntityDTO = makeUser(userId, "Jane Doe", "jane@mail.com", "081234567890", "AGENT", null);
                when(userRepositories.findById(userId)).thenReturn(Optional.of(userEntityDTO));

                RestApiResponse<Object> restApiResponse = userService.getUserProfile(userId);
                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("User profile retrieved successfully", restApiResponse.getRestAPIResponseMessage());

                UserProfileResponse userProfileResponse =
                        (UserProfileResponse) restApiResponse.getRestAPIResponseResults();

                assertEquals(userId, userProfileResponse.getUserEntityDTOId());
                assertEquals("Bayu wijaya", userProfileResponse.getUserEntityDTOFullName());
                assertEquals("bayu12@mail.com", userProfileResponse.getUserEntityDTOEmail());
                assertEquals("081234567890", userProfileResponse.getUserEntityDTOPhoneNumber());
                assertEquals("AGENT", userProfileResponse.getUserEntityDTORoleName());
            }
        }

        @Nested
        @DisplayName("Negative Case")
        class GetUserProfileNegativeCase {
            @Test
            @DisplayName("404 - user not found")
            void getProfile_notFound() {
                commonBoot();

                UUID userId = UUID.randomUUID();
                when(userRepositories.findById(userId)).thenReturn(Optional.empty());

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> userService.getUserProfile(userId));
                assertTrue(ex.getMessage().contains("User not found with ID: " + userId));
            }
        }
    }

    @Nested
    @DisplayName("Get User Downline")
    class GetUserDownline {
        @Nested
        @DisplayName("Positive Case")
        class GetUserDownlinePositiveCase {
            @Test
            @DisplayName("200 - success, The commission will be funded from the balance when available.")
            void getDownline_success() {
                commonBoot();

                UUID refUserId = UUID.randomUUID();
                UUID uuid = UUID.randomUUID();
                UUID uuid1 = UUID.randomUUID();

                UsersReferralEntityDTO childOne = makeDownline(uuid, "Child One", "0812-111", "c1@mail.com", refUserId);
                UsersReferralEntityDTO childTwo = makeDownline(uuid1, "Child Two", "0812-222", "c2@mail.com", refUserId);

                when(tUsersReferralRepositories.findAllByUsersReferralEntityDTOReferenceUserId(refUserId))
                        .thenReturn(List.of(childOne, childTwo));

                when(userBalanceRepositories.findBalanceAmountByUserId(uuid))
                        .thenReturn(Optional.of(new BigDecimal("150000")));
                when(userBalanceRepositories.findBalanceAmountByUserId(uuid1))
                        .thenReturn(Optional.empty());

                RestApiResponse<Object> restApiResponse = userService.getUserDownline(refUserId);

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("Downline retrieved successfully", restApiResponse.getRestAPIResponseMessage());
                assertTrue(restApiResponse.getRestAPIResponseResults() instanceof List<?>);

                List<UsersDownlineResponse> usersDownlineResponses =
                        (List<UsersDownlineResponse>) restApiResponse.getRestAPIResponseResults();

                assertEquals(2, usersDownlineResponses.size());
                assertEquals(new BigDecimal("150000"), usersDownlineResponses.get(0).getUsersReferralEntityDTOInviteeCommissionValue());
                assertEquals(BigDecimal.ZERO, usersDownlineResponses.get(1).getUsersReferralEntityDTOInviteeCommissionValue());
            }
        }

        @Nested
        @DisplayName("Negative Case")
        class GetUserDownlineNegativeCase {
            @Test
            @DisplayName("404 - downline not found")
            void getDownline_notFound() {
                commonBoot();

                UUID refUserId = UUID.randomUUID();
                when(tUsersReferralRepositories.findAllByUsersReferralEntityDTOReferenceUserId(refUserId))
                        .thenReturn(Collections.emptyList());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.getUserDownline(refUserId));
                assertTrue(ex.getMessage().contains("Downline not found"));
            }
        }
    }

    @Nested
    @DisplayName("Admin Update Commissions")
    class AdminUpdateCommissions {
        @Nested
        @DisplayName("Positive Case")
        class AdminUpdateCommissionsPositiveCase {
            @Test
            @DisplayName("200 - success update commissions and send audit log")
            void updateCommissions_success() {
                UUID productId = UUID.randomUUID();
                UUID adminId = ColumnNameEntityUtility.USER_ID_ADMIN_VALUE;
                BigDecimal oldValue = new BigDecimal("10.00");
                BigDecimal newValue = new BigDecimal("15.50");

                CommissionEntityDTO commissionEntityDTO = new CommissionEntityDTO();
                commissionEntityDTO.setCommissionsEntityDTOProductId(productId);
                commissionEntityDTO.setCommissionsEntityDTOProductName("Open Bank BCA");
                commissionEntityDTO.setCommissionsEntityDTOValue(oldValue);
                commissionEntityDTO.setCommissionsEntityDTOCreatedDate(LocalDateTime.now().minusDays(1));
                commissionEntityDTO.setCommissionsEntityDTOUpdatedDate(LocalDateTime.now().minusHours(3));

                UserEntityDTO userEntityDTO = new UserEntityDTO();
                userEntityDTO.setUserEntityDTOId(adminId);
                userEntityDTO.setUserEntityDTOFullName("Admin One");
                userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
                userEntityDTO.setUserEntityDTORoleName("ADMIN");

                UserAdminUpdateCommissionsRequest userAdminUpdateCommissionsRequest = new UserAdminUpdateCommissionsRequest();
                userAdminUpdateCommissionsRequest.setCommissionsEntityDTOValue(newValue);

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.of(commissionEntityDTO));
                when(userRepositories.findByUserEntityDTOId(adminId))
                        .thenReturn(Optional.of(userEntityDTO));

                ArgumentCaptor<CommissionEntityDTO> entityCap = ArgumentCaptor.forClass(CommissionEntityDTO.class);
                when(commissionRepositories.save(entityCap.capture()))
                        .thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> restApiResponse = userService.updateCommissions(productId, userAdminUpdateCommissionsRequest);

                assertEquals(HttpStatus.OK.value(), restApiResponse.getRestAPIResponseCode());
                assertEquals("Commissions updated successfully", restApiResponse.getRestAPIResponseMessage());

                UserAdminUpdateCommissionsResponse userAdminUpdateCommissionsResponse =
                        (UserAdminUpdateCommissionsResponse) restApiResponse.getRestAPIResponseResults();
                assertEquals("ADMIN", userAdminUpdateCommissionsResponse.getUpdateCommissionsEntityDTORoleName());
                assertEquals("Admin One", userAdminUpdateCommissionsResponse.getUpdateCommissionsEntityDTOUserFullName());
                assertEquals("Open Bank BCA", userAdminUpdateCommissionsResponse.getUpdateCommissionsEntityDTOProductName());
                assertEquals(newValue, userAdminUpdateCommissionsResponse.getUpdateCommissionsEntityDTOValue());
                assertNotNull(userAdminUpdateCommissionsResponse.getUpdateCommissionsEntityDTOUpdatedDate());

                CommissionEntityDTO saved = entityCap.getValue();
                assertEquals(productId, saved.getCommissionsEntityDTOProductId());
                assertEquals(newValue, saved.getCommissionsEntityDTOValue());
                assertNotNull(saved.getCommissionsEntityDTOUpdatedDate());

                verify(auditLogProducerServiceImpl, times(1)).logUpdate(
                        eq(TableNameEntityUtility.TABLE_COMMISSION),
                        eq(productId),
                        anyMap(),
                        anyMap(),
                        eq(userEntityDTO.getUserEntityDTOId()),
                        eq("Admin One"),
                        eq(userEntityDTO.getUserEntityDTORoleId()),
                        eq("ADMIN"),
                        anyString(),
                        anyString()
                );
            }
        }

        @Nested
        @DisplayName("Negative Case")
        class AdminUpdateCommissionsNegativeCase {
            @Test
            @DisplayName("404 - product not found")
            void updateCommissions_productNotFound() {
                UUID productId = UUID.randomUUID();
                UserAdminUpdateCommissionsRequest userAdminUpdateCommissionsRequest = new UserAdminUpdateCommissionsRequest();
                userAdminUpdateCommissionsRequest.setCommissionsEntityDTOValue(new BigDecimal("5"));

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.empty());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.updateCommissions(productId, userAdminUpdateCommissionsRequest));

                assertTrue(ex.getMessage().contains("Product not found"));
                verify(userRepositories, never()).findByUserEntityDTOId(any());
                verify(commissionRepositories, never()).save(any());
                verify(auditLogProducerServiceImpl, never()).logUpdate(any(), any(), anyMap(), anyMap(),
                        any(), anyString(), any(), anyString(), anyString(), anyString());
            }

            @Test
            @DisplayName("404 - user ADMIN not found")
            void updateCommissions_adminNotFound() {
                UUID productId = UUID.randomUUID();
                BigDecimal newValue = new BigDecimal("7.5");

                CommissionEntityDTO commissionEntityDTO = new CommissionEntityDTO();
                commissionEntityDTO.setCommissionsEntityDTOProductId(productId);
                commissionEntityDTO.setCommissionsEntityDTOProductName("Open Bank Mandiri");
                commissionEntityDTO.setCommissionsEntityDTOValue(new BigDecimal("3"));
                commissionEntityDTO.setCommissionsEntityDTOCreatedDate(LocalDateTime.now().minusDays(2));
                commissionEntityDTO.setCommissionsEntityDTOUpdatedDate(LocalDateTime.now().minusDays(1));

                UserAdminUpdateCommissionsRequest userAdminUpdateCommissionsRequest = new UserAdminUpdateCommissionsRequest();
                userAdminUpdateCommissionsRequest.setCommissionsEntityDTOValue(newValue);

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.of(commissionEntityDTO));
                when(userRepositories.findByUserEntityDTOId(ColumnNameEntityUtility.USER_ID_ADMIN_VALUE))
                        .thenReturn(Optional.empty());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.updateCommissions(productId, userAdminUpdateCommissionsRequest));

                assertTrue(ex.getMessage().contains("User ADMIN not found"));
                verify(commissionRepositories, never()).save(any());
                verify(auditLogProducerServiceImpl, never()).logUpdate(any(), any(), anyMap(), anyMap(),
                        any(), anyString(), any(), anyString(), anyString(), anyString());
            }

            @Test
            @DisplayName("400 - commissions < 0 not allowed")
            void updateCommissions_negativeValue() {
                UUID productId = UUID.randomUUID();

                CommissionEntityDTO commissionEntityDTO = new CommissionEntityDTO();
                commissionEntityDTO.setCommissionsEntityDTOProductId(productId);
                commissionEntityDTO.setCommissionsEntityDTOProductName("Open Bank BNI");
                commissionEntityDTO.setCommissionsEntityDTOValue(new BigDecimal("2"));
                commissionEntityDTO.setCommissionsEntityDTOCreatedDate(LocalDateTime.now().minusDays(3));
                commissionEntityDTO.setCommissionsEntityDTOUpdatedDate(LocalDateTime.now().minusDays(1));

                UserAdminUpdateCommissionsRequest userAdminUpdateCommissionsRequest = new UserAdminUpdateCommissionsRequest();
                userAdminUpdateCommissionsRequest.setCommissionsEntityDTOValue(new BigDecimal("-0.01"));

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.of(commissionEntityDTO));

                UserEntityDTO userEntityDTO = new UserEntityDTO();
                userEntityDTO.setUserEntityDTOId(ColumnNameEntityUtility.USER_ID_ADMIN_VALUE);
                userEntityDTO.setUserEntityDTOFullName("Admin One");
                userEntityDTO.setUserEntityDTORoleId(UUID.randomUUID());
                userEntityDTO.setUserEntityDTORoleName("ADMIN");
                when(userRepositories.findByUserEntityDTOId(ColumnNameEntityUtility.USER_ID_ADMIN_VALUE))
                        .thenReturn(Optional.of(userEntityDTO));

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.updateCommissions(productId, userAdminUpdateCommissionsRequest));

                assertTrue(ex.getMessage().contains("Commissions value cannot be less than 0"));

                verify(commissionRepositories, never()).save(any());
                verify(auditLogProducerServiceImpl, never()).logUpdate(
                        any(), any(), anyMap(), anyMap(), any(), anyString(), any(), anyString(), anyString(), anyString()
                );
            }
        }
    }
}
