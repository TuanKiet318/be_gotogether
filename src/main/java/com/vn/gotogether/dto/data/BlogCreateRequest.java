package com.vn.gotogether.dto.data;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCreateRequest {

    // id địa điểm (Place) gắn với blog – có thể null nếu không bắt buộc
    private String placeId;

    @NotBlank
    @Size(min = 1, max = 5000)
    private String content;

    // danh sách media (ảnh/video) đi kèm
    private List<MediaCreateRequest> media;
}
