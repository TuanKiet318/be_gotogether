package com.vn.gotogether.config;



import com.vn.gotogether.service.user.UserService;
import com.vn.gotogether.utils.SecurityUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ActivityFilter extends OncePerRequestFilter {

    private final UserService userService;

    public ActivityFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        SecurityUtil.getCurrentUserLogin().ifPresent(userService::updateLastActivity);

        filterChain.doFilter(request, response);
    }
}
