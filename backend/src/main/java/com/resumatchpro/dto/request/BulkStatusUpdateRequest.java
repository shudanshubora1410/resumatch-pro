package com.resumatchpro.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BulkStatusUpdateRequest {
    @NotBlank private String status;
    private List<Long> applicationIds;
}
