package com.resumatchpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ApplicationStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;

    private String notes;
}
