package com.sololifeos.user.application;

import com.sololifeos.user.domain.model.Tag;
import com.sololifeos.user.domain.model.TagType;
import com.sololifeos.user.domain.service.TagDomainService;
import com.sololifeos.user.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 标签应用服务 (CODE_RULES §3.1 Application Service)。
 * <p>
 * 职责：标签创建 / 查询用例协调 / 事务边界。
 */
@Service
public class TagApplicationService {

    private final TagDomainService tagDomainService;
    private final TagRepository tagRepository;

    public TagApplicationService(TagDomainService tagDomainService, TagRepository tagRepository) {
        this.tagDomainService = tagDomainService;
        this.tagRepository = tagRepository;
    }

    /**
     * 创建标签。校验 (userId, name, type) 唯一后持久化。
     */
    @Transactional
    public Tag create(Long userId, String name, TagType type) {
        Tag tag = tagDomainService.create(userId, name, type);
        return tagRepository.save(tag);
    }

    /**
     * 查询用户全部标签。
     */
    @Transactional(readOnly = true)
    public List<Tag> listByUser(Long userId) {
        return tagDomainService.listByUser(userId);
    }

    /**
     * 按类型查询用户标签。
     */
    @Transactional(readOnly = true)
    public List<Tag> listByUserAndType(Long userId, TagType type) {
        return tagDomainService.listByUserAndType(userId, type);
    }
}
