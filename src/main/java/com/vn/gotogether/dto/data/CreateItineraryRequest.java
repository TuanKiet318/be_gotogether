package com.vn.gotogether.dto.data;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateItineraryRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    // Danh sách item trong lịch trình (có thể rỗng/null)
    private List<CreateItineraryItemRequest> items;

    // NEW: danh sách lời mời (tùy chọn)
    private List<InviteBody> invites;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class InviteBody {
        @NotBlank @Email
        private String inviteEmail;

        @NotBlank
        @Pattern(regexp = "VIEWER|EDITOR")
        private String role;
    }
}
