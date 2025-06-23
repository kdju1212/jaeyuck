package com.office.cook.board;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import jakarta.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.office.cook.member.MemberVo;

@Controller
@RequestMapping("/board")
public class BoardController {

	@Autowired
	BoardService boardService;

	/*
	 * 댓글 작성
	 */
	@PostMapping("/addComment")
	public String addComment(BoardVo comment) {
		// 댓글 추가 서비스 호출
		boardService.addComment(comment);

		// 댓글을 추가한 후 해당 요리의 상세 페이지로 포워딩
		String cookName = comment.getCKG_NM();
		int cook_no = comment.getCook_no()
;		if (cookName == null || cookName.isEmpty()) {
			// 예외 처리: 기본값 또는 에러 페이지로 이동
			return "error"; // error.jsp로 이동
		}

		// cookName 인코딩 처리
		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			String encodedCook_no = URLEncoder.encode(String.valueOf(cook_no), "UTF-8");
			// 리다이렉트 URL에 인코딩된 요리 이름을 추가
			return "redirect:/list/details?cook_no=" + encodedCook_no + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// 인코딩 오류 발생 시 에러 페이지로 이동
			return "error";
		}
	}

	/*
	 * 내가 쓴 댓글 목록
	 */
	@GetMapping("/myComment")
	public String myComment(Model model, HttpSession session) {


		String nextPage = "list/myComment";

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		List<BoardVo> comments = boardService.myComment(loginedMemberVo.getUserid());

		model.addAttribute("comments", comments); // 데이터를 JSP로 전달

		return nextPage;
	}
	
	/*
	 * 댓글 수정
	 */
	@PostMapping("/updateComment")
	public String updateComment(HttpSession session,BoardVo boardVo,@RequestParam("new_content") String new_content) {
		System.out.println("updateComment Controller new_content="+new_content);
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		int result = boardService.updateComment(loginedMemberVo.getUserid(), boardVo,new_content);

		String cookName = boardVo.getCKG_NM();
		int cook_no = boardVo.getCook_no();
		// cookName 인코딩 처리
		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			String encodedCook_no = URLEncoder.encode(String.valueOf(cook_no), "UTF-8");
			// 리다이렉트 URL에 인코딩된 요리 이름을 추가
			return "redirect:/list/details?cook_no=" + encodedCook_no + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// 인코딩 오류 발생 시 에러 페이지로 이동
			return "error";
		}
	}

	/*
	 * 댓글 삭제
	 */
	@PostMapping("/deleteComment")
	public String deleteComment(HttpSession session, BoardVo boardVo) {

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		int result = boardService.deleteComment(loginedMemberVo.getUserid(), boardVo);

		String cookName = boardVo.getCKG_NM();
		int cook_no = boardVo.getCook_no();
		// cookName 인코딩 처리
		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			String encodedCook_no = URLEncoder.encode(String.valueOf(cook_no), "UTF-8");
			// 리다이렉트 URL에 인코딩된 요리 이름을 추가
			return "redirect:/list/details?cook_no=" + encodedCook_no + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// 인코딩 오류 발생 시 에러 페이지로 이동
			return "error";
		}
	}

	/*
	 * 댓글 좋아요
	 */
	@PostMapping("/likeComment")
	public String likeComment(HttpSession session, LikeVo likeVo, @RequestParam("cook_no") int cook_no) {

		int result = boardService.likeComment(likeVo);

		String cookName = likeVo.getCKG_NM();
		// cookName 인코딩 처리
		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			String encodedCook_no = URLEncoder.encode(String.valueOf(cook_no), "UTF-8");
			// 리다이렉트 URL에 인코딩된 요리 이름을 추가
			return "redirect:/list/details?cook_no=" + encodedCook_no + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// 인코딩 오류 발생 시 에러 페이지로 이동
			return "error";
		}
	}
	
	/*
	 * 댓글 싫어요
	 */
	@PostMapping("/dislikeComment")
	public String dislikeComment(HttpSession session, LikeVo likeVo, @RequestParam("cook_no") int cook_no) {

		int result = boardService.dislikeComment(likeVo);

		String cookName = likeVo.getCKG_NM();
		// cookName 인코딩 처리
		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			String encodedCook_no = URLEncoder.encode(String.valueOf(cook_no), "UTF-8");
			// 리다이렉트 URL에 인코딩된 요리 이름을 추가
			return "redirect:/list/details?cook_no=" + encodedCook_no + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// 인코딩 오류 발생 시 에러 페이지로 이동
			return "error";
		}
	}

}
