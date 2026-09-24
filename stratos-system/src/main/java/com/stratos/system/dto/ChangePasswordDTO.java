package com.stratos.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordDTO {

    @NotNull
    private Long adminId;

    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;
}
