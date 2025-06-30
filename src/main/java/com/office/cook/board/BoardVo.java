package com.office.cook.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardVo {
	private int board_no; // 댓글/게시글 고유 번호 (PK)

	// 데이터베이스의 실제 컬럼명에 따라 'cook_no' 또는 'recipe_id' 중 하나를 선택하세요.
	// 만약 DB 컬럼이 'recipe_id'라면 아래처럼 변경하는 것을 강력히 추천합니다.
	private int recipe_id; // 이 댓글이 속한 요리/레시피의 ID (cook_no 대신 recipe_id 사용 가정)

	private String CKG_NM; // 요리 이름 (CookBoard 테이블에 저장되어 있다면 유지)
	private String userid; // 댓글 작성자 ID
	private String content; // 댓글 내용
	private int likeCount; // 좋아요 수 (DB에서 집계된 값을 매핑)
	private int dislikeCount; // 싫어요 수 (DB에서 집계된 값을 매핑)
	private String write_date; // 작성 날짜
}