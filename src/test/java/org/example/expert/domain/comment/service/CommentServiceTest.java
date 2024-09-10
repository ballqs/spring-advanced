package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

    @Test
    @DisplayName("saveComment_save_검증")
    public void saveComment_save_검증() {
        // given
        long todoId = 1;
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("내용");

        Todo todo = new Todo("제목" , "내용" , "날씨" , user);
        ReflectionTestUtils.setField(todo , "id" , todoId);

        Comment comment = new Comment(
                commentSaveRequest.getContents(),
                user,
                todo
        );
        ReflectionTestUtils.setField(comment , "id" , 1L);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentSaveResponse commentSaveResponse = commentService.saveComment(authUser , todoId , commentSaveRequest);

        // then
        assertNotNull(commentSaveResponse);
        assertEquals(comment.getId(), commentSaveResponse.getId());
        assertEquals(comment.getContents(), commentSaveResponse.getContents());
    }

    @Test
    @DisplayName("getComments_동작_완료")
    public void getComments_동작_완료() {
        // given
        AuthUser authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;

        Todo todo = new Todo("제목", "내용", "날씨", user);

        // User 및 Comment 객체 생성
        List<Comment> commentList = Arrays.asList(
                new Comment("내용1", user, todo),
                new Comment("내용2", user, todo)
        );

        // Repository의 findByTodoIdWithUser 메서드를 모킹
        given(commentRepository.findByTodoIdWithUser(todoId)).willReturn(commentList);

        // when
        List<CommentResponse> result = commentService.getComments(todoId);

        // then
        assertEquals(2, result.size());
        assertEquals("내용1", result.get(0).getContents());
        assertEquals("내용2", result.get(1).getContents());
        assertEquals("test@test.com", result.get(0).getUser().getEmail());
    }

}
