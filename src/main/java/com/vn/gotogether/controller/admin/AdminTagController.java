package com.vn.gotogether.controller.admin;

import com.vn.gotogether.dto.data.*;
import com.vn.gotogether.service.data.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    // CREATE
    @PostMapping
    public TagResponse create(@Valid @RequestBody TagCreateRequest request) {
        return tagService.create(request);
    }

    // UPDATE
    @PutMapping("/{id}")
    public TagResponse update(
            @PathVariable String id,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        return tagService.update(id, request);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        tagService.delete(id);
    }

    // DETAIL
    @GetMapping("/{id}")
    public TagResponse getById(@PathVariable String id) {
        return tagService.getById(id);
    }

    // LIST + PAGINATION + SEARCH
    @GetMapping
    public TagPageResponse getAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return tagService.getAll(keyword, pageable);
    }
}
