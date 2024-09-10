package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {ManagerController.class}
)
public class ManagerControllerTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // 깡통 객체를 주입받는다. (있는 척 한다)
    private ManagerService managerService;

    @SpyBean
    private JwtUtil jwtUtil; // JwtUtil 모킹

    @Mock
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Autowired
    private ManagerController managerController;

    private String token;
    private AuthUser authUser;

    @BeforeEach
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(managerController)
                .setCustomArgumentResolvers(authUserArgumentResolver).build();

        this.authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        this.token = jwtUtil.createToken(1L , "test@test.com" , UserRole.ADMIN);
    }

    @Test
    public void saveManager_동작_완료() throws Exception {
        // given
        long todoId = 1L;
        ManagerSaveRequest requestDto = new ManagerSaveRequest(1L);

        String postInfo = objectMapper.writeValueAsString(requestDto);

        User user = User.fromAuthUser(authUser);
        ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(1L , new UserResponse(user.getId(), user.getEmail()));

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        given(managerService.saveManager(any() , anyLong() , any())).willReturn(managerSaveResponse);

        // when
        ResultActions resultActions = mvc.perform(post("/todos/{todoId}/managers" , todoId)
                .header(HttpHeaders.AUTHORIZATION , token)
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(managerService , times(1)).saveManager(any() , anyLong() , any());
    }

    @Test
    public void saveManager_동작_실패() throws Exception {
        // given
        long todoId = 1L;
        ManagerSaveRequest requestDto = new ManagerSaveRequest();

        String postInfo = objectMapper.writeValueAsString(requestDto);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);

        // when
        ResultActions resultActions = mvc.perform(post("/todos/{todoId}/managers" , todoId)
                .header(HttpHeaders.AUTHORIZATION , token)
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(managerService , times(0)).saveManager(any() , anyLong() , any());
    }

    @Test
    public void getMembers_동작_완료() throws Exception {
        // given
        long todoId = 1L;

        User user = User.fromAuthUser(authUser);
        List<ManagerResponse> dtoList = List.of(
                new ManagerResponse(1L , new UserResponse(user.getId(), user.getEmail())),
                new ManagerResponse(2L , new UserResponse(user.getId(), user.getEmail()))
        );

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        given(managerService.getManagers(anyLong())).willReturn(dtoList);

        // when
        ResultActions resultActions = mvc.perform(get("/todos/{todoId}/managers" , todoId)
                .header(HttpHeaders.AUTHORIZATION , token)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(managerService , times(1)).getManagers(anyLong());
    }

    @Test
    public void deleteManager_동작_완료() throws Exception {
        // given
        long todoId = 1L;
        long managerId = 1L;

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        doNothing().when(managerService).deleteManager(any() , anyLong() , anyLong());

        // when
        ResultActions resultActions = mvc.perform(delete("/todos/{todoId}/managers/{managerId}" , todoId , managerId)
                .header(HttpHeaders.AUTHORIZATION , token)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(managerService , times(1)).deleteManager(any() , anyLong() , anyLong());
    }

}
