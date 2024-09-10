package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {TodoController.class}
)
public class TodoControllerTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // 깡통 객체를 주입받는다. (있는 척 한다)
    private TodoService todoService;

    @SpyBean
    private JwtUtil jwtUtil; // JwtUtil 모킹

    @Mock
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Autowired
    private TodoController todoController;

    private String token;
    private AuthUser authUser;

    @BeforeEach
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(todoController)
                .setCustomArgumentResolvers(authUserArgumentResolver).build();

        this.authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        this.token = jwtUtil.createToken(1L , "test@test.com" , UserRole.ADMIN);
    }

    @Test
    public void saveTodo_동작_완료() throws Exception {
        // given
        TodoSaveRequest requestDto = new TodoSaveRequest("제목" , "내용");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        User user = User.fromAuthUser(authUser);
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(1L , "제목" , "내용" , "날씨" , new UserResponse(user.getId(), user.getEmail()));

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        given(todoService.saveTodo(any() , any())).willReturn(todoSaveResponse);

        // when
        ResultActions resultActions = mvc.perform(post("/todos")
                .header(HttpHeaders.AUTHORIZATION , token)
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(todoService , times(1)).saveTodo(any() , any());
    }

    @Test
    public void saveTodo_제목_검증() throws Exception {
        // given
        TodoSaveRequest requestDto = new TodoSaveRequest("" , "내용");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);

        // when
        ResultActions resultActions = mvc.perform(post("/todos")
                .header(HttpHeaders.AUTHORIZATION , token)
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(todoService , times(0)).saveTodo(any() , any());
    }
    
    @Test
    public void saveTodo_내용_검증() throws Exception {
        // given
        TodoSaveRequest requestDto = new TodoSaveRequest("제목" , "");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);

        // when
        ResultActions resultActions = mvc.perform(post("/todos")
                .header(HttpHeaders.AUTHORIZATION , token)
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(todoService , times(0)).saveTodo(any() , any());
    }

    @Test
    public void getTodos_동작_완료() throws Exception {
        // given
        int page = 1;
        int size = 10;

        Pageable pageable = PageRequest.of(page - 1 , size);
        User user = User.fromAuthUser(authUser);
        List<TodoResponse> todoList = Arrays.asList(
                new TodoResponse(1L , "제목1", "내용1", "맑음", new UserResponse(user.getId() , user.getEmail()) , null , null),
                new TodoResponse(2L , "제목2", "내용2", "흐림", new UserResponse(user.getId() , user.getEmail()) , null , null)
        );
        // ※page 에 담기
        Page<TodoResponse> todoResponsesPage = new PageImpl<>(todoList, pageable, todoList.size());

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        given(todoService.getTodos(anyInt() , anyInt())).willReturn(todoResponsesPage);

        // when
        ResultActions resultActions = mvc.perform(get("/todos")
                .header(HttpHeaders.AUTHORIZATION , token)
                .param("page" , String.valueOf(page))
                .param("size" , String.valueOf(size))
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(todoService , times(1)).getTodos(anyInt() , anyInt());
    }

    @Test
    public void getTodo_동작_완료() throws Exception {
        // given
        long todoId = 1L;

        User user = User.fromAuthUser(authUser);
        TodoResponse todoResponse = new TodoResponse(1L , "제목" , "내용" , "날씨" , new UserResponse(user.getId(), user.getEmail()) , null , null);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        given(todoService.getTodo(anyLong())).willReturn(todoResponse);

        // when
        ResultActions resultActions = mvc.perform(get("/todos/{todoId}" , todoId)
                .header(HttpHeaders.AUTHORIZATION , token)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(todoService , times(1)).getTodo(anyLong());
    }
}
