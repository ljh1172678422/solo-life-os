package com.sololifeos.user.repository;

import com.sololifeos.user.domain.model.Tag;
import com.sololifeos.user.domain.model.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 标签仓储 (CODE_RULES §3.1 Repository Interface)。
 * <p>
 * 查询方法对齐 DATABASE_DESIGN §8 uk_tag_user_name_type 唯一索引。
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /** 查询某用户的全部标签。 */
    List<Tag> findByUserId(Long userId);

    /** 按用户和类型查询标签。 */
    List<Tag> findByUserIdAndType(Long userId, TagType type);

    /** 查询某用户指定名称与类型的标签（§8 uk_tag_user_name_type）。 */
    Optional<Tag> findByUserIdAndNameAndType(Long userId, String name, TagType type);

    /** 判断标签是否已存在（创建前去重校验）。 */
    boolean existsByUserIdAndNameAndType(Long userId, String name, TagType type);
}
