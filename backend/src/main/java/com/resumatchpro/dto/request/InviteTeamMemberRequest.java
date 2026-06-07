package com.resumatchpro.dto.request;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InviteTeamMemberRequest {
    @NotBlank @Email private String email;
    private String role = "RECRUITER_TEAM";
}
