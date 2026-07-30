package com.sololifeos.user.application;

import com.sololifeos.user.domain.model.Tag;
import com.sololifeos.user.domain.model.User;
import com.sololifeos.user.domain.model.UserPreference;
import com.sololifeos.user.dto.TagResponse;
import com.sololifeos.user.dto.UserPreferenceResponse;
import com.sololifeos.user.dto.UserResponse;

import java.util.List;

/**
 * DTO 转换器 (CODE_RULES §5: Controller 与 Application Service 之间必须经 DTO 转换)。
 * <p>
 * 将 Domain Entity 转换为 Response DTO，禁止反向转换（入参由 record 直接承载）。
 */
public final class UserAssembler {

    private UserAssembler() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getAvatar(),
                user.getEmail(),
                user.getPhone(),
                user.getCity(),
                user.getStatus() != null ? user.getStatus().name() : null,
                user.getCreatedTime()
        );
    }

    public static UserPreferenceResponse toResponse(UserPreference pref) {
        return new UserPreferenceResponse(
                pref.getId(),
                pref.getUserId(),
                pref.getInterest(),
                pref.getBudget() != null ? pref.getBudget().name() : null,
                pref.getLifestyle(),
                pref.getCreatedTime(),
                pref.getUpdatedTime()
        );
    }

    public static TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getUserId(),
                tag.getName(),
                tag.getType() != null ? tag.getType().name() : null,
                tag.getCreatedTime()
        );
    }

    public static List<TagResponse> toTagResponseList(List<Tag> tags) {
        return tags.stream().map(UserAssembler::toResponse).toList();
    }
}
