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
	public String addComment(@RequestParam("recipeId") int recipeId, // HTML 폼의 name="recipeId"와 정확히 매핑
			@RequestParam("content") String content, // 댓글 내용
			@RequestParam("ckg_nm") String ckg_nm, // 요리 이름
			HttpSession session) {

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		// 로그인 여부 확인
		if (loginedMemberVo == null) {
			System.out.println("DEBUG_ADD_COMMENT: 로그인되지 않은 사용자 요청.");
			return "redirect:/member/loginForm"; // 로그인되어 있지 않으면 로그인 폼으로 리다이렉트
		}

		// BoardVo 객체를 생성하고 수동으로 값 설정
		BoardVo comment = new BoardVo();
		comment.setRecipe_id(recipeId); // ⭐ @RequestParam으로 받은 recipeId 값을 BoardVo에 설정 ⭐
		comment.setUserid(loginedMemberVo.getUserid());
		comment.setContent(content);
		comment.setCKG_NM(ckg_nm);

		// 디버그 로그 추가: 현재 전달받은 파라미터 값과 BoardVo에 설정된 값 확인
		System.out.println("DEBUG_ADD_COMMENT: addComment 요청 - recipeId: " + recipeId);
		System.out.println("DEBUG_ADD_COMMENT: addComment 요청 - content: " + content);
		System.out.println("DEBUG_ADD_COMMENT: addComment 요청 - ckg_nm: " + ckg_nm);
		System.out.println("DEBUG_ADD_COMMENT: BoardVo에 설정된 recipe_id: " + comment.getRecipe_id());
		System.out.println("DEBUG_ADD_COMMENT: BoardVo에 설정된 userid: " + comment.getUserid());

		// 서비스 계층 호출하여 댓글 저장
		boardService.addComment(comment);

		// 리다이렉트 URL 처리
		// cookName (ckg_nm)이 URL 파라미터로 사용되므로 인코딩 필요
		try {
			String encodedCookName = URLEncoder.encode(ckg_nm, "UTF-8");
			// 댓글 등록 후 해당 레시피 상세 페이지로 리다이렉트
			return "redirect:/list/details?recipeId=" + recipeId + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// 인코딩 실패 시 에러 페이지 반환
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
	 * 댓글 수정 (AJAX JSON 응답 방식으로 변경)
	 */
	@PostMapping("/updateComment")
	@ResponseBody // JSON 응답을 위해 추가
	public ResponseEntity<Map<String, Object>> updateComment( // 반환 타입을 String에서 ResponseEntity로 변경
			HttpSession session, @RequestParam("board_no") int board_no, // 댓글 번호
			// @RequestParam("recipeId") int recipeId, // 리다이렉트하지 않으므로 더 이상 필요 없을 수 있음 (선택적)
			// @RequestParam("ckg_nm") String ckg_nm, // 리다이렉트하지 않으므로 더 이상 필요 없을 수 있음 (선택적)
			@RequestParam("new_content") String new_content) {

		Map<String, Object> response = new HashMap<>(); // 응답 데이터를 담을 맵 생성
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		System.out.println(
				"DEBUG_UPDATE_COMMENT: updateComment 요청 - new_content=" + new_content + ", board_no=" + board_no);

		// 로그인 여부 확인
		if (loginedMemberVo == null) {
			response.put("success", false);
			response.put("message", "로그인이 필요합니다.");
			System.out.println("DEBUG_UPDATE_COMMENT: 로그인되지 않은 사용자. 401 UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
		}

		try {
			// BoardVo 객체를 생성하여 필요한 값 설정 (권한 확인 및 서비스 로직을 위해)
			BoardVo boardVo = new BoardVo();
			boardVo.setBoard_no(board_no);
			// boardVo.setRecipe_id(recipeId); // 만약 서비스에서 필요하다면 설정
			// boardVo.setCKG_NM(ckg_nm); // 만약 서비스에서 필요하다면 설정

			// 서비스 계층 호출 (서비스 내에서 해당 댓글의 소유자가 현재 로그인한 사용자인지 확인해야 함)
			boardService.updateComment(loginedMemberVo.getUserid(), boardVo, new_content);

			response.put("success", true);
			response.put("message", "댓글이 성공적으로 수정되었습니다.");
			// 프론트엔드에서 댓글 내용을 즉시 업데이트할 수 있도록, 수정된 내용도 함께 반환
			response.put("updatedContent", new_content);
			System.out.println("DEBUG_UPDATE_COMMENT: 댓글 수정 성공. 200 OK");
			return ResponseEntity.ok(response); // 200 OK

		} catch (RuntimeException e) { // 서비스에서 발생시킨 예외 (예: 권한 없음, 댓글 없음)
			response.put("success", false);
			response.put("message", e.getMessage());
			System.out.println(
					"DEBUG_UPDATE_COMMENT: 댓글 수정 실패 (RuntimeException): " + e.getMessage() + ". 400 BAD REQUEST");
			return ResponseEntity.badRequest().body(response); // 400 Bad Request
		} catch (Exception e) { // 기타 예상치 못한 서버 내부 오류
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "서버 내부 오류가 발생했습니다.");
			System.out.println(
					"DEBUG_UPDATE_COMMENT: 댓글 수정 실패 (Exception): " + e.getMessage() + ". 500 INTERNAL SERVER ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 Internal Server Error
		}
	}

	/*
	 * 댓글 삭제 (AJAX JSON 응답 방식으로 변경)
	 */
	@PostMapping("/deleteComment")
	@ResponseBody // JSON 응답을 위해 추가
	public ResponseEntity<Map<String, Object>> deleteComment( // 반환 타입을 String에서 ResponseEntity로 변경
			HttpSession session, @RequestParam("board_no") int board_no // 삭제할 댓글 번호
	) {
		Map<String, Object> response = new HashMap<>(); // 응답 데이터를 담을 맵 생성
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		System.out.println("DEBUG_DELETE_COMMENT: deleteComment 요청 - board_no=" + board_no);

		// 로그인 여부 확인
		if (loginedMemberVo == null) {
			response.put("success", false);
			response.put("message", "로그인이 필요합니다.");
			System.out.println("DEBUG_DELETE_COMMENT: 로그인되지 않은 사용자. 401 UNAUTHORIZED");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Unauthorized
		}

		try {
			// BoardVo 객체를 생성하여 필요한 값 설정 (서비스 로직을 위해)
			BoardVo boardVo = new BoardVo();
			boardVo.setBoard_no(board_no);

			// 서비스 계층 호출 (서비스 내에서 해당 댓글의 소유자가 현재 로그인한 사용자인지 확인해야 함)
			boardService.deleteComment(loginedMemberVo.getUserid(), boardVo);

			response.put("success", true);
			response.put("message", "댓글이 성공적으로 삭제되었습니다.");
			// 삭제 성공 시, 프론트엔드에서 해당 댓글 UI를 제거하도록 처리합니다.
			System.out.println("DEBUG_DELETE_COMMENT: 댓글 삭제 성공. 200 OK");
			return ResponseEntity.ok(response); // 200 OK

		} catch (RuntimeException e) { // 서비스에서 발생시킨 예외 (예: 권한 없음, 댓글 없음)
			response.put("success", false);
			response.put("message", e.getMessage());
			System.out.println(
					"DEBUG_DELETE_COMMENT: 댓글 삭제 실패 (RuntimeException): " + e.getMessage() + ". 400 BAD REQUEST");
			return ResponseEntity.badRequest().body(response); // 400 Bad Request
		} catch (Exception e) { // 기타 예상치 못한 서버 내부 오류
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "서버 내부 오류가 발생했습니다.");
			System.out.println(
					"DEBUG_DELETE_COMMENT: 댓글 삭제 실패 (Exception): " + e.getMessage() + ". 500 INTERNAL SERVER ERROR");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 Internal Server Error
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