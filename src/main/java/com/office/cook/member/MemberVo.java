package com.office.cook.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberVo {

	private int user_no;
	private String name; // 이름
	private String userid; // 아이디
	private String pwd; // 비밀번호
	private String email; // 이메일
	private String phone; // 휴대폰 번호
	private int admin; // 관리자 여부
	private String gender; // 성별
	private String reg_date; // 생성일자
	private String mod_date; // 수정일자

}
