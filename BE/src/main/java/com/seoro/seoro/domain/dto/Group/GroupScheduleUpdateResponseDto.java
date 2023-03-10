package com.seoro.seoro.domain.dto.Group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupScheduleUpdateResponseDto {
    private Boolean result;
    private Long groupId;
    private Long writerId;
    private String groupScheduleTitle;
    private LocalDateTime groupScheduleTime;
    private String groupScheduleContent;
}
