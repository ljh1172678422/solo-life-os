package com.sololifeos.user.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.domain.model.UserStatus;
import com.sololifeos.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserDomainService} 单元测试 (CODE_RULES §10 Testing)。
 * <p>
 * 覆盖 register / activate / ban / updateProfile 业务规则。
 * Mock UserRepository，不依赖数据库。
 */
@ExtendWith(MockitoExtension.class)
class UserDomainServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDomainService userDomainService;

    private static final String HASHED_PWD = "$2a$10$hashedpasswordplaceholderhashedpasswordplaceholder";

    @Nested
    @DisplayName("register 注册校验")
    class RegisterTest {

        @Test
        @DisplayName("正常注册：返回 INACTIVE 状态用户，密码已哈希")
        void shouldRegisterInactiveUserWithHashedPassword() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(userRepository.existsByPhone("13800138000")).thenReturn(false);

            User user = userDomainService.register("tester", "test@example.com", "13800138000", HASHED_PWD);

            assertThat(user.getNickname()).isEqualTo("tester");
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPhone()).isEqualTo("13800138000");
            assertThat(user.getPassword()).isEqualTo(HASHED_PWD);
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        }

        @Test
        @DisplayName("昵称为空：抛 BusinessException")
        void shouldThrowWhenNicknameBlank() {
            assertThatThrownBy(() -> userDomainService.register("", "test@example.com", null, HASHED_PWD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("昵称不可为空");
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("密码为空：抛 BusinessException")
        void shouldThrowWhenPasswordBlank() {
            assertThatThrownBy(() -> userDomainService.register("tester", "test@example.com", null, ""))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("密码不可为空");
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("邮箱已注册：抛 BusinessException")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userDomainService.register("tester", "dup@example.com", null, HASHED_PWD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("邮箱已被注册");
            verify(userRepository, times(1)).existsByEmail("dup@example.com");
            verify(userRepository, never()).existsByPhone(anyString());
        }

        @Test
        @DisplayName("手机号已注册：抛 BusinessException")
        void shouldThrowWhenPhoneExists() {
            when(userRepository.existsByPhone("13800138000")).thenReturn(true);

            assertThatThrownBy(() -> userDomainService.register("tester", null, "13800138000", HASHED_PWD))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("手机号已被注册");
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("邮箱手机号均空：允许注册（匿名用户）")
        void shouldAllowRegisterWithoutContact() {
            User user = userDomainService.register("anon", null, null, HASHED_PWD);

            assertThat(user.getEmail()).isNull();
            assertThat(user.getPhone()).isNull();
            assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).existsByPhone(anyString());
        }
    }

    @Nested
    @DisplayName("activate 激活规则")
    class ActivateTest {

        @Test
        @DisplayName("INACTIVE 用户可激活")
        void shouldActivateInactiveUser() {
            User user = User.register("tester", "t@e.com", null, HASHED_PWD);

            userDomainService.activate(user);

            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("BANNED 用户不可激活：抛 BusinessException")
        void shouldNotActivateBannedUser() {
            User user = User.register("tester", "t@e.com", null, HASHED_PWD);
            user.ban();

            assertThatThrownBy(() -> userDomainService.activate(user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("封禁用户不可激活");
            assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
        }
    }

    @Nested
    @DisplayName("ban 封禁规则")
    class BanTest {

        @Test
        @DisplayName("ACTIVE 用户可封禁")
        void shouldBanActiveUser() {
            User user = new User("tester", "t@e.com", null);
            user.activate();

            userDomainService.ban(user);

            assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
        }
    }

    @Nested
    @DisplayName("updateProfile 资料更新规则")
    class UpdateProfileTest {

        @Test
        @DisplayName("正常更新昵称/头像/城市")
        void shouldUpdateProfile() {
            User user = new User("old", "t@e.com", null);
            user.activate();

            userDomainService.updateProfile(user, "newname", "https://avatar.png", "Beijing");

            assertThat(user.getNickname()).isEqualTo("newname");
            assertThat(user.getAvatar()).isEqualTo("https://avatar.png");
            assertThat(user.getCity()).isEqualTo("Beijing");
        }

        @Test
        @DisplayName("昵称为空：抛 BusinessException")
        void shouldThrowWhenNicknameBlankOnUpdate() {
            User user = new User("old", "t@e.com", null);

            assertThatThrownBy(() -> userDomainService.updateProfile(user, "", null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("昵称不可为空");
            assertThat(user.getNickname()).isEqualTo("old");
        }
    }
}
