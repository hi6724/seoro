package com.seoro.seoro.domain.dto.Group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupShowDto {
	private String groupName;
	private String groupDescrib;
	private String groupProfile;
	private Long groupId;
}
