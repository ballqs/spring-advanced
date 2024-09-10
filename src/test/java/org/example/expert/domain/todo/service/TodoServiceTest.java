package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private TodoService todoService;
    @Mock
    private WeatherClient weatherClient;

    @Test
    public void saveTodo_동작_완료() {
        // given
        AuthUser authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("제목" , "내용");

        given(weatherClient.getTodayWeather()).willReturn("날씨");

        Todo savedTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                "날씨",
                user
        );
        // 영속성 컨텍스트는 id값으로 비교를 하는데 여기서 넣을때 newTodo에는 id가 정의되어 있지 않기 때문에 다르다고 판단해서 에러남
        given(todoRepository.save(any(Todo.class))).willReturn(savedTodo);

        // when
        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser , todoSaveRequest);

        // then
        assertEquals(savedTodo.getTitle() , todoSaveResponse.getTitle());
        assertEquals(savedTodo.getContents() , todoSaveResponse.getContents());
        assertEquals(savedTodo.getWeather() , todoSaveResponse.getWeather());
        assertEquals(savedTodo.getUser().getId() , todoSaveResponse.getUser().getId());
        assertEquals(savedTodo.getUser().getEmail() , todoSaveResponse.getUser().getEmail());
    }

    @Test
    public void getTodos_조회데이터_검증하기() {
        // given
        int page = 1;
        int size = 10;
        // ※pageable 생성
        Pageable pageable = PageRequest.of(page - 1 , size);
        AuthUser authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        User user = User.fromAuthUser(authUser);
        List<Todo> todoList = Arrays.asList(
                new Todo("제목1", "내용1", "맑음", user),
                new Todo("제목2", "내용2", "흐림", user)
        );
        // ※page 에 담기
        Page<Todo> todosPage = new PageImpl<>(todoList, pageable, todoList.size());
        given(todoRepository.findAllByOrderByModifiedAtDesc(any())).willReturn(todosPage);

        // when
        Page<TodoResponse> result = todoService.getTodos(page, size);

        // then
        assertEquals(2, result.getContent().size());
        assertEquals("제목1", result.getContent().get(0).getTitle());
        assertEquals("제목2", result.getContent().get(1).getTitle());
        assertEquals("test@test.com", result.getContent().get(0).getUser().getEmail());
    }

    @Test
    public void getTodo_Todo_없음() {
        // given
        long todoId = 1L;
        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoService.getTodo(todoId));

        // then
        assertEquals("Todo not found" , exception.getMessage());
    }

    @Test
    public void getTodo_조회데이터_검증하기() {
        // given
        long todoId = 1L;
        AuthUser authUser = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("제목" , "내용" , "날씨" , user);
        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when
        TodoResponse todoResponse = todoService.getTodo(todoId);

        // then
        assertEquals(todo.getTitle() , todoResponse.getTitle());
        assertEquals(todo.getContents() , todoResponse.getContents());
        assertEquals(todo.getWeather() , todoResponse.getWeather());
        assertEquals(todo.getUser().getId() , todoResponse.getUser().getId());
        assertEquals(todo.getUser().getEmail() , todoResponse.getUser().getEmail());
    }
}
