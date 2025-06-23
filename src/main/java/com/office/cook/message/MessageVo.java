package com.office.cook.message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageVo {

	private int message_no;
	private int send_id_no;
	private int get_id_no;
	private String get_id_name;
	private String send_id_name;
	private String send_id;
	private String get_id;
	private String title;
	private String content;
	private String send_date;
	
}
