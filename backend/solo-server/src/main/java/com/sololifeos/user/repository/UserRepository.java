package com.sololifeos.user.repository;

import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.domain.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储 (CODE_RULES §3.1 Repository Interface)。
 * <p>
 * Spring Data JPA 代理实现，不手写 RepositoryImpl（框架约定）。
 * 查询方法对齐 DATABASE_DESIGN §8 索引：uk_user_email / uk_user_phone / idx_user_status。
 * <p>
 * 软删除过滤由 Entity 上的 {@code @SQLRestriction} 自动处理，无需在此声明。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 按邮箱查询（登录用，§8 uk_user_email 唯一索引）。 */
    Optional<User> findByEmail(String email);

    /** 按手机号查询（登录用，§8 uk_user_phone 唯一索引）。 */
    Optional<User> findByPhone(String phone);

    /** 邮箱是否已存在（注册校验）。 */
    boolean existsByEmail(String email);

    /** 手机号是否已存在（注册校验）。 */
    boolean existsByPhone(String phone);

    /** 按状态筛选用户列表（§8 idx_user_status 索引）。 */
    java.util.List<User> findByStatus(UserStatus status);
}
