package com.example.QuoraApp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequestDTO {

    @NotBlank(message = "Target ID is required")
    private String targetID;

    @NotBlank(message = "Target Type is required")
    private String targetType;

    @NotBlank(message = "Is Like is required")
    private Boolean isLike;
}
