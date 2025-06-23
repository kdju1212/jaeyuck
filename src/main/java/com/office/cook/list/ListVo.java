package com.office.cook.list;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListVo {
	private int cook_no;
	private int recipe_no;
	private String CKG_NM; // 요리 이름
	private String CKG_KND_ACTO_NM; // 요리 종류
	private String CKG_MTRL_CN; // 재료
	private String CKG_INBUN_NM; // 인분
	private String CKG_TIME_NM; // 시간
	private String CKG_IMG_URL; // 이미지 경로
	private MultipartFile CKG_IMG_FILE; // 파일 경로
	private int read_count; // 조회수
	private String step1;
	private String step2;
	private String step3;
	private String step4;
	private String step5;
	private String step6;
	private String step7;
	private String step8;
	private String step9;
	private String step10;
	private String step11;
	private String step12;
	private String step13;
	private String step14;
	private String step15;
	private String step16;
	private String step17;
	private String step18;
	private String step19;
	private String step20;

}
