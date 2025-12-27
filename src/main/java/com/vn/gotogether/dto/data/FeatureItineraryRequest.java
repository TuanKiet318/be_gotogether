package com.vn.gotogether.dto.data;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeatureItineraryRequest {

    @NotBlank
    private String overview;

    private Set<String> tagIds;

    // upload nhiều ảnh hero
    private List<MultipartFile> heroImageFiles;
}
