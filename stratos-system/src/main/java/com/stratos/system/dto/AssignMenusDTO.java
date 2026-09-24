package com.stratos.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AssignMenusDTO {

    @NotNull
    private Long roleId;

    private List<Long> menuIds;
}
