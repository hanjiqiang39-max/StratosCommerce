package com.stratos.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveCommentDTO {

    @NotNull
    private Long userId;

    @NotNull
    private Long spuId;

    @NotNull
    private Long skuId;

    @NotNull
    private Long orderId;

    private String nickname;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer starRating;

    @NotBlank
    private String content;

    private String images;

    private String specDesc;

    private Integer isAnonymous;
}
