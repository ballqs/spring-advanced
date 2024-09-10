package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    // mock 를 하게 되면은 PasswordEncoder 그 자체를 가져와서 했기에 내부 동작의 정의는 보장하지 않는다.
    // 내부적으로 BCrypt가 주입이 안되고 있다. Spy를 사용하면 내부적으로 존재하는 BCrypt가 주입이 되서 사용이 된다.
    @Spy
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("getUser_아이디_못찾음")
    public void getUser_아이디_못찾음() {
        // given
        long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));

        // then
        assertEquals("User not found" , exception.getMessage());
    }

    @Test
    @DisplayName("getUser_정보_가져오기")
    public void getUser_정보_가져오기() {
        // given
        long userId = 1L;
        User user = User.fromAuthUser(new AuthUser(1L , "test@test.com" , UserRole.ADMIN));
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse userResponse = userService.getUser(userId);

        // then
        assertEquals(userId , userResponse.getId());
        assertEquals("test@test.com" , userResponse.getEmail());
    }

    @Test
    @DisplayName("changePassword_비밀번호_정규식_검증")
    public void changePassword_비밀번호_정규식_검증() {
        // given
        long userId = 1L;
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("1234" , "2345");

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, userChangePasswordRequest));

        // then
        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다." , exception.getMessage());
    }

    @Test
    @DisplayName("changePassword_아이디_못찾음")
    public void changePassword_아이디_못찾음() {
        // given
        long userId = 1L;
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("Aasd1234" , "Aasd2345");
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId , userChangePasswordRequest));

        // then
        assertEquals("User not found" , exception.getMessage());
    }

    @Test
    @DisplayName("changePassword_비밀번호_변경_검증")
    public void changePassword_비밀번호_변경_검증() {
        // given
        long userId = 1L;
        User user = new User("test@test.com" , passwordEncoder.encode("Aasd12345") , UserRole.ADMIN);
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("Aasd12345" , "Aasd12345");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId , userChangePasswordRequest));

        // then
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다." , exception.getMessage());
    }

    @Test
    @DisplayName("changePassword_기존_비밀번호_검증")
    public void changePassword_기존_비밀번호_검증() {
        // given
        long userId = 1L;
        User user = new User("test@test.com" , passwordEncoder.encode("Aasd12345") , UserRole.ADMIN);
        UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("Aasd1234" , "Aasd23456");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId , userChangePasswordRequest));

        // then
        assertEquals("잘못된 비밀번호입니다." , exception.getMessage());
    }

}
