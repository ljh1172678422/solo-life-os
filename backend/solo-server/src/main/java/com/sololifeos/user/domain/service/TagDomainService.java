package com.sololifeos.user.domain.service;

import com.sololifeos.common.exception.BusinessException;
import com.sololifeos.user.domain.model.Tag;
import com.sololifeos.user.domain.model.TagType;
import com.sololifeos.user.repository.TagRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签领域服务 (CODE_RULES §3.1 Domain Service)。
 * <p>
 * 职责：标签创建去重校验、查询。持久化归 Application Service (TASK-0103)。
 * 唯一约束：(user_id, name, type) 防重复标签 (§8 uk_tag_user_name_type)。
 */
@Service
public class TagDomainService {

    private final TagRepository tagRepository;

    public TagDomainService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * 创建标签。校验 (userId, name, type) 唯一后创建对象。
     *
     * @return 未持久化的 Tag
     */
    public Tag create(Long userId, String name, TagType type) {
        if (userId == null) {
            throw new BusinessException("userId 不可为空");
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException("标签名不可为空");
        }
        if (type == null) {
            type = TagType.GENERAL;
        }
        if (tagRepository.existsByUserIdAndNameAndType(userId, name, type)) {
            throw new BusinessException("标签已存在: userId=" + userId + ", name=" + name + ", type=" + type);
        }
        return new Tag(userId, name, type);
    }

    /**
     * 查询用户全部标签。
     */
    public List<Tag> listByUser(Long userId) {
        return tagRepository.findByUserId(userId);
    }

    /**
     * 按类型查询用户标签。
     */
    public List<Tag> listByUserAndType(Long userId, TagType type) {
        return tagRepository.findByUserIdAndType(userId, type);
    }
}
