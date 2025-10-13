package com.jdt16.agenin.users.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(min = 5, max = 100, message = "Minimal 5 karakter, maksimal 100")
    @JsonProperty("userFullName")
    private String userEntityDTOFullName;

    @Email(message = "Masukkan format email yang valid")
    @NotBlank(message = "Email tidak boleh kosong")
    @Size(min = 5, max = 100, message = "Minimal 5 karakter, maksimal 100")
    @JsonProperty("userEmail")
    private String userEntityDTOEmail;

    @NotBlank(message = "Nomor telepon tidak boleh kosong")
    @JsonProperty("userPhoneNumber")
    private String userEntityDTOPhoneNumber;

    @NotBlank(message = "Kata sandi tidak boleh kosong")
    @Size(min = 5, max = 100, message = "Minimal 5 dan maksimal 100 karakter")
    @JsonProperty("userPassword")
    private String userEntityDTOPassword;

    @JsonProperty("userReferralCode")
    private String userEntityDTOReferralCode;
}
