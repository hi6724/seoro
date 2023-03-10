package com.seoro.seoro.domain.dto.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecommendMemberDto {
	private String memberProfile;
	private String memberName;
	private Long memberId;
}
