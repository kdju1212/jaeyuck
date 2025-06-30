package com.office.cook.board;

import lombok.Data;

@Data
public class LikeVo {

	// DB 컬럼과 매핑될 필드들
	private int reaction_id; // 좋아요/싫어요 반응 자체의 고유 ID (PK, DB에 따라 SERIAL 타입이므로 int)
	private Integer board_no; // 댓글 번호 (nullable 이므로 Integer 사용 권장)
	private Integer recipe_id; // 레시피 ID (nullable 이므로 Integer 사용 권장)
	private String like_id; // 좋아요/싫어요를 누른 사용자 ID
	private String liked; // 좋아요 여부 ('1' 또는 '0')
	private String disliked; // 싫어요 여부 ('1' 또는 '0')
	private String liked_date; // 좋아요/싫어요 한 날짜

	// DDL에 'reaction_id' 컬럼이 추가되었으므로 LikeVo에도 추가해야 합니다.
	// `board_no`와 `recipe_id`는 DDL에서 `INT` 타입이지만, NULL을 허용하므로 Java에서는 `int` 대신
	// `Integer` (래퍼 클래스)를 사용하는 것이 좋습니다.
	// `int`는 기본적으로 0으로 초기화되지만 `Integer`는 `null` 값을 가질 수 있기 때문입니다.
}