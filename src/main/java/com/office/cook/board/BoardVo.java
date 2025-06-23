package com.office.cook.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardVo {
	private int board_no;
	private int cook_no;
	private String CKG_NM; // 요리 이름
	private String userid; // 사용자 ID
	private String content; // 댓글 내용
	private int likeCount; // 좋아요 수
	private int dislikeCount; // 싫어요 수
	private String write_date; // 최근 작성 또는 수정 시간
}
