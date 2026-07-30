package com.sololifeos.user.dto;

import com.sololifeos.user.domain.model.TagType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 标签创建请求 DTO。
 */
public record TagCreateRequest(
        @NotBlank(message = "标签名不可为空")
        @Size(max = 50, message = "标签名最长 50 字符")
        String name,

        TagType type
) {
}
