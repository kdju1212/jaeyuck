package com.office.cook.message;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

	@Autowired
	MessageDao messageDao;

	/*
	 * 받은 메시지 수 가져오기 (로그인한 사용자만)
	 */
	public int getTotalMessageCount(String userId) {
		return messageDao.getTotalMessageCount(userId);
	}

	/*
	 * id로 받은 메시지 출력
	 */
	public List<MessageVo> getMessagesByPage(String userId, int currentPage, int pageSize) {
		int offset = (currentPage - 1) * pageSize;
		return messageDao.getMessagesByPage(userId, offset, pageSize);
	}

	/*
	 * id로 보낸 메시지 출력
	 */
	public List<MessageVo> sendMessagesByPage(String userid, int currentPage, int pageSize) {
		int offset = (currentPage - 1) * pageSize;
		return messageDao.sendMessagesByPage(userid, offset, pageSize);
	}

	/*
	 * 보낸 쪽지 개수
	 */
	public int sendTotalMessageCount(String userid) {

		return messageDao.sendTotalMessageCount(userid);
	}

	/*
	 * 번호로 메시지 가져오기
	 */
	public MessageVo getMessageByNo(int message_no) {

		return messageDao.getMessageByNo(message_no);
	}

	/*
	 * 쪽지 보내기
	 */
	public int messageSend(MessageVo messageVo) {

		return messageDao.messageSend(messageVo);
	}

	/*
	 * 보낸 쪽지함
	 */
//	public List<MessageVo> sendMessageList(String userid) {
//
//		return messageDao.sendMessageList(userid);
//	}

	/*
	 * 쪽지 삭제
	 */
	public int messageDelete(int message_no) {

		return messageDao.messageDelete(message_no);
	}

}
