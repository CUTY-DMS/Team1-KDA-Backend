package com.example.cmd.global.security.error.exception;

import com.example.cmd.global.security.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class GlobalExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal (HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BusinessException e){

            ErrorCode errorCode = e.getErrorCode();

            response.setStatus(errorCode.getStatusCode());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ErrorResponse errorResponse = ErrorResponse.of(errorCode, errorCode.getMessage());
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }
}