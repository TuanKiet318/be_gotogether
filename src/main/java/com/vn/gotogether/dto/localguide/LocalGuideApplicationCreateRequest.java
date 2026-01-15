package com.vn.gotogether.dto.localguide;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class LocalGuideApplicationCreateRequest {

    @NotBlank
    private String destinationId;

    @NotBlank
    private String fullName;

    @Pattern(regexp = "^(0[0-9]{9})$")
    private String phone;

    @NotBlank
    private String nationalId;

    @Min(0)
    @Max(50)
    private Integer experienceYears;

    // Đổi từ String → Set<String>
    @NotEmpty
    private Set<String> languages;

    // Files
    @NotNull
    private MultipartFile frontImageFile;

    @NotNull
    private MultipartFile backImageFile;


    // Optional
    private MultipartFile portfolioFile;
    private MultipartFile certificateFile;

    @NotBlank
    private String localAddress;

    @NotBlank
    private String description;

    // 🔥 CAM KẾT PHÁP LÝ
    @AssertTrue
    private Boolean agreeToTerms;
}

