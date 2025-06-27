package com.office.cook.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

	@Autowired
	BoardDao boardDao;

	/*
	 * 특정 요리 이름에 대한 댓글 목록을 조회
	 */
//	public List<BoardVo> getCommentsByCookName(String cookName, int cook_no, int offset, int pageSize) {
//		// 댓글 목록을 페이지네이션 적용해서 가져오기
//		return boardDao.getCommentsByCookName(cookName, cook_no, offset, pageSize);
//	}
	public List<BoardVo> getCommentsByCookId(int cook_no, int offset, int pageSize) {
		// 댓글 목록을 페이지네이션 적용해서 가져오기
		return boardDao.getCommentsByCookId(cook_no, offset, pageSize);
	}

	/*
	 * 댓글 개수
	 */
//	public int getTotalCommentsCount(String cookName, int cook_no) {
//
//		return boardDao.getTotalCommentsCount(cookName, cook_no);
//	}
	public int getTotalCommentsCount(int cook_no) {

		return boardDao.getTotalCommentsCount(cook_no);
	}

	/*
	 * 댓글 추가
	 */
	public void addComment(BoardVo comment) {
		boardDao.insertComment(comment);
	}

	/*
	 * 나의 댓글보기
	 */
	public List<BoardVo> myComment(String userid) {

		return boardDao.myComment(userid);
	}

	/*
	 * 댓글 삭제
	 */
	public int deleteComment(String userid, BoardVo boardVo) {
		// userid는 로그인되어있는 userid
		// boardVo는 댓글 작성한 userid
		return boardDao.deleteComment(userid, boardVo);
	}

	/*
	 * 댓글 좋아요
	 */
	public int likeComment(LikeVo likeVo) {

		return boardDao.likeComment(likeVo);
	}

	/*
	 * 댓글 싫어요
	 */
	public int dislikeComment(LikeVo likeVo) {

		return boardDao.dislikeComment(likeVo);
	}

	/*
	 * 댓글 수정
	 */
	public int updateComment(String userid, BoardVo boardVo, String new_content) {

		return boardDao.updateComment(userid, boardVo, new_content);
	}
}
