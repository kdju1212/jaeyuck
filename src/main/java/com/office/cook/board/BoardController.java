package com.office.cook.board;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.office.cook.member.MemberVo;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/board") // 클래스 레벨의 기본 경로 설정
public class BoardController {

	@Autowired
	BoardService boardService;

	/*
	 * 댓글 작성
	 */
	@PostMapping("/addComment")
	public String addComment(BoardVo comment) {
		boardService.addComment(comment);

		// CKG_NM은 recipe 테이블에서 조인해서 가져오므로, BoardVo에 항상 값이 있을 거라고 가정하기 어렵습니다.
		// 리다이렉트 시에는 recipe_id만으로 충분하며, cookName은 필요하다면 해당 페이지에서 다시 조회할 수 있습니다.
		// 또는, comment 객체에 ckg_nm이 있다면 (예: 폼에서 hidden 필드로 넘겨받았다면) 그대로 사용합니다.
		String cookName = comment.getCKG_NM();
		int recipe_id = comment.getRecipe_id();

		// cookName이 없을 경우의 예외 처리 로직은 그대로 유지합니다.
		if (cookName == null || cookName.isEmpty()) {
			return "error";
		}

		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			// 리다이렉트 URL 파라미터는 'recipeId'로 통일
			return "redirect:/list/details?recipeId=" + recipe_id + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "error";
		}
	}

	/*
	 * 내가 쓴 댓글 목록 조회
	 */
	@GetMapping("/myComment")
	public String myComment(Model model, HttpSession session) {
		String nextPage = "list/myComment";
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		if (loginedMemberVo == null) {
			return "redirect:/member/loginForm";
		}

		List<BoardVo> comments = boardService.myComment(loginedMemberVo.getUserid());
		model.addAttribute("comments", comments);

		return nextPage;
	}

	/*
	 * 댓글 수정
	 */
	@PostMapping("/updateComment")
	public String updateComment(HttpSession session, BoardVo boardVo, @RequestParam("new_content") String new_content) {
		System.out.println("updateComment Controller new_content=" + new_content);
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		if (loginedMemberVo == null) {
			return "redirect:/member/loginForm";
		}

		boardService.updateComment(loginedMemberVo.getUserid(), boardVo, new_content);

		// 리다이렉트 URL 처리 (addComment와 동일)
		String cookName = boardVo.getCKG_NM();
		int recipe_id = boardVo.getRecipe_id();

		if (cookName == null || cookName.isEmpty()) {
			return "error";
		}

		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			return "redirect:/list/details?recipeId=" + recipe_id + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "error";
		}
	}

	/*
	 * 댓글 삭제
	 */
	@PostMapping("/deleteComment")
	public String deleteComment(HttpSession session, BoardVo boardVo) {
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		if (loginedMemberVo == null) {
			return "redirect:/member/loginForm";
		}

		boardService.deleteComment(loginedMemberVo.getUserid(), boardVo);

		// 리다이렉트 URL 처리 (addComment와 동일)
		String cookName = boardVo.getCKG_NM();
		int recipe_id = boardVo.getRecipe_id();

		if (cookName == null || cookName.isEmpty()) {
			return "error";
		}

		try {
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			return "redirect:/list/details?recipeId=" + recipe_id + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "error";
		}
	}

	/*
	 * 댓글/게시글 좋아요 또는 싫어요 처리 (AJAX 응답) URL: /board/processReaction
	 *
	 * 필요한 파라미터만 받도록 수정 (ckg_nm, content, targetUserId 제거). board_no와 recipe_id는
	 * Integer로 처리하여 null을 받을 수 있게 함.
	 */
	@PostMapping("/processReaction")
	@ResponseBody // JSON 응답을 위해 필수
	public ResponseEntity<Map<String, Object>> processReaction(
			@RequestParam(value = "board_no", required = false) Integer boardNo, // 댓글인 경우 사용, 없으면 null
			@RequestParam(value = "recipe_id", required = false) Integer recipeId, // 레시피인 경우 사용, 없으면 null
			@RequestParam("userId") String userId,
			// @RequestParam("ckg_nm") String ckg_nm, // LikeVo에서 제거했으므로 더 이상 받지 않음
			// @RequestParam("content") String content, // LikeVo에서 제거했으므로 더 이상 받지 않음
			// @RequestParam("comment_userid") String targetUserId, // LikeVo에서 제거했으므로 더 이상
			// 받지 않음
			@RequestParam("reactionType") String reactionType) {

		Map<String, Object> response = new HashMap<>();
		int newLikeCount = 0;
		int newDislikeCount = 0;

		try {
			// 반응의 대상이 레시피인지 댓글인지 구분
			// DB의 CHK_ONE_TARGET 제약조건과 일치시키기 위해, 둘 중 하나는 반드시 null이어야 함.
			boolean isRecipeReaction = (recipeId != null && boardNo == null);
			boolean isCommentReaction = (boardNo != null && recipeId == null);

			if (!isRecipeReaction && !isCommentReaction) {
				response.put("success", false);
				response.put("message", "잘못된 요청입니다. 레시피 ID 또는 댓글 ID 중 하나만 제공되어야 합니다.");
				return ResponseEntity.badRequest().body(response);
			}

			// BoardService의 processReaction 메서드 호출.
			// 이제 ckg_nm, content, targetUserId는 전달하지 않습니다.
			// boardNo와 recipeId는 Integer 타입으로, 실제 값 또는 null을 전달합니다.
			int result = boardService.processReaction(boardNo, recipeId, userId, reactionType);

			String message = "";
			if (result == 1) { // 새로운 반응 추가
				message = reactionType.equalsIgnoreCase("LIKE") ? "좋아요 완료!" : "싫어요 완료!";
			} else if (result == -1) { // 기존 반응 취소 (삭제)
				message = reactionType.equalsIgnoreCase("LIKE") ? "좋아요 취소 완료!" : "싫어요 취소 완료!";
			} else if (result == -2) { // 반응 변경 (예: 싫어요 -> 좋아요)
				message = reactionType.equalsIgnoreCase("LIKE") ? "싫어요에서 좋아요로 변경 완료!" : "좋아요에서 싫어요로 변경 완료!";
			} else { // 처리 실패 또는 기타 오류
				message = "요청 처리 실패 또는 알 수 없는 오류.";
			}

			if (result != 0) { // 성공적으로 처리된 경우 (1, -1, -2)
				response.put("success", true);
				response.put("message", message);

				// 좋아요/싫어요 개수 조회 시 레시피/댓글에 따라 적절한 메서드 호출
				// 여기서는 boardNo와 recipeId가 각각의 유효성을 나타내므로 0 체크 대신 null 체크로 변경
				if (isRecipeReaction) {
					newLikeCount = boardService.getRecipeLikeCount(recipeId); // recipeId는 null이 아님
					newDislikeCount = boardService.getRecipeDislikeCount(recipeId); // recipeId는 null이 아님
				} else { // isCommentReaction
					newLikeCount = boardService.getCommentLikeCount(boardNo); // boardNo는 null이 아님
					newDislikeCount = boardService.getCommentDislikeCount(boardNo); // boardNo는 null이 아님
				}

				response.put("newLikeCount", newLikeCount);
				response.put("newDislikeCount", newDislikeCount);
				return ResponseEntity.ok(response);
			} else { // 처리 실패 (result가 0인 경우)
				response.put("success", false);
				response.put("message", message);
				return ResponseEntity.badRequest().body(response); // 400 Bad Request
			}
		} catch (Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "서버 내부 오류가 발생했습니다.");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 Internal Server Error
		}
	}
}