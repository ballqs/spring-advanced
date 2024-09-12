package org.example.expert.domain.comment.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.TestCommonData;
import org.example.expert.config.FilterConfig;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.service.CommentAdminService;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {CommentAdminController.class}
)
public class CommentAdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean // 깡통 객체를 주입받는다. (있는 척 한다)
    private CommentAdminService commentAdminService;

    @SpyBean
    private JwtUtil jwtUtil; // JwtUtil 모킹

    private String token;

    @BeforeEach
    public void setUp() throws Exception {
        TestCommonData.initToken(jwtUtil);
        this.token = TestCommonData.TOKEN;
    }

    @Test
    public void deleteComment_동작_완료() throws Exception {
        // given
        long commentId = 1L;
        doNothing().when(commentAdminService).deleteComment(anyLong());

        // when
        ResultActions resultActions = mvc.perform(delete("/admin/comments/{commentId}" , commentId)
                        .header(HttpHeaders.AUTHORIZATION , token)
                        .contentType(MediaType.APPLICATION_JSON)
                    );

        // then
        resultActions.andExpect(status().isOk()).andDo(print());

        verify(commentAdminService , times(1)).deleteComment(anyLong());
    }


}
