package com.office.cook.board;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class BoardDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	/*
	 * 요리이름으로 댓글 찾기
	 */
	public List<BoardVo> getCommentsByCookName(String cookName, int cook_no, int offset, int pageSize) {
		// 페이지네이션과 좋아요/싫어요 카운트가 포함된 SQL 쿼리
		String sql = "SELECT * FROM (" + "  SELECT b.board_no, b.cook_no, b.userid, b.CKG_NM, b.content, "
				+ "    COUNT(CASE WHEN l.liked = '1' THEN 1 END) AS like_count, "
				+ "    COUNT(CASE WHEN l.disliked = '1' THEN 1 END) AS dislike_count, " + "    b.write_date, "
				+ "    ROW_NUMBER() OVER (ORDER BY b.write_date DESC) AS row_num " + "  FROM CookBoard b "
				+ "  LEFT JOIN CookBoardLike l ON b.board_no = l.board_no AND b.CKG_NM = l.CKG_NM "
				+ "  WHERE b.CKG_NM = ? AND b.cook_no = ? "
				+ "  GROUP BY b.board_no, b.cook_no, b.userid, b.CKG_NM, b.content, b.write_date "
				+ ") WHERE row_num BETWEEN ? AND ?";

		// RowMapper 설정
		RowMapper<BoardVo> rowMapper = new BeanPropertyRowMapper<>(BoardVo.class);

		// 쿼리 실행: 댓글 목록 가져오기
		return jdbcTemplate.query(sql, new Object[] { cookName, cook_no, offset + 1, offset + pageSize }, rowMapper);
	}

	/*
	 * 댓글 개수
	 */
	public int getTotalCommentsCount(String cookName, int cook_no) {
		// 올바른 SQL 쿼리: COUNT(*) 사용
		String sql = "SELECT COUNT(*) FROM cookboard WHERE cook_no = ? AND CKG_NM = ?";

		// queryForObject를 사용하여 단일 값(카운트)을 가져옴
		int result = jdbcTemplate.queryForObject(sql, Integer.class, cook_no, cookName);

		return result;
	}

	/*
	 * 댓글을 추가하는 쿼리
	 */
	public void insertComment(BoardVo comment) {
		String sql = "INSERT INTO CookBoard (board_no, cook_no, CKG_NM, userid, content, write_date) VALUES (cookBoard_seq.NEXTVAL, ?, ?, ?, ?, SYSDATE)";
		jdbcTemplate.update(sql, comment.getCook_no(), comment.getCKG_NM(), comment.getUserid(), comment.getContent());
	}

	/*
	 * 내가 쓴 댓글 가져오기
	 */
	public List<BoardVo> myComment(String userid) {
		// SQL 쿼리 정의, 각 키워드 사이에 공백 추가
		String sql = "SELECT cb.cook_no, cb.userid, cb.CKG_NM, cb.content, cb.write_date " + "FROM cookboard cb "
				+ "JOIN cooks c ON cb.CKG_NM = c.CKG_NM AND c.cook_no = cb.cook_no " + "WHERE cb.userid = ?";

		// RowMapper를 사용하여 cookboard 테이블의 데이터를 BoardVo 객체로 매핑
		RowMapper<BoardVo> rowMapper = new BeanPropertyRowMapper<>(BoardVo.class);

		// jdbcTemplate을 사용하여 SQL 쿼리를 실행하고 결과를 List<BoardVo>로 반환
		List<BoardVo> comments = jdbcTemplate.query(sql, rowMapper, userid);

		return comments; // 조회된 댓글 리스트 반환
	}

	/*
	 * 댓글 삭제
	 */
	public int deleteComment(String userid, BoardVo boardVo) {
		// userid는 로그인 되어있는 id
		// boardVo는 작성된 댓글
		String sql = "delete cookboard where board_no = ? and cook_no = ? and CKG_NM = ? and userid = ? and content = ?";

		int result = jdbcTemplate.update(sql, boardVo.getBoard_no(), boardVo.getCook_no(), boardVo.getCKG_NM(),
				boardVo.getUserid(), boardVo.getContent());

		return result;
	}

	/*
	 * 댓글 좋아요
	 */
	public int likeComment(LikeVo likeVo) {
		String checkSql = "SELECT COUNT(*) FROM CookBoardLike WHERE board_no = ? and userid = ? AND CKG_NM = ? and like_id = ? and liked = 1";
		// String checkSql = "select count(*) from cookboardLike where board_no = ?";
		int count = 0;

		try {
			count = jdbcTemplate.queryForObject(checkSql,
					new Object[] { likeVo.getBoard_no(), likeVo.getUserid(), likeVo.getCKG_NM(), likeVo.getLike_id() },
					Integer.class);
		} catch (EmptyResultDataAccessException e) {
			// 결과가 없으면 count는 0
			count = 0;
		}

		// 북마크가 이미 존재하는 경우, 삭제 처리
		if (count > 0) {
			// 이미 좋아요가 존재하면 삭제
			String deleteSql = "DELETE FROM CookBoardLike WHERE board_no = ? and userid = ? AND CKG_NM = ? and like_id = ? and liked = 1";
			int rowsAffected = jdbcTemplate.update(deleteSql, likeVo.getBoard_no(), likeVo.getUserid(),
					likeVo.getCKG_NM(), likeVo.getLike_id());

			// 삭제 성공 시 -1 반환
			if (rowsAffected > 0) {
				return -1; // -1: 좋아요 삭제 성공
			}
			return 0; // 삭제 실패
		}

		// 좋아요가 없으면 생성
		String insertSql = "INSERT INTO CookBoardLike (board_no,CKG_NM, content, userid , Like_id,Liked,liked_date) VALUES (?,?, ?, ?,?,1,SYSDATE)";
		int rowsAffected = jdbcTemplate.update(insertSql, likeVo.getBoard_no(), likeVo.getCKG_NM(), likeVo.getContent(),
				likeVo.getUserid(), likeVo.getLike_id());

		// 추가가 성공하면 1을 반환
		if (rowsAffected > 0) {
			return 1; // 1: 좋아요 성공
		}

		return 0; // 실패한 경우 0 반환
	}

	/*
	 * 댓글 싫어요
	 */
	public int dislikeComment(LikeVo likeVo) {

		String checkSql = "SELECT COUNT(*) FROM CookBoardLike WHERE board_no = ? and userid = ? AND CKG_NM = ? and like_id = ? and disliked = 1";

		int count = 0;

		try {
			count = jdbcTemplate.queryForObject(checkSql,
					new Object[] { likeVo.getBoard_no(), likeVo.getUserid(), likeVo.getCKG_NM(), likeVo.getLike_id() },
					Integer.class);
		} catch (EmptyResultDataAccessException e) {
			// 결과가 없으면 count는 0
			count = 0;
		}

		// 북마크가 이미 존재하는 경우, 삭제 처리
		if (count > 0) {
			// 이미 좋아요가 존재하면 삭제
			String deleteSql = "DELETE FROM CookBoardLike WHERE board_no = ? and userid = ? AND CKG_NM = ? and like_id = ? and disliked = 1";
			int rowsAffected = jdbcTemplate.update(deleteSql, likeVo.getBoard_no(), likeVo.getUserid(),
					likeVo.getCKG_NM(), likeVo.getLike_id());

			// 삭제 성공 시 -1 반환
			if (rowsAffected > 0) {
				return -1; // -1: 좋아요 삭제 성공
			}
			return 0; // 삭제 실패
		}

		// 싫어요가 없으면 생성
		String insertSql = "INSERT INTO CookBoardLike (board_no ,CKG_NM, content, userid , Like_id,disLiked,liked_date) VALUES (?,?, ?, ?,?,1,SYSDATE)";
		int rowsAffected = jdbcTemplate.update(insertSql, likeVo.getBoard_no(), likeVo.getCKG_NM(), likeVo.getContent(),
				likeVo.getUserid(), likeVo.getLike_id());

		// 추가가 성공하면 1을 반환
		if (rowsAffected > 0) {
			return 1; // 1: 좋아요 성공
		}

		return 0; // 실패한 경우 0 반환
	}

	/*
	 * 댓글 수정
	 */
	public int updateComment(String userid, BoardVo boardVo, String new_content) {

		// userid는 로그인 되어있는 id
		// boardVo는 작성된 댓글

		String sql = "update cookboard set content = ? where board_no =? and cook_no = ? and CKG_NM = ? and userid = ? and content = ?";

		int result = jdbcTemplate.update(sql, new_content, boardVo.getBoard_no(), boardVo.getCook_no(),
				boardVo.getCKG_NM(), boardVo.getUserid(), boardVo.getContent());

		return result;

	}
}
