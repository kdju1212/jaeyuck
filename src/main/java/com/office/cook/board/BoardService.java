package com.office.cook.board;

import com.office.cook.board.LikeVo; // LikeVo 패키지 경로를 다시 한번 확인하세요.
										// 만약 com.office.cook.like 패키지로 이동했다면 import com.office.cook.like.LikeVo; 로 변경해야 합니다.
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BoardService {

	@Autowired
	BoardDao boardDao;

	/*
	 * 특정 레시피 ID에 대한 댓글 목록을 조회합니다 (페이지네이션 적용).
	 */
	public List<BoardVo> getCommentsByCookId(int recipeId, int offset, int pageSize) {
		return boardDao.getCommentsByCookId(recipeId, offset, pageSize);
	}

	/*
	 * 특정 레시피 ID에 대한 총 댓글 개수를 조회합니다.
	 */
	public int getTotalCommentsCount(int recipeId) {
		return boardDao.getTotalCommentsCount(recipeId);
	}

	/*
	 * 댓글을 추가합니다.
	 */
	public void addComment(BoardVo comment) {
		boardDao.insertComment(comment);
	}

	/*
	 * 특정 사용자가 작성한 댓글 목록을 조회합니다.
	 */
	public List<BoardVo> myComment(String userid) {
		return boardDao.myComment(userid);
	}

	/*
	 * 댓글을 삭제합니다.
	 *
	 * @param userid 로그인된 사용자 ID (삭제 권한 확인용)
	 * 
	 * @param boardVo 삭제할 댓글 정보 (board_no 포함)
	 * 
	 * @return 삭제된 행의 수
	 */
	public int deleteComment(String userid, BoardVo boardVo) {
		return boardDao.deleteComment(userid, boardVo);
	}

	/*
	 * 댓글/게시글 좋아요 또는 싫어요를 처리합니다 (통합).
	 *
	 * @param boardNo 게시글/댓글 ID (댓글인 경우 사용, 레시피인 경우 null 또는 0)
	 * 
	 * @param recipeId 해당 댓글이 속한 레시피의 ID (레시피인 경우 사용, 댓글인 경우 null 또는 0)
	 * 
	 * @param userId 좋아요/싫어요를 누른 사용자 ID
	 * 
	 * @param reactionType 요청된 반응 타입 ('LIKE' 또는 'DISLIKE')
	 * 
	 * @return 1: 새로운 반응 추가 성공, -1: 기존 반응 취소 성공, -2: 반응 변경 성공 (예: 좋아요 -> 싫어요), 0: 처리
	 * 실패
	 */
	@Transactional
	public int processReaction(Integer boardNo, Integer recipeId, String userId, String reactionType) {
		// ckg_nm, content, targetUserId 파라미터는 LikeVo 및 DB 스키마에서 제거되었으므로 더 이상 필요 없습니다.
		// boardNo와 recipeId를 int에서 Integer로 변경하여 null 값을 받을 수 있도록 합니다.

		// 레시피 반응인지 댓글 반응인지 명확하게 구분합니다.
		// DDL의 CHK_ONE_TARGET 제약 조건에 따라 boardNo 또는 recipeId 둘 중 하나만 유효한 값을 가져야 합니다.
		boolean isRecipeReaction = (recipeId != null && recipeId != 0 && (boardNo == null || boardNo == 0));
		boolean isCommentReaction = (boardNo != null && boardNo != 0 && (recipeId == null || recipeId == 0));

		if (!isRecipeReaction && !isCommentReaction) {
			System.err.println("BoardService: processReaction - 레시피 또는 댓글 ID가 명확하지 않습니다. boardNo=" + boardNo
					+ ", recipeId=" + recipeId);
			return 0; // 유효하지 않은 요청
		}

		// 현재 사용자의 해당 board_no 또는 recipe_id에 대한 반응 상태를 확인
		// DAO의 getUserReactionType 메서드 시그니처에 맞춰 Integer 파라미터를 전달합니다.
		String existingReactionType = boardDao.getUserReactionType(boardNo, userId, recipeId);

		// LikeVo 객체 생성 및 값 설정
		LikeVo likeVo = new LikeVo();
		likeVo.setLike_id(userId); // 좋아요/싫어요 누른 사용자 ID

		// 중요: LikeVo에 board_no와 recipe_id를 명확히 설정합니다.
		// DDL의 CHK_ONE_TARGET 제약 조건에 따라 둘 중 하나는 반드시 NULL이어야 합니다.
		if (isRecipeReaction) {
			likeVo.setRecipe_id(recipeId);
			likeVo.setBoard_no(null); // 레시피 반응이므로 board_no는 NULL
		} else { // isCommentReaction
			likeVo.setBoard_no(boardNo);
			likeVo.setRecipe_id(null); // 댓글 반응이므로 recipe_id는 NULL
		}

		// 요청된 reactionType에 따라 로직 분기
		if ("LIKE".equalsIgnoreCase(reactionType)) {
			if (existingReactionType == null) {
				// 기존 반응 없음: 좋아요 추가
				likeVo.setLiked("1");
				likeVo.setDisliked("0");
				return boardDao.upsertReaction(likeVo);
			} else if ("LIKED".equalsIgnoreCase(existingReactionType)) {
				// 이미 '좋아요' 상태: 좋아요 취소 (삭제)
				return boardDao.deleteReaction(boardNo, userId, recipeId);
			} else if ("DISLIKED".equalsIgnoreCase(existingReactionType)) {
				// '싫어요' 상태: '싫어요' 취소 후 '좋아요'로 변경
				likeVo.setLiked("1");
				likeVo.setDisliked("0");
				return boardDao.upsertReaction(likeVo);
			}
		} else if ("DISLIKE".equalsIgnoreCase(reactionType)) {
			if (existingReactionType == null) {
				// 기존 반응 없음: 싫어요 추가
				likeVo.setLiked("0");
				likeVo.setDisliked("1");
				return boardDao.upsertReaction(likeVo);
			} else if ("DISLIKED".equalsIgnoreCase(existingReactionType)) {
				// 이미 '싫어요' 상태: 싫어요 취소 (삭제)
				return boardDao.deleteReaction(boardNo, userId, recipeId);
			} else if ("LIKED".equalsIgnoreCase(existingReactionType)) {
				// '좋아요' 상태: '좋아요' 취소 후 '싫어요'로 변경
				likeVo.setLiked("0");
				likeVo.setDisliked("1");
				return boardDao.upsertReaction(likeVo);
			}
		}
		// 유효하지 않은 reactionType이 전달되거나, 예상치 못한 상황 발생 시
		return 0;
	}

	/*
	 * 특정 레시피의 현재 좋아요 개수를 조회합니다.
	 */
	public int getRecipeLikeCount(int recipeId) {
		return boardDao.getRecipeLikeCount(recipeId);
	}

	/*
	 * 특정 레시피의 현재 싫어요 개수를 조회합니다.
	 */
	public int getRecipeDislikeCount(int recipeId) {
		return boardDao.getRecipeDislikeCount(recipeId);
	}

	/*
	 * 특정 댓글(게시글)의 현재 좋아요 개수를 조회합니다.
	 */
	public int getCommentLikeCount(int boardNo) {
		return boardDao.getCommentLikeCount(boardNo);
	}

	/*
	 * 특정 댓글(게시글)의 현재 싫어요 개수를 조회합니다.
	 */
	public int getCommentDislikeCount(int boardNo) {
		return boardDao.getCommentDislikeCount(boardNo);
	}

	/*
	 * 댓글 내용을 수정합니다.
	 *
	 * @param userid 로그인된 사용자 ID (수정 권한 확인용)
	 * 
	 * @param boardVo 수정할 댓글의 기존 정보 (board_no, 작성자 ID 등)
	 * 
	 * @param new_content 새로 변경할 댓글 내용
	 * 
	 * @return 업데이트된 행의 수
	 */
	public int updateComment(String userid, BoardVo boardVo, String new_content) {
		return boardDao.updateComment(userid, boardVo, new_content);
	}
}