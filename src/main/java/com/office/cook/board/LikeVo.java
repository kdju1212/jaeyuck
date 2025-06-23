package com.office.cook.board;

import lombok.Data;

@Data
public class LikeVo {

	private int board_no; // 댓글 번호
	private String CKG_NM; // 요리이름
	private String content; // 댓글
	private String userid; // 댓글 작성자 아이디
	private String like_id; // 좋아요를 누른 아이디
	private String liked; // 좋아요
	private String disliked; // 싫어요
	private String liked_date; // 좋아요싫어요 한 날짜
	
}
