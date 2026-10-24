package com.pgrental.backend.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Integer overallRating;
    private Integer foodRating;
    private Integer safetyRating;
    private Integer cleanlinessRating;
    private String comment;
}
