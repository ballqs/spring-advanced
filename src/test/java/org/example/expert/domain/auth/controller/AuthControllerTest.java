package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {AuthController.class}
)
public class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // 깡통 객체를 주입받는다. (있는 척 한다)
    private AuthService authService; // AuthService 모킹

    @SpyBean
    private JwtUtil jwtUtil; // JwtUtil 모킹

    @Test
    public void signup_동작_완료() throws Exception {
        // given
        SignupRequest requestDto = new SignupRequest("sollertia@sparta.com" , "robbie1234" , "ADMIN");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        String token = jwtUtil.createToken(1L , requestDto.getEmail() , UserRole.ADMIN);
        SignupResponse signupResponse = new SignupResponse(token);

        given(authService.signup(any())).willReturn(signupResponse);

        // when
        ResultActions resultActions = mvc.perform(post("/auth/signup")
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(authService , times(1)).signup(any());
    }

    @Test
    public void signup_이메일_검증() throws Exception {
        // given
        SignupRequest requestDto = new SignupRequest("sollertia" , "robbie1234" , "ADMIN");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post("/auth/signup")
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(authService , times(0)).signup(any());
    }

    @Test
    public void signup_비밀번호_검증() throws Exception {
        // given
        SignupRequest requestDto = new SignupRequest("sollertia" , "" , "ADMIN");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post("/auth/signup")
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(authService , times(0)).signup(any());
    }


    @Test
    public void signup_역할_검증() throws Exception {
        // given
        SignupRequest requestDto = new SignupRequest("sollertia" , "robbie1234" , "");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post("/auth/signup")
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(authService , times(0)).signup(any());
    }

    @Test
    public void signin_동작_완료() throws Exception {
        // given
        SigninRequest requestDto = new SigninRequest("sollertia@sparta.com" , "robbie1234");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        String token = jwtUtil.createToken(1L , requestDto.getEmail() , UserRole.ADMIN);
        SigninResponse signinResponse = new SigninResponse(token);

        given(authService.signin(any())).willReturn(signinResponse);

        // when
        ResultActions resultActions = mvc.perform(post("/auth/signin")
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(authService , times(1)).signin(any());
    }

    @Test
    public void signin_이메일_검증() throws Exception {
        // given
        SigninRequest requestDto = new SigninRequest("sollert" , "robbie1234");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post("/auth/signin")
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(authService , times(0)).signin(any());
    }

    @Test
    public void signin_비밀번호_검증() throws Exception {
        // given
        SigninRequest requestDto = new SigninRequest("sollert" , "");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        // when
        ResultActions resultActions = mvc.perform(post("/auth/signin")
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(authService , times(0)).signin(any());
    }
}
