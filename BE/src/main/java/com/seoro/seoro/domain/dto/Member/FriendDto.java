package com.seoro.seoro.domain.dto.Member;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FriendDto {
	private Long friendId;
	private Long userId;
}