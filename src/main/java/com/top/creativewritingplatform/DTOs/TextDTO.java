package com.top.creativewritingplatform.DTOs;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 1000, max = 20000, message = "Content must be between 1000 and 20000 characters")
    private String content;

    @NotEmpty(message = "At least one topic is required")
    private Set<String> topics;

    private String imageUrl;
}