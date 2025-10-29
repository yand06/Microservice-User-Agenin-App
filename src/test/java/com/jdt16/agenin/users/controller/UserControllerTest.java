package com.jdt16.agenin.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jdt16.agenin.users.controller.module.UserController;
import com.jdt16.agenin.users.dto.request.UserAdminUpdateCommissionsRequest;
import com.jdt16.agenin.users.dto.request.UserLoginRequest;
import com.jdt16.agenin.users.dto.request.UserRequest;
import com.jdt16.agenin.users.dto.response.*;
import com.jdt16.agenin.users.service.interfacing.module.UserService;
import com.jdt16.agenin.users.utility.RestApiPathUtility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final String HEADER_USER = "X-USER-ID";
    private static final String HEADER_REFERRAL_USER = "X-REFERENCE-USER-ID";
    private static final String HEADER_PRODUCT = "X-PRODUCT-ID";

    private static <T> RestApiResponse<T> okResponseTyped(T results) {
        RestApiResponse<T> body = new RestApiResponse<>();
        body.setRestAPIResponseCode(200);
        body.setRestAPIResponseMessage("OK");
        body.setRestAPIResponseResults(results);
        return body;
    }

    private static String api(String path) {
        return RestApiPathUtility.API_PATH
                + RestApiPathUtility.API_VERSION
                + RestApiPathUtility.API_PATH_USER
                + path;
    }

    @Nested
    @DisplayName("POST create user")
    class CreateUserTests {

        @Test
        @DisplayName("200: success")
        void create_success() throws Exception {
            UserRequest userRequest = new UserRequest();
            userRequest.setUserEntityDTOFullName("Bayu Wijaya");
            userRequest.setUserEntityDTOEmail("bayu12@example.com");
            userRequest.setUserEntityDTOPhoneNumber("081234567890");
            userRequest.setUserEntityDTOPassword("Secret#123");
            userRequest.setUserEntityDTOReferralCode("XX1234");

            UserResponse userResponse = mock(UserResponse.class);
            when(userService.saveUser(any(UserRequest.class)))
                    .thenReturn(okResponseTyped(userResponse));

            mvc.perform(post(api(RestApiPathUtility.API_PATH_CREATE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()))
                    .andExpect(jsonPath("$.message").value("OK"))
                    .andExpect(jsonPath("$.error").isEmpty());

            ArgumentCaptor<UserRequest> captor = ArgumentCaptor.forClass(UserRequest.class);
            verify(userService).saveUser(captor.capture());
        }

        @Test
        @DisplayName("400: body invalid → failed validation")
        void create_invalid_body() throws Exception {
            mvc.perform(post(api(RestApiPathUtility.API_PATH_CREATE))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("POST generate referral code")
    class GenerateReferralCodeTests {

        @Test
        @DisplayName("200: success")
        void generate_success() throws Exception {
            UUID userId = UUID.randomUUID();

            UserReferralCodeResponse userReferralCodeResponse = mock(UserReferralCodeResponse.class);
            when(userService.generateReferralCode(eq(userId)))
                    .thenReturn(okResponseTyped(userReferralCodeResponse));

            mvc.perform(post(api(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE))
                            .header(HEADER_USER, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            verify(userService).generateReferralCode(eq(userId));
        }

        @Test
        @DisplayName("500: missing X-USER-ID header (Global Advice)")
        void generate_missing_header() throws Exception {
            mvc.perform(post(api(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE)))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("POST login")
    class LoginTests {

        @Test
        @DisplayName("200: success")
        void login_success() throws Exception {
            UserLoginRequest userLoginRequest = new UserLoginRequest();
            userLoginRequest.setUserIdentifier("bayu12@example.com");
            userLoginRequest.setUserPassword("Secret#123");

            UserLoginResponse userLoginResponse = mock(UserLoginResponse.class);
            when(userService.login(any(UserLoginRequest.class)))
                    .thenReturn(okResponseTyped(userLoginResponse));

            mvc.perform(post(api(RestApiPathUtility.API_PATH_MODULE_LOGIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            ArgumentCaptor<UserLoginRequest> captor = ArgumentCaptor.forClass(UserLoginRequest.class);
            verify(userService).login(captor.capture());
        }

        @Test
        @DisplayName("400: body invalid → failed validation")
        void login_invalid() throws Exception {
            mvc.perform(post(api(RestApiPathUtility.API_PATH_MODULE_LOGIN))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("GET user profile")
    class GetProfileTests {

        @Test
        @DisplayName("200: success")
        void profile_success() throws Exception {
            UUID userId = UUID.randomUUID();

            UserProfileResponse userProfileResponse = mock(UserProfileResponse.class);
            when(userService.getUserProfile(eq(userId)))
                    .thenReturn(okResponseTyped(userProfileResponse));

            mvc.perform(get(api(RestApiPathUtility.API_PATH_MODULE_PROFILE))
                            .header(HEADER_USER, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            verify(userService).getUserProfile(eq(userId));
        }

        @Test
        @DisplayName("500: missing X-USER-ID header")
        void profile_missing_header() throws Exception {
            mvc.perform(get(api(RestApiPathUtility.API_PATH_MODULE_PROFILE)))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("GET user downline")
    class GetDownlineTests {

        @Test
        @DisplayName("200: success")
        void downline_success() throws Exception {
            UUID refUserId = UUID.randomUUID();

            List<UsersDownlineResponse> usersDownlineResponses = List.of(mock(UsersDownlineResponse.class));
            when(userService.getUserDownline(eq(refUserId)))
                    .thenReturn(okResponseTyped(usersDownlineResponses));

            mvc.perform(get(api(RestApiPathUtility.API_PATH_MODULE_DOWNLINE))
                            .header(HEADER_REFERRAL_USER, refUserId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.results").isArray());

            verify(userService).getUserDownline(eq(refUserId));
        }

        @Test
        @DisplayName("500: missing X-REFERENCE-USER-ID header")
        void downline_missing_header() throws Exception {
            mvc.perform(get(api(RestApiPathUtility.API_PATH_MODULE_DOWNLINE)))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("GET referral code")
    class GetReferralCodeTests {

        @Test
        @DisplayName("200: success")
        void get_referral_code_success() throws Exception {
            UUID userId = UUID.randomUUID();

            UserReferralCodeResponse userReferralCodeResponse = mock(UserReferralCodeResponse.class);
            when(userService.getReferralCode(eq(userId)))
                    .thenReturn(okResponseTyped(userReferralCodeResponse));

            mvc.perform(get(api(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE))
                            .header(HEADER_USER, userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            verify(userService).getReferralCode(eq(userId));
        }

        @Test
        @DisplayName("500: missing X-USER-ID header")
        void get_referral_code_missing_header() throws Exception {
            mvc.perform(get(api(RestApiPathUtility.API_PATH_MODULE_REFERRAL_CODE)))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(userService);
        }
    }

    @Nested
    @DisplayName("PATCH update commissions")
    class UpdateCommissionsTests {

        @Test
        @DisplayName("200: success")
        void update_commissions_success() throws Exception {
            UUID productId = UUID.randomUUID();

            UserAdminUpdateCommissionsRequest userAdminUpdateCommissionsRequest = new UserAdminUpdateCommissionsRequest();
            userAdminUpdateCommissionsRequest.setCommissionsEntityDTOValue(new BigDecimal("10.000"));

            UserAdminUpdateCommissionsRequest userAdminUpdateCommissionsRequest1 = mock(UserAdminUpdateCommissionsRequest.class);
            when(userService.updateCommissions(eq(productId), any(UserAdminUpdateCommissionsRequest.class)))
                    .thenReturn(okResponseTyped(userAdminUpdateCommissionsRequest1));

            mvc.perform(patch(api(RestApiPathUtility.API_PATH_MODULE_UPDATE_COMMISSIONS))
                            .header(HEADER_PRODUCT, productId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(userAdminUpdateCommissionsRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.results", notNullValue()));

            ArgumentCaptor<UserAdminUpdateCommissionsRequest> captor =
                    ArgumentCaptor.forClass(UserAdminUpdateCommissionsRequest.class);
            verify(userService).updateCommissions(eq(productId), captor.capture());
        }


        @Test
        @DisplayName("500: missing X-PRODUCT-ID header")
        void update_commissions_missing_header() throws Exception {
            mvc.perform(patch(api(RestApiPathUtility.API_PATH_MODULE_UPDATE_COMMISSIONS))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());

            verifyNoInteractions(userService);
        }

        @Test
        @DisplayName("400: body invalid → failed validation")
        void update_commissions_invalid_body() throws Exception {
            UUID productId = UUID.randomUUID();

            mvc.perform(patch(api(RestApiPathUtility.API_PATH_MODULE_UPDATE_COMMISSIONS))
                            .header(HEADER_PRODUCT, productId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
