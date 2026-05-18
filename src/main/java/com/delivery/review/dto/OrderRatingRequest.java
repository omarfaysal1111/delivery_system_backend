package com.delivery.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRatingRequest {
    @Min(1) @Max(5)
    private int restaurantRating;

    @Min(1) @Max(5)
    private Integer driverRating;

    private String comment;
}
