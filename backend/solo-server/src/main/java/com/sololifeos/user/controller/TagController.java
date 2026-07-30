package com.sololifeos.user.controller;

import com.sololifeos.common.response.ApiResponse;
import com.sololifeos.user.application.TagApplicationService;
import com.sololifeos.user.application.UserAssembler;
import com.sololifeos.user.domain.model.Tag;
import com.sololifeos.user.domain.model.TagType;
import com.sololifeos.user.dto.TagCreateRequest;
import com.sololifeos.user.dto.TagResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签控制器。
 */
@RestController
@RequestMapping("/api/users/{userId}/tags")
public class TagController {

    private final TagApplicationService tagApplicationService;

    public TagController(TagApplicationService tagApplicationService) {
        this.tagApplicationService = tagApplicationService;
    }

    /**
     * 创建标签。
     */
    @PostMapping
    public ApiResponse<TagResponse> create(@PathVariable Long userId,
                                           @Valid @RequestBody TagCreateRequest request) {
        Tag tag = tagApplicationService.create(userId, request.name(), request.type());
        return ApiResponse.success(UserAssembler.toResponse(tag));
    }

    /**
     * 查询用户标签。支持按类型筛选（?type=INTEREST）。
     */
    @GetMapping
    public ApiResponse<List<TagResponse>> list(@PathVariable Long userId,
                                              @RequestParam(required = false) TagType type) {
        List<Tag> tags = type != null
                ? tagApplicationService.listByUserAndType(userId, type)
                : tagApplicationService.listByUser(userId);
        return ApiResponse.success(UserAssembler.toTagResponseList(tags));
    }
}
