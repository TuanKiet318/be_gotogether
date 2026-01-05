package com.vn.gotogether.service.data;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.entity.Tag;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.data.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepo;

    /* ================= CREATE ================= */
    @Transactional
    public TagResponse create(TagCreateRequest request) {

        if (tagRepo.existsByCode(request.getCode())) {
            throw new InvalidDataException("Tag code đã tồn tại");
        }

        Tag tag = Tag.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();

        tagRepo.save(tag);
        return toResponse(tag);
    }

    /* ================= UPDATE ================= */
    @Transactional
    public TagResponse update(String id, TagUpdateRequest request) {

        Tag tag = tagRepo.findById(id)
                .orElseThrow(() -> new InvalidDataException("Tag không tồn tại"));

        tag.setName(request.getName().trim());
        tag.setDescription(request.getDescription());

        return toResponse(tag);
    }

    /* ================= DELETE ================= */
    @Transactional
    public void delete(String id) {

        if (!tagRepo.existsById(id)) {
            throw new InvalidDataException("Tag không tồn tại");
        }

        tagRepo.deleteById(id);
    }

    /* ================= DETAIL ================= */
    @Transactional(readOnly = true)
    public TagResponse getById(String id) {

        Tag tag = tagRepo.findById(id)
                .orElseThrow(() -> new InvalidDataException("Tag không tồn tại"));

        return toResponse(tag);
    }

    /* ================= LIST + PAGINATION ================= */
    @Transactional(readOnly = true)
    public TagPageResponse getAll(String keyword, Pageable pageable) {

        Page<Tag> page;

        if (keyword == null || keyword.isBlank()) {
            page = tagRepo.findAll(pageable);
        } else {
            page = tagRepo
                    .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(
                            keyword,
                            keyword,
                            pageable
                    );
        }

        return new TagPageResponse(page);
    }

    /* ================= MAPPER ================= */
    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .code(tag.getCode())
                .name(tag.getName())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
