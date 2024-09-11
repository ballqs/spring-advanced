package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {CommentController.class}
)
public class CommentControllerTest {

    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // 깡통 객체를 주입받는다. (있는 척 한다)
    private CommentService commentService;

    @SpyBean
    private JwtUtil jwtUtil; // JwtUtil 모킹

    @Mock
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Autowired
    private CommentController commentController;

    private String token;
    private AuthUser authUser;

    @BeforeEach
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(commentController)
                .setCustomArgumentResolvers(authUserArgumentResolver).build();

        this.authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        this.token = jwtUtil.createToken(1L , "test@test.com" , UserRole.ADMIN);
    }

    @Test
    public void saveComment_동작_완료() throws Exception {
        // given
        long todoId = 1L;
        CommentSaveRequest requestDto = new CommentSaveRequest("내용");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        User user = User.fromAuthUser(authUser);
        CommentSaveResponse commentSaveResponse = new CommentSaveResponse(1L , "내용" , new UserResponse(user.getId(), user.getEmail()));

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        given(commentService.saveComment(any() , anyLong() , any())).willReturn(commentSaveResponse);

        // when
        ResultActions resultActions = mvc.perform(post("/todos/{todoId}/comments" , todoId)
                        .header(HttpHeaders.AUTHORIZATION , token)
                        .content(postInfo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(commentService , times(1)).saveComment(any() , anyLong() , any());
    }

    @Test
    public void saveComment_동작_실패() throws Exception {
        // given
        long todoId = 1L;
        CommentSaveRequest requestDto = new CommentSaveRequest("");

        String postInfo = objectMapper.writeValueAsString(requestDto);

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);

        // when
        ResultActions resultActions = mvc.perform(post("/todos/{todoId}/comments" , todoId)
                .header(HttpHeaders.AUTHORIZATION , token)
                .content(postInfo)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(print());

        verify(commentService , times(0)).saveComment(any() , anyLong() , any());
    }

    @Test
    public void getComments_동작_완료() throws Exception {
        // given
        long todoId = 1L;

        User user = User.fromAuthUser(authUser);
        List<CommentResponse> dtoList = List.of(
                new CommentResponse(1L , "내용1" , new UserResponse(user.getId(), user.getEmail())),
                new CommentResponse(2L , "내용2" , new UserResponse(user.getId(), user.getEmail()))
        );

        given(authUserArgumentResolver.supportsParameter(any())).willReturn(true);
        given(authUserArgumentResolver.resolveArgument(any() , any() , any() , any())).willReturn(authUser);
        given(commentService.getComments(anyLong())).willReturn(dtoList);

        // when
        ResultActions resultActions = mvc.perform(get("/todos/{todoId}/comments" , todoId)
                .header(HttpHeaders.AUTHORIZATION , token)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(commentService , times(1)).getComments(anyLong());
    }
}
