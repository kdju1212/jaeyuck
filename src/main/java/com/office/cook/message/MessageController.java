package com.office.cook.message;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.office.cook.list.ListVo;
import com.office.cook.member.MemberVo;

@Controller
@RequestMapping("/message")
public class MessageController {

	@Autowired
	MessageService messageService;

	/*
	 * 받은 쪽지 목록
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public String getmessageList(@RequestParam(value = "page", defaultValue = "1") int currentPage, HttpSession session,
			Model model) {

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		// 페이지 크기 설정 (한 페이지당 표시할 메시지 수)
		int pageSize = 10; // 예시로 10개씩 표시

		// 전체 메시지 수를 가져오기 위한 서비스 호출
		int totalMessages = messageService.getTotalMessageCount(loginedMemberVo.getUserid());

		// 전체 페이지 수 계산
		int totalPages = (int) Math.ceil((double) totalMessages / pageSize);

		// 현재 페이지가 범위를 벗어나지 않도록 처리
		if (currentPage > totalPages) {
			currentPage = totalPages;
		} else if (currentPage < 1) {
			currentPage = 1;
		}

		// 페이지에 맞는 메시지 목록 가져오기
		List<MessageVo> messageList = messageService.getMessagesByPage(loginedMemberVo.getUserid(), currentPage,
				pageSize);

		// 모델에 데이터 전달
		model.addAttribute("loginedMemberVo", loginedMemberVo);
		model.addAttribute("messageList", messageList);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", totalPages);

		return "message/getMessageList"; // JSP로 포워딩
	}

	/*
	 * 보낸 쪽지 목록
	 */
	@GetMapping("/sendMessageList")
	public String sendMessageList(@RequestParam(value = "page", defaultValue = "1") int currentPage,
			HttpSession session, Model model) {
		String nextPage = "message/sendMessageList";

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		// List<MessageVo> messageList =
		// messageService.sendMessageList(loginedMemberVo.getUserid());

		// 페이지 크기 설정 (한 페이지당 표시할 메시지 수)
		int pageSize = 10; // 예시로 10개씩 표시

		// 전체 메시지 수를 가져오기 위한 서비스 호출
		int totalMessages = messageService.sendTotalMessageCount(loginedMemberVo.getUserid());

		// 전체 페이지 수 계산
		int totalPages = (int) Math.ceil((double) totalMessages / pageSize);

		// 현재 페이지가 범위를 벗어나지 않도록 처리
		if (currentPage > totalPages) {
			currentPage = totalPages;
		} else if (currentPage < 1) {
			currentPage = 1;
		}

		// 페이지에 맞는 메시지 목록 가져오기
		List<MessageVo> messageList = messageService.sendMessagesByPage(loginedMemberVo.getUserid(), currentPage,
				pageSize);

		model.addAttribute("loginedMemberVo", loginedMemberVo);
		model.addAttribute("messageList", messageList);
		model.addAttribute("currentPage", currentPage);
		model.addAttribute("totalPages", totalPages);

		return nextPage;
	}

	/*
	 * 쪽지 상세 보기
	 */
	@GetMapping("/detail")
	public String detail(@RequestParam("message_no") int message_no, Model model, HttpSession session) {

		String nextPage = "message/messagedetail";

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		MessageVo messageInfo = messageService.getMessageByNo(message_no);

		model.addAttribute("loginedMemberVo", loginedMemberVo);
		model.addAttribute("messageInfo", messageInfo); // 데이터를 JSP로 전달

		return nextPage;
	}

	/*
	 * 쪽지 작성 폼
	 */
	@GetMapping("/messageSendForm")
	public String sendForm(HttpSession session, Model model,
			@RequestParam(value = "message_no", required = false) Integer message_no,
			@RequestParam(value = "get_id", required = false) String get_id) {
		String nextPage = "message/messageSendForm";

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		if (message_no != null) {
			MessageVo message = messageService.getMessageByNo(message_no); // 받는사람 이름 적으려고
			model.addAttribute("message", message);
		}
		if (get_id != null) {
			model.addAttribute("admin_get_id", get_id);
		}

		model.addAttribute("loginedMemberVo", loginedMemberVo);

		return nextPage;
	}

	/*
	 * 쪽지 작성 확인 (쪽지 보내기)
	 */
	@PostMapping("/messageSend")
	public String messageSend(MessageVo messageVo) {
		String nextPage = "message/message_send_ng";

		int result = messageService.messageSend(messageVo);

		if (result > 0)
			nextPage = "message/messageSendConfirm";

		return nextPage;
	}

	/*
	 * 쪽지 삭제
	 */
	@GetMapping("/messageDelete")
	public String messageDelete(HttpSession session, @RequestParam("message_no") int message_no, Model model) {

		String nextPage = "member/ng";

		int result = messageService.messageDelete(message_no);

		if (result > 0) {
			nextPage = "redirect:/message";

		}

		return nextPage;
	}

}
