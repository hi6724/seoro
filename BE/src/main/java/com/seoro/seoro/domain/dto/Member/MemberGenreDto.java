package com.seoro.seoro.domain.dto.Member;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberGenreDto {
	private Long userGenreId;
	private Long userId;
	private Long genreId;
}
