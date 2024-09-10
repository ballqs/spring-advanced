package org.example.expert.aop;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j(topic = "SpringAop")
@Aspect
@RequiredArgsConstructor
@Component
public class SpringAop {
    // 요청한 사용자의 ID
    // API 요청시간
    // API 요청 URL

    private final JwtUtil jwtUtil;

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
    private void checkDeleteComment() {}

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    private void checkChangeUserRole() {}

    @Around("checkDeleteComment() || checkChangeUserRole()")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime dt = LocalDateTime.now();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // request.getAttribute("userId")로도 처리 가능
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        Claims claims = jwtUtil.extractClaims(token);

        try {
            Object output = joinPoint.proceed();
            return output;
        } finally {
            log.info("요청한 사용자 ID : " + claims.getSubject() + ", API 요청시간 : " + dt + ", API 요청 URL : " + request.getRequestURI());
        }
    }
}
