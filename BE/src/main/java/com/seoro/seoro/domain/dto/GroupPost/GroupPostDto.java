package com.seoro.seoro.domain.dto.GroupPost;

import com.seoro.seoro.domain.entity.Groups.PostCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@Builder
public class GroupPostDto {
    private Long postId;
    private String postTitle;
    private String postCategory;
    private String postContent;
    private Long memberId;
    private String memberName;
    private String memberProfile;
    private LocalDateTime postTime;
    private Boolean isUpdate;
    private List<GroupPostImageDto> images;
}
