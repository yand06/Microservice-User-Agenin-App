package com.jdt16.agenin.users.service;

import com.jdt16.agenin.users.components.generator.ReferralCodeGenerator;
import com.jdt16.agenin.users.components.handler.UserAuthJWT;
import com.jdt16.agenin.users.configuration.security.SecurityConfig;
import com.jdt16.agenin.users.dto.entity.*;
import com.jdt16.agenin.users.dto.exception.CoreThrowHandlerException;
import com.jdt16.agenin.users.dto.request.UserAdminUpdateCommissionsRequest;
import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.RestApiResponse;
import com.jdt16.agenin.users.dto.response.UserAdminUpdateCommissionsResponse;
import com.jdt16.agenin.users.dto.response.UserLoginResponse;
import com.jdt16.agenin.users.dto.response.UserReferralCodeResponse;
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
        UserRequest r = new UserRequest();
        r.setUserEntityDTOEmail(email);
        r.setUserEntityDTOPhoneNumber(phone);
        r.setUserEntityDTOFullName(fullName);
        r.setUserEntityDTOPassword(pwd);
        r.setUserEntityDTOReferralCode(referral);
        return r;
    }

    private static UserLoginRequest makeLogin(String identifier, String password) {
        UserLoginRequest r = new UserLoginRequest();
        r.setUserIdentifier(identifier);
        r.setUserPassword(password);
        return r;
    }

    private static UserEntityDTO makeUser(UUID id, String fullName, String email, String phone, String role, String hashedPwd) {
        UserEntityDTO u = new UserEntityDTO();
        u.setUserEntityDTOId(id);
        u.setUserEntityDTOFullName(fullName);
        u.setUserEntityDTOEmail(email);
        u.setUserEntityDTOPhoneNumber(phone);
        u.setUserEntityDTORoleName(role);
        u.setUserEntityDTOPassword(hashedPwd);
        return u;
    }

    private static UserRoleEntityDTO makeRole(String name) {
        UserRoleEntityDTO r = new UserRoleEntityDTO();
        r.setUserRoleEntityDTOId(UUID.randomUUID());
        r.setUserRoleEntityDTOName(name);
        return r;
    }

    private static UsersReferralEntityDTO makeDownline(UUID inviteeId, String name, String phone, String email, UUID refId) {
        UsersReferralEntityDTO d = new UsersReferralEntityDTO();
        d.setUsersReferralEntityDTOId(UUID.randomUUID());
        d.setUsersReferralEntityDTOInviteeUserId(inviteeId);
        d.setUsersReferralEntityDTOInviteeUserFullName(name);
        d.setUsersReferralEntityDTOInviteeUserPhoneNumber(phone);
        d.setUsersReferralEntityDTOInviteeUserEmail(email);
        d.setUsersReferralEntityDTOReferenceUserId(refId);
        d.setUsersReferralEntityDTOReferralCode("REF-XYZ");
        return d;
    }

    private static UserReferralCodeEntityDTO makeReferralEntity(UUID userId, String code, LocalDateTime createdAt) {
        UserReferralCodeEntityDTO e = new UserReferralCodeEntityDTO();
        e.setUserReferralEntityDTOId(UUID.randomUUID());
        e.setUserReferralEntityDTOUserId(userId);
        e.setUserReferralEntityDTOCode(code);
        e.setUserReferralEntityDTOCreatedAt(createdAt);
        return e;
    }

    private static CommissionEntityDTO makeCommission(UUID id, String productName, UUID productId, BigDecimal value, LocalDateTime created, LocalDateTime updated) {
        CommissionEntityDTO c = new CommissionEntityDTO();
        c.setCommissionsEntityDTOId(id);
        c.setCommissionsEntityDTOProductName(productName);
        c.setCommissionsEntityDTOProductId(productId);
        c.setCommissionsEntityDTOValue(value);
        c.setCommissionsEntityDTOCreatedDate(created);
        c.setCommissionsEntityDTOUpdatedDate(updated);
        return c;
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

                UserRequest req = makeUserRequest("new.user@mail.com", "08123456789", "New User", "Passw0rd!", null);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("new.user@mail.com"))
                        .thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08123456789"))
                        .thenReturn(Optional.empty());
                when(userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase("AGENT"))
                        .thenReturn(Optional.of(makeRole("AGENT")));
                when(passwordEncoder.encode(anyString())).thenReturn("hashed");

                ArgumentCaptor<UserEntityDTO> userCap = ArgumentCaptor.forClass(UserEntityDTO.class);
                when(userRepositories.save(userCap.capture())).thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> resp = userService.saveUser(req);

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("User successfully registered", resp.getRestAPIResponseMessage());
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
                UserRequest req = makeUserRequest("child@mail.com", "08111111111", "Child User", "S3cret!", code);

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

                RestApiResponse<Object> resp = userService.saveUser(req);

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("User successfully registered", resp.getRestAPIResponseMessage());

                verify(tUsersReferralRepositories, never()).save(any(UsersReferralEntityDTO.class));

                assertEquals("SUB_AGENT", userCap.getValue().getUserEntityDTORoleName());
            }

            @Test
            @DisplayName("200 - referral code valid tapi owner tidak ditemukan → user tersimpan SUB_AGENT, UsersReferral tidak disimpan")
            void register_referralOwnerMissing() {
                commonBoot();

                String code = "REF-OK";
                UserRequest req = makeUserRequest("user@mail.com", "08123", "User", "pw", code);

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

                RestApiResponse<Object> resp = userService.saveUser(req);

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("User successfully registered", resp.getRestAPIResponseMessage());
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

                UserRequest req = makeUserRequest("dup@mail.com", "08123", "Dup", "pw", null);
                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("dup@mail.com"))
                        .thenReturn(Optional.of(new UserEntityDTO()));

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class, () -> userService.saveUser(req));
                assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
                verify(userRepositories, never()).save(any());
            }

            @Test
            @DisplayName("409 - duplicate phone")
            void register_duplicatePhone() {
                commonBoot();

                UserRequest req = makeUserRequest("unique@mail.com", "08123456789", "DupPhone", "pw", null);
                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("unique@mail.com")).thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08123456789")).thenReturn(Optional.of(new UserEntityDTO()));

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class, () -> userService.saveUser(req));
                assertTrue(ex.getMessage().toLowerCase().contains("users already exist"));
                verify(userRepositories, never()).save(any());
            }

            @Test
            @DisplayName("400 - role AGENT not prepared in DB")
            void register_roleNotPrepared() {
                commonBoot();

                UserRequest req = makeUserRequest("user@mail.com", "08123", "User", "pw", null);
                when(userRepositories.findByUserEntityDTOEmailIgnoreCase("user@mail.com")).thenReturn(Optional.empty());
                when(userRepositories.findByUserEntityDTOPhoneNumber("08123")).thenReturn(Optional.empty());
                when(userRoleRepositories.findByUserRoleEntityDTONameIgnoreCase("AGENT")).thenReturn(Optional.empty());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class, () -> userService.saveUser(req));
                assertTrue(ex.getMessage().contains("Role 'AGENT'"));
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

                var realEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
                when(securityConfig.passwordEncoder()).thenReturn(realEncoder);
                String hashed = realEncoder.encode(raw);

                UUID id = UUID.randomUUID();
                UserEntityDTO user = makeUser(id, "User One", email, "0812xxxx", "AGENT", hashed);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase(email))
                        .thenReturn(Optional.of(user));

                when(userAuthJWT.generateAuthToken(any(UserEntityDTO.class), eq(3600)))
                        .thenReturn("jwt-token-123");

                RestApiResponse<Object> resp = userService.login(makeLogin(email, raw));

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("User login successful", resp.getRestAPIResponseMessage());
                UserLoginResponse body = (UserLoginResponse) resp.getRestAPIResponseResults();
                assertEquals("jwt-token-123", body.getUserLoginResponseToken());
                assertEquals(id, body.getUserEntityDTOId());
                assertEquals("User One", body.getUserEntityDTOFullName());
                assertEquals(email, body.getUserEntityDTOEmail());
                assertEquals("0812xxxx", body.getUserEntityDTOPhoneNumber());
                assertEquals("AGENT", body.getUserEntityDTORoleName());
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
                UserEntityDTO user = makeUser(id, "Phone User", "phone@mail.com", phone, "SUB_AGENT", hashed);

                when(userRepositories.findByUserEntityDTOPhoneNumber(phone))
                        .thenReturn(Optional.of(user));

                when(userAuthJWT.generateAuthToken(any(UserEntityDTO.class), eq(3600)))
                        .thenReturn("jwt-xyz");

                RestApiResponse<Object> resp = userService.login(makeLogin(phone, raw));

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("User login successful", resp.getRestAPIResponseMessage());
                UserLoginResponse body = (UserLoginResponse) resp.getRestAPIResponseResults();
                assertEquals("jwt-xyz", body.getUserLoginResponseToken());
                assertEquals(id, body.getUserEntityDTOId());
                assertEquals("Phone User", body.getUserEntityDTOFullName());
                assertEquals("phone@mail.com", body.getUserEntityDTOEmail());
                assertEquals(phone, body.getUserEntityDTOPhoneNumber());
                assertEquals("SUB_AGENT", body.getUserEntityDTORoleName());
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

                UserEntityDTO user = makeUser(id, "User", email, "0812", "AGENT", hashed);

                when(userRepositories.findByUserEntityDTOEmailIgnoreCase(email)).thenReturn(Optional.of(user));
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

                UserEntityDTO user = new UserEntityDTO();
                user.setUserEntityDTOId(userId);
                user.setUserEntityDTOFullName("Sub Agent One");
                user.setUserEntityDTORoleId(UUID.randomUUID());
                user.setUserEntityDTORoleName(ROLE_SUB_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(user));
                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)).thenReturn(false);

                ArgumentCaptor<UserReferralCodeEntityDTO> refCap = ArgumentCaptor.forClass(UserReferralCodeEntityDTO.class);
                when(tUserReferralCodeRepositories.save(refCap.capture()))
                        .thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> resp = userService.generateReferralCode(userId);

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("Referral referralCode generated successfully", resp.getRestAPIResponseMessage());
                UserReferralCodeResponse body = (UserReferralCodeResponse) resp.getRestAPIResponseResults();
                assertNotNull(body.getUserReferralEntityDTOId());
                assertNotNull(body.getUserReferralEntityDTOCreatedAt());

                UserReferralCodeEntityDTO saved = refCap.getValue();
                String actualCode = saved.getUserReferralEntityDTOCode();

                assertEquals(actualCode, body.getUserReferralEntityDTOCode());
                assertNotNull(actualCode);
                assertFalse(actualCode.isBlank());
                assertTrue(actualCode.matches("[A-Z0-9]{6,12}"));

                assertEquals(ROLE_AGENT, user.getUserEntityDTORoleName());
                verify(userRepositories).save(user);
                verify(auditLogProducerServiceImpl, times(1)).logUpdate(
                        eq(TableNameEntityUtility.TABLE_USERS),
                        eq(userId), anyMap(), anyMap(),
                        eq(userId), eq("Sub Agent One"),
                        eq(user.getUserEntityDTORoleId()), eq(ROLE_AGENT),
                        anyString(), anyString()
                );
                verify(auditLogProducerServiceImpl, times(1)).logCreate(
                        eq(TableNameEntityUtility.TABLE_USER_REFERRAL_CODE),
                        eq(saved.getUserReferralEntityDTOId()), anyMap(),
                        eq(userId), eq("Sub Agent One"),
                        eq(user.getUserEntityDTORoleId()), eq(ROLE_AGENT),
                        anyString(), anyString()
                );
            }


            @Test
            @DisplayName("200 - success generate without upgrade role (AGENT)")
            void generateReferralCode_success_withoutRoleUpgrade() {
                UUID userId = UUID.randomUUID();

                UserEntityDTO user = new UserEntityDTO();
                user.setUserEntityDTOId(userId);
                user.setUserEntityDTOFullName("Agent Two");
                user.setUserEntityDTORoleId(UUID.randomUUID());
                user.setUserEntityDTORoleName(ROLE_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(user));
                when(tUserReferralCodeRepositories.existsByUserReferralEntityDTOUserId(userId)).thenReturn(false);

                ArgumentCaptor<UserReferralCodeEntityDTO> refCap = ArgumentCaptor.forClass(UserReferralCodeEntityDTO.class);
                when(tUserReferralCodeRepositories.save(refCap.capture()))
                        .thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> resp = userService.generateReferralCode(userId);

                UserReferralCodeEntityDTO saved = refCap.getValue();
                String actualCode = saved.getUserReferralEntityDTOCode();

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("Referral referralCode generated successfully", resp.getRestAPIResponseMessage());
                UserReferralCodeResponse body = (UserReferralCodeResponse) resp.getRestAPIResponseResults();
                assertNotNull(body.getUserReferralEntityDTOId());
                assertNotNull(body.getUserReferralEntityDTOCreatedAt());

                assertEquals(actualCode, body.getUserReferralEntityDTOCode());
                assertNotNull(actualCode);
                assertFalse(actualCode.isBlank());
                assertTrue(actualCode.matches("[A-Z0-9]{6,12}"));

                assertEquals(ROLE_AGENT, user.getUserEntityDTORoleName());
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
                        eq(user.getUserEntityDTORoleId()),
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

                UserEntityDTO user = new UserEntityDTO();
                user.setUserEntityDTOId(userId);
                user.setUserEntityDTOFullName("Has Code");
                user.setUserEntityDTORoleId(UUID.randomUUID());
                user.setUserEntityDTORoleName(ROLE_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(user));
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
                UserEntityDTO user = new UserEntityDTO();
                user.setUserEntityDTOId(userId);
                user.setUserEntityDTOFullName("No Code");
                user.setUserEntityDTORoleId(UUID.randomUUID());
                user.setUserEntityDTORoleName(ROLE_AGENT);

                when(userRepositories.findById(userId)).thenReturn(Optional.of(user));
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
                UserReferralCodeEntityDTO entity = makeReferralEntity(userId, "REF-XYZ123", LocalDateTime.now());
                when(tUserReferralCodeRepositories.findByUserReferralEntityDTOUserId(userId))
                        .thenReturn(Optional.of(entity));

                RestApiResponse<Object> resp = userService.getReferralCode(userId);
                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("Referral code retrieved successfully", resp.getRestAPIResponseMessage());

                UserReferralCodeResponse body = (UserReferralCodeResponse) resp.getRestAPIResponseResults();
                assertEquals(entity.getUserReferralEntityDTOId(), body.getUserReferralEntityDTOId());
                assertEquals("REF-XYZ123", body.getUserReferralEntityDTOCode());
                assertNotNull(body.getUserReferralEntityDTOCreatedAt());
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
                UserEntityDTO user = makeUser(userId, "Jane Doe", "jane@mail.com", "081234567890", "AGENT", null);
                when(userRepositories.findById(userId)).thenReturn(Optional.of(user));

                RestApiResponse<Object> resp = userService.getUserProfile(userId);
                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("User profile retrieved successfully", resp.getRestAPIResponseMessage());

                com.jdt16.agenin.users.dto.response.UserProfileResponse body =
                        (com.jdt16.agenin.users.dto.response.UserProfileResponse) resp.getRestAPIResponseResults();

                assertEquals(userId, body.getUserEntityDTOId());
                assertEquals("Jane Doe", body.getUserEntityDTOFullName());
                assertEquals("jane@mail.com", body.getUserEntityDTOEmail());
                assertEquals("081234567890", body.getUserEntityDTOPhoneNumber());
                assertEquals("AGENT", body.getUserEntityDTORoleName());
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
                UUID c1 = UUID.randomUUID();
                UUID c2 = UUID.randomUUID();

                UsersReferralEntityDTO d1 = makeDownline(c1, "Child One", "0812-111", "c1@mail.com", refUserId);
                UsersReferralEntityDTO d2 = makeDownline(c2, "Child Two", "0812-222", "c2@mail.com", refUserId);

                when(tUsersReferralRepositories.findAllByUsersReferralEntityDTOReferenceUserId(refUserId))
                        .thenReturn(List.of(d1, d2));

                when(userBalanceRepositories.findBalanceAmountByUserId(c1))
                        .thenReturn(Optional.of(new BigDecimal("150000")));
                when(userBalanceRepositories.findBalanceAmountByUserId(c2))
                        .thenReturn(Optional.empty());

                RestApiResponse<Object> resp = userService.getUserDownline(refUserId);

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("Downline retrieved successfully", resp.getRestAPIResponseMessage());
                assertTrue(resp.getRestAPIResponseResults() instanceof List<?>);

                @SuppressWarnings("unchecked")
                List<com.jdt16.agenin.users.dto.response.UsersDownlineResponse> body =
                        (List<com.jdt16.agenin.users.dto.response.UsersDownlineResponse>) resp.getRestAPIResponseResults();

                assertEquals(2, body.size());
                assertEquals(new BigDecimal("150000"), body.get(0).getUsersReferralEntityDTOInviteeCommissionValue());
                assertEquals(BigDecimal.ZERO, body.get(1).getUsersReferralEntityDTOInviteeCommissionValue());
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

                CommissionEntityDTO entity = new CommissionEntityDTO();
                entity.setCommissionsEntityDTOProductId(productId);
                entity.setCommissionsEntityDTOProductName("Open Bank BCA");
                entity.setCommissionsEntityDTOValue(oldValue);
                entity.setCommissionsEntityDTOCreatedDate(LocalDateTime.now().minusDays(1));
                entity.setCommissionsEntityDTOUpdatedDate(LocalDateTime.now().minusHours(3));

                UserEntityDTO admin = new UserEntityDTO();
                admin.setUserEntityDTOId(adminId);
                admin.setUserEntityDTOFullName("Admin One");
                admin.setUserEntityDTORoleId(UUID.randomUUID());
                admin.setUserEntityDTORoleName("ADMIN");

                UserAdminUpdateCommissionsRequest req = new UserAdminUpdateCommissionsRequest();
                req.setCommissionsEntityDTOValue(newValue);

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.of(entity));
                when(userRepositories.findByUserEntityDTOId(adminId))
                        .thenReturn(Optional.of(admin));

                ArgumentCaptor<CommissionEntityDTO> entityCap = ArgumentCaptor.forClass(CommissionEntityDTO.class);
                when(commissionRepositories.save(entityCap.capture()))
                        .thenAnswer(inv -> inv.getArgument(0));

                RestApiResponse<Object> resp = userService.updateCommissions(productId, req);

                assertEquals(HttpStatus.OK.value(), resp.getRestAPIResponseCode());
                assertEquals("Commissions updated successfully", resp.getRestAPIResponseMessage());
                UserAdminUpdateCommissionsResponse body =
                        (UserAdminUpdateCommissionsResponse) resp.getRestAPIResponseResults();
                assertEquals("ADMIN", body.getUpdateCommissionsEntityDTORoleName());
                assertEquals("Admin One", body.getUpdateCommissionsEntityDTOUserFullName());
                assertEquals("Open Bank BCA", body.getUpdateCommissionsEntityDTOProductName());
                assertEquals(newValue, body.getUpdateCommissionsEntityDTOValue());
                assertNotNull(body.getUpdateCommissionsEntityDTOUpdatedDate());

                CommissionEntityDTO saved = entityCap.getValue();
                assertEquals(productId, saved.getCommissionsEntityDTOProductId());
                assertEquals(newValue, saved.getCommissionsEntityDTOValue());
                assertNotNull(saved.getCommissionsEntityDTOUpdatedDate());

                verify(auditLogProducerServiceImpl, times(1)).logUpdate(
                        eq(TableNameEntityUtility.TABLE_COMMISSION),
                        eq(productId),
                        anyMap(),
                        anyMap(),
                        eq(admin.getUserEntityDTOId()),
                        eq("Admin One"),
                        eq(admin.getUserEntityDTORoleId()),
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
                UserAdminUpdateCommissionsRequest req = new UserAdminUpdateCommissionsRequest();
                req.setCommissionsEntityDTOValue(new BigDecimal("5"));

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.empty());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.updateCommissions(productId, req));

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

                CommissionEntityDTO entity = new CommissionEntityDTO();
                entity.setCommissionsEntityDTOProductId(productId);
                entity.setCommissionsEntityDTOProductName("Open Bank Mandiri");
                entity.setCommissionsEntityDTOValue(new BigDecimal("3"));
                entity.setCommissionsEntityDTOCreatedDate(LocalDateTime.now().minusDays(2));
                entity.setCommissionsEntityDTOUpdatedDate(LocalDateTime.now().minusDays(1));

                UserAdminUpdateCommissionsRequest req = new UserAdminUpdateCommissionsRequest();
                req.setCommissionsEntityDTOValue(newValue);

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.of(entity));
                when(userRepositories.findByUserEntityDTOId(ColumnNameEntityUtility.USER_ID_ADMIN_VALUE))
                        .thenReturn(Optional.empty());

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.updateCommissions(productId, req));

                assertTrue(ex.getMessage().contains("User ADMIN not found"));
                verify(commissionRepositories, never()).save(any());
                verify(auditLogProducerServiceImpl, never()).logUpdate(any(), any(), anyMap(), anyMap(),
                        any(), anyString(), any(), anyString(), anyString(), anyString());
            }

            @Test
            @DisplayName("400 - commissions < 0 not allowed")
            void updateCommissions_negativeValue() {
                UUID productId = UUID.randomUUID();

                CommissionEntityDTO entity = new CommissionEntityDTO();
                entity.setCommissionsEntityDTOProductId(productId);
                entity.setCommissionsEntityDTOProductName("Open Bank BNI");
                entity.setCommissionsEntityDTOValue(new BigDecimal("2"));
                entity.setCommissionsEntityDTOCreatedDate(LocalDateTime.now().minusDays(3));
                entity.setCommissionsEntityDTOUpdatedDate(LocalDateTime.now().minusDays(1));

                UserAdminUpdateCommissionsRequest req = new UserAdminUpdateCommissionsRequest();
                req.setCommissionsEntityDTOValue(new BigDecimal("-0.01"));

                when(commissionRepositories.findByCommissionsEntityDTOProductId(productId))
                        .thenReturn(Optional.of(entity));

                UserEntityDTO admin = new UserEntityDTO();
                admin.setUserEntityDTOId(ColumnNameEntityUtility.USER_ID_ADMIN_VALUE);
                admin.setUserEntityDTOFullName("Admin One");
                admin.setUserEntityDTORoleId(UUID.randomUUID());
                admin.setUserEntityDTORoleName("ADMIN");
                when(userRepositories.findByUserEntityDTOId(ColumnNameEntityUtility.USER_ID_ADMIN_VALUE))
                        .thenReturn(Optional.of(admin));

                CoreThrowHandlerException ex = assertThrows(CoreThrowHandlerException.class,
                        () -> userService.updateCommissions(productId, req));

                assertTrue(ex.getMessage().contains("Commissions value cannot be less than 0"));

                verify(commissionRepositories, never()).save(any());
                verify(auditLogProducerServiceImpl, never()).logUpdate(
                        any(), any(), anyMap(), anyMap(), any(), anyString(), any(), anyString(), anyString(), anyString()
                );
            }
        }
    }
}
