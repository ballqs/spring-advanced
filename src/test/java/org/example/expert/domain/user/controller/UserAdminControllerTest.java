package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {UserAdminController.class}
)
public class UserAdminControllerTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // 깡통 객체를 주입받는다. (있는 척 한다)
    private UserAdminService userAdminService;

    @SpyBean
    private JwtUtil jwtUtil; // JwtUtil 모킹

    @Mock
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Autowired
    private UserAdminController userAdminController;

    private String token;
    private AuthUser authUser;

    @BeforeEach
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(userAdminController)
                .setCustomArgumentResolvers(authUserArgumentResolver).build();

        this.authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        this.token = jwtUtil.createToken(1L , "test@test.com" , UserRole.ADMIN);
    }

    @Test
    public void changeUserRole_동작_완료() throws Exception {
        // given
        long userId = 1L;
        UserRoleChangeRequest requestDto = new UserRoleChangeRequest("ADMIN");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        doNothing().when(userAdminService).changeUserRole(anyLong(), any());

        // when
        ResultActions resultActions = mvc.perform(patch("/admin/users/{userId}" , userId)
                .header(HttpHeaders.AUTHORIZATION , token)
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(userAdminService , times(1)).changeUserRole(anyLong(), any());
    }
}
