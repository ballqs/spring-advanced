package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Spy
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    public void signup_이메일_없음() {
        // given
        SignupRequest signupRequest = new SignupRequest(null , "1234" , "ADMIN");

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class , () -> authService.signup(signupRequest));

        // then
        assertEquals("이메일 값이 없습니다." , exception.getMessage());
    }

    @Test
    public void signup_이메일_중복_검증() {
        // given
        SignupRequest signupRequest = new SignupRequest("test@test.com" , "1234" , "ADMIN");
        given(userRepository.existsByEmail(signupRequest.getEmail())).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class , () -> authService.signup(signupRequest));

        // then
        assertEquals("이미 존재하는 이메일입니다." , exception.getMessage());
    }

    @Test
    public void signup_get_token() {
        // given
        SignupRequest signupRequest = new SignupRequest("test@test.com" , "1234" , "ADMIN");

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        UserRole userRole = UserRole.of(signupRequest.getUserRole());

        User newUser = new User(
                signupRequest.getEmail(),
                encodedPassword,
                userRole
        );

        String token = jwtUtil.createToken(newUser.getId(), newUser.getEmail(), userRole);

        given(userRepository.save(any(User.class))).willReturn(newUser);
        when(jwtUtil.createToken(any(), anyString(), any())).thenReturn(token);

        // when
        SignupResponse signupResponse = authService.signup(signupRequest);

        // then
        assertEquals(token , signupResponse.getBearerToken());
    }

    @Test
    public void signin_가입_유저_검증() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@test.com" , "1234");
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class , () -> authService.signin(signinRequest));

        // then
        assertEquals("가입되지 않은 유저입니다." , exception.getMessage());
    }

    @Test
    public void signin_비밀번호_검증() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@test.com" , "1234");
        User user = new User(signinRequest.getEmail() , passwordEncoder.encode("2345") , UserRole.ADMIN);
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));

        // when
        AuthException exception = assertThrows(AuthException.class , () -> authService.signin(signinRequest));

        // then
        assertEquals("잘못된 비밀번호입니다." , exception.getMessage());
    }

    @Test
    public void signin_동작_완료() {
        // given
        SigninRequest signinRequest = new SigninRequest("test@test.com" , "1234");
        User user = new User(signinRequest.getEmail() , passwordEncoder.encode(signinRequest.getPassword()) , UserRole.ADMIN);
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));

        String token = jwtUtil.createToken(1L, user.getEmail(), UserRole.ADMIN);
        when(jwtUtil.createToken(any(), anyString(), any())).thenReturn(token);

        // when
        SigninResponse signinResponse = authService.signin(signinRequest);


        // then
        assertEquals(token , signinResponse.getBearerToken());
    }

}
