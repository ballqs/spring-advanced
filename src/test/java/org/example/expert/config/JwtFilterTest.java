package org.example.expert.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest httpRequest;

    @Spy
    private HttpServletResponse httpResponse;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    @Test
    @DisplayName("테스트 커버리지 100%를 위한 행위")
    // 개발자는 코드로 대화해야 한다고 들었습니다.
    // 김동현 튜터님께서 일단 이렇게 주석 및 DisplayName 로 달아두라고 하셨습니다.
    public void startsWith_검증() throws Exception {
        // given
        given(httpRequest.getRequestURI()).willReturn("/auth");

        // when
        jwtFilter.doFilter(httpRequest, httpResponse, filterChain);

        // then
        verify(filterChain, times(1)).doFilter(httpRequest, httpResponse);
    }

    @Test
    @DisplayName("테스트 커버리지 100%를 위한 행위")
    public void 토큰_존재_검증() throws Exception {
        // given
        given(httpRequest.getRequestURI()).willReturn("/users");
        given(httpRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);
        doNothing().when(httpResponse).sendError(anyInt(), anyString());

        // when
        jwtFilter.doFilter(httpRequest, httpResponse, filterChain);

        // then
        verify(httpResponse, times(1)).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("테스트 커버리지 100%를 위한 행위")
    public void Claims_검증() throws Exception {
        // given
        String token = "Bearer validToken";
        given(httpRequest.getRequestURI()).willReturn("/users");
        when(httpRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
        when(jwtUtil.substringToken(anyString())).thenReturn("validToken");
        when(jwtUtil.extractClaims(anyString())).thenReturn(null);
        doNothing().when(httpResponse).sendError(anyInt(), anyString());

        // when
        jwtFilter.doFilter(httpRequest, httpResponse, filterChain);

        // then
        verify(httpResponse, times(1)).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("테스트 커버리지 100%를 위한 행위")
    public void startsWith_admin_검증() throws Exception {
        // given
        String token = "Bearer validToken";
        when(httpRequest.getRequestURI()).thenReturn("/admin/resource");
        when(httpRequest.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.substringToken(anyString())).thenReturn("validToken");
        when(jwtUtil.extractClaims(anyString())).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("test@test.com");
        when(claims.get("userRole", String.class)).thenReturn("ADMIN");

        // when
        jwtFilter.doFilter(httpRequest, httpResponse, filterChain);

        // then
        verify(filterChain, times(1)).doFilter(httpRequest, httpResponse);
    }

    @Test
    @DisplayName("테스트 커버리지 100%를 위한 행위")
    public void startsWith_admin_검증_및_역할_검증() throws Exception {
        // given
        String token = "Bearer validToken";
        when(httpRequest.getRequestURI()).thenReturn("/admin/resource");
        when(httpRequest.getHeader("Authorization")).thenReturn(token);
        when(jwtUtil.substringToken(anyString())).thenReturn("validToken");
        when(jwtUtil.extractClaims(anyString())).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1");
        when(claims.get("email")).thenReturn("test@test.com");
        when(claims.get("userRole", String.class)).thenReturn("USER");
        doNothing().when(httpResponse).sendError(anyInt(), anyString());

        // when
        jwtFilter.doFilter(httpRequest, httpResponse, filterChain);


        // then
        verify(httpResponse, times(1)).sendError(anyInt(), anyString());
    }
}
