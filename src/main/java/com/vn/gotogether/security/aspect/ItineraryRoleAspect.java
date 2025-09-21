package com.vn.gotogether.security.aspect;

import com.vn.gotogether.security.annotation.RequireItineraryRole;
import com.vn.gotogether.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class ItineraryRoleAspect {

//    private final CollaborationService collabService;
//    private final UserService userService;
//
//    @Around("@annotation(requireRole) && args(itineraryId,..)")
//    public Object checkRole(ProceedingJoinPoint pjp, RequireItineraryRole requireRole, String itineraryId) throws Throwable {
//        String userId = userService.getUserInformation().getId();
//
//        var collab = collabService.findCollaborator(userId, itineraryId)
//                .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền với lịch trình này"));
//
//        boolean allowed = Arrays.stream(requireRole.value())
//                .anyMatch(r -> r == collab.getRole());
//
//        if (!allowed) {
//            throw new AccessDeniedException("Không đủ quyền để thực hiện hành động này");
//        }
//
//        return pjp.proceed();
//    }
}
