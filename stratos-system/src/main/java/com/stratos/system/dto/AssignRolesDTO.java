package com.stratos.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignRolesDTO {

    @NotNull
    private Long adminId;

    private List<Long> roleIds;
}
