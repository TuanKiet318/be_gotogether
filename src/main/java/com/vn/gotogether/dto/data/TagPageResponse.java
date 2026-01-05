package com.vn.gotogether.dto.data;

import com.vn.gotogether.dto.PageResponseAbstract;
import com.vn.gotogether.entity.Tag;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TagPageResponse extends PageResponseAbstract {

    private List<TagResponse> items;

    public TagPageResponse(Page<Tag> page) {
        this.items = page.getContent()
                .stream()
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .code(tag.getCode())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .createdAt(tag.getCreatedAt())
                        .build())
                .toList();

        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }
}
