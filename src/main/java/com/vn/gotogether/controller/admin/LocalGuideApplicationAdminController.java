package com.vn.gotogether.controller.admin;

import com.vn.gotogether.dto.localguide.LocalGuideApplicationAdminResponse;
import com.vn.gotogether.dto.localguide.ReviewLocalGuideApplicationRequest;
import com.vn.gotogether.entity.User;
import com.vn.gotogether.enums.ApplicationStatus;
import com.vn.gotogether.exception.InvalidDataException;
import com.vn.gotogether.repository.user.UserRepository;
import com.vn.gotogether.service.data.LocalGuideApplicationAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/local-guide-applications")
@RequiredArgsConstructor
public class LocalGuideApplicationAdminController {

    private final LocalGuideApplicationAdminService service;
    private final UserRepository userRepository;

    // ================= GET LIST =================
    @GetMapping
    public Page<LocalGuideApplicationAdminResponse> getApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return service.getApplications(status, pageable);
    }

    // ================= APPROVE / REJECT =================
    @PutMapping("/{id}/review")
    public ResponseEntity<Void> reviewApplication(
            @PathVariable String id,
            @RequestBody @Valid ReviewLocalGuideApplicationRequest request
    ) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User admin = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidDataException("Người dùng không tồn tại."));
        service.reviewApplication(id, request, admin.getId());
        return ResponseEntity.ok().build();
    }
}
