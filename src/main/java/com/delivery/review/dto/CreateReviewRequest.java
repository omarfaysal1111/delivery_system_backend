package com.delivery.review.dto;

import com.delivery.review.domain.TargetType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateReviewRequest {
    @NotNull private TargetType targetType;
    @NotNull private UUID targetId;
    @Min(1) @Max(5) private int rating;
    @Size(max = 2000) private String comment;
}
