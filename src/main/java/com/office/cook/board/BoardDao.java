package com.office.cook.board;

import com.office.cook.board.LikeVo; // LikeVo 패키지 확인
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
	 * 특정 레시피 ID에 대한 댓글 목록을 조회합니다. 페이지네이션 및 'liked'/'disliked' 컬럼을 사용하여 좋아요/싫어요 카운트를
	 * 포함합니다.
	 */
	public List<BoardVo> getCommentsByCookId(int recipeId, int offset, int pageSize) {
		String sql = """
				SELECT * FROM (
				    SELECT b.board_no, b.recipe_id, b.userid,
				           r.title AS ckg_nm, -- recipe 테이블에서 title을 ckg_nm으로 가져옴 (BoardVo에 매핑)
				           b.content,
				           COALESCE(SUM(CASE WHEN l.liked = '1' THEN 1 ELSE 0 END), 0) AS likeCount,
				           COALESCE(SUM(CASE WHEN l.disliked = '1' THEN 1 ELSE 0 END), 0) AS dislikeCount,
				           b.write_date,
				           ROW_NUMBER() OVER (ORDER BY b.write_date DESC) AS row_num
				    FROM cookboard b
				    LEFT JOIN recipe r ON b.recipe_id = r.recipe_id -- recipe 테이블 조인
				    LEFT JOIN cookboardlike l ON b.board_no = l.board_no AND l.recipe_id IS NULL -- 댓글 좋아요만 카운트
				    WHERE b.recipe_id = ?
				    GROUP BY b.board_no, b.recipe_id, b.userid, r.title, b.content, b.write_date
				) AS subquery
				WHERE row_num BETWEEN ? AND ?
				""";

		RowMapper<BoardVo> rowMapper = new BeanPropertyRowMapper<>(BoardVo.class);
		return jdbcTemplate.query(sql, new Object[] { recipeId, offset + 1, offset + pageSize }, rowMapper);
	}

	/*
	 * 특정 레시피 ID에 대한 총 댓글 개수를 조회합니다.
	 */
	public int getTotalCommentsCount(int recipeId) {
		String sql = "SELECT COUNT(*) FROM cookboard WHERE recipe_id = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, recipeId);
	}

	/*
	 * CookBoard 테이블에 새로운 댓글을 삽입합니다. BoardVo의 getRecipe_id() 메서드가 'recipe_id' 값을
	 * 제공한다고 가정합니다.
	 */
	public void insertComment(BoardVo comment) {
		// DDL에 따르면 cookboard 테이블에는 ckg_nm 컬럼이 없습니다.
		// ckg_nm은 recipe 테이블에서 가져와야 하므로, insert 시에는 필요 없습니다.
		String sql = "INSERT INTO CookBoard (board_no, recipe_id, userid, content, write_date) "
				+ "VALUES (nextval('cookboard_board_no_seq'), ?, ?, ?, CURRENT_TIMESTAMP)"; // PostgreSQL 시퀀스명 확인
		// PostgreSQL의 경우 시퀀스 이름은 보통 '테이블명_컬럼명_seq' 형태입니다.
		// `CREATE TABLE IF NOT EXISTS cookboard (board_no SERIAL PRIMARY KEY, ...)` 이라면
		// 자동으로 `cookboard_board_no_seq` 시퀀스가 생성됩니다.
		jdbcTemplate.update(sql, comment.getRecipe_id(), comment.getUserid(), comment.getContent());
	}

	/*
	 * 특정 사용자가 작성한 댓글 목록을 조회합니다.
	 */
	public List<BoardVo> myComment(String userid) {
		// cookboard 테이블에 CKG_NM 컬럼이 없으므로 recipe 테이블과 조인하여 가져와야 합니다.
		String sql = """
				SELECT cb.board_no, cb.recipe_id, cb.userid, r.title AS ckg_nm, cb.content, cb.write_date
				FROM cookboard cb
				LEFT JOIN recipe r ON cb.recipe_id = r.recipe_id
				WHERE cb.userid = ?
				ORDER BY cb.write_date DESC
				""";

		RowMapper<BoardVo> rowMapper = new BeanPropertyRowMapper<>(BoardVo.class);
		return jdbcTemplate.query(sql, rowMapper, userid);
	}

	/*
	 * 댓글을 삭제합니다. 'userid' 파라미터는 로그인한 사용자 ID이며, 댓글 소유권 확인에 사용됩니다.
	 */
	public int deleteComment(String userid, BoardVo boardVo) {
		String sql = "DELETE FROM cookboard WHERE board_no = ? AND userid = ?";
		int result = jdbcTemplate.update(sql, boardVo.getBoard_no(), userid);
		return result;
	}

	/**
	 * 사용자가 특정 게시글(board_no) 또는 레시피(recipe_id)에 대해 어떤 반응('liked' 또는 'disliked')을 했는지
	 * 확인합니다. * @param boardNo 댓글/게시글 번호 (댓글 반응인 경우 사용)
	 * 
	 * @param userId   좋아요/싫어요를 누른 사용자 ID (DB 스키마의 'like_id' 컬럼에 해당)
	 * @param recipeId 해당 반응이 레시피와 연관된 경우의 recipeId (레시피 반응인 경우 사용)
	 * @return 'LIKED', 'DISLIKED' 문자열 또는 반응이 없으면 null
	 */
	public String getUserReactionType(Integer boardNo, String userId, Integer recipeId) {
		// DDL의 CHK_ONE_TARGET 제약 조건에 따라 board_no와 recipe_id 중 하나만 값이 있고 다른 하나는 NULL이어야
		// 합니다.
		String sql;
		Object[] params;

		if (boardNo != null && boardNo != 0) { // 댓글 반응을 조회하는 경우
			sql = "SELECT CASE " + "WHEN liked = '1' THEN 'LIKED' " + "WHEN disliked = '1' THEN 'DISLIKED' "
					+ "ELSE NULL END " + "FROM CookBoardLike WHERE board_no = ? AND recipe_id IS NULL AND like_id = ?";
			params = new Object[] { boardNo, userId };
		} else if (recipeId != null && recipeId != 0) { // 레시피 반응을 조회하는 경우
			sql = "SELECT CASE " + "WHEN liked = '1' THEN 'LIKED' " + "WHEN disliked = '1' THEN 'DISLIKED' "
					+ "ELSE NULL END " + "FROM CookBoardLike WHERE recipe_id = ? AND board_no IS NULL AND like_id = ?";
			params = new Object[] { recipeId, userId };
		} else {
			// boardNo와 recipeId 둘 다 유효하지 않은 경우
			System.err.println("경고: getUserReactionType - 레시피 또는 댓글 반응을 명확히 구분할 수 없습니다. boardNo: " + boardNo
					+ ", recipeId: " + recipeId);
			return null;
		}

		try {
			return jdbcTemplate.queryForObject(sql, String.class, params);
		} catch (EmptyResultDataAccessException e) {
			return null; // 해당 반응이 존재하지 않음
		}
	}

	/**
	 * 좋아요/싫어요 반응을 삽입하거나 업데이트합니다 (Upsert). * @param likeVo 좋아요/싫어요 정보 (board_no,
	 * recipe_id, like_id, liked, disliked 등 포함)
	 * 
	 * @return 삽입 또는 업데이트된 행의 수 (일반적으로 1)
	 */
	public int upsertReaction(LikeVo likeVo) {
		// DDL의 CHK_ONE_TARGET 제약 조건에 따라 board_no와 recipe_id 중 하나만 값을 가져야 합니다.
		// LikeVo의 board_no와 recipe_id를 Integer 타입으로 변경했다면, 0 대신 null을 사용할 수 있습니다.
		boolean isRecipeReaction = (likeVo.getRecipe_id() != null && likeVo.getRecipe_id() != 0
				&& likeVo.getBoard_no() == null);
		boolean isCommentReaction = (likeVo.getBoard_no() != null && likeVo.getBoard_no() != 0
				&& likeVo.getRecipe_id() == null);

		if (!isRecipeReaction && !isCommentReaction) {
			System.err.println("경고: upsertReaction - 레시피 또는 댓글 반응을 명확히 구분할 수 없습니다. likeVo: " + likeVo);
			return 0;
		}

		// 기존 레코드 확인 SQL
		String checkSql;
		Object[] checkParams;
		if (isRecipeReaction) {
			checkSql = "SELECT COUNT(*) FROM CookBoardLike WHERE recipe_id = ? AND board_no IS NULL AND like_id = ?";
			checkParams = new Object[] { likeVo.getRecipe_id(), likeVo.getLike_id() };
		} else { // isCommentReaction
			checkSql = "SELECT COUNT(*) FROM CookBoardLike WHERE board_no = ? AND recipe_id IS NULL AND like_id = ?";
			checkParams = new Object[] { likeVo.getBoard_no(), likeVo.getLike_id() };
		}

		int count = jdbcTemplate.queryForObject(checkSql, Integer.class, checkParams);

		if (count > 0) {
			// 기존 레코드가 있으면 업데이트합니다.
			String updateSql;
			Object[] updateParams;

			if ("1".equals(likeVo.getLiked())) { // 좋아요를 누르는 경우
				updateSql = "UPDATE CookBoardLike SET liked = '1', disliked = '0', liked_date = CURRENT_TIMESTAMP "
						+ "WHERE like_id = ? AND ";
			} else { // 싫어요를 누르는 경우
				updateSql = "UPDATE CookBoardLike SET liked = '0', disliked = '1', liked_date = CURRENT_TIMESTAMP "
						+ "WHERE like_id = ? AND ";
			}

			if (isRecipeReaction) {
				updateSql += "recipe_id = ? AND board_no IS NULL";
				updateParams = new Object[] { likeVo.getLike_id(), likeVo.getRecipe_id() };
			} else { // isCommentReaction
				updateSql += "board_no = ? AND recipe_id IS NULL";
				updateParams = new Object[] { likeVo.getLike_id(), likeVo.getBoard_no() };
			}
			return jdbcTemplate.update(updateSql, updateParams);

		} else {
			// 기존 레코드가 없으면 삽입합니다.
			// DDL에 따라 ckg_nm, content, userid 컬럼은 CookBoardLike 테이블에서 제거되었습니다.
			String insertSql = "INSERT INTO CookBoardLike (board_no, recipe_id, like_id, liked, disliked, liked_date) "
					+ "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

			// board_no와 recipe_id 중 하나는 NULL (DB의 CHK_ONE_TARGET 제약 조건 만족)
			return jdbcTemplate.update(insertSql, likeVo.getBoard_no(), likeVo.getRecipe_id(), likeVo.getLike_id(),
					likeVo.getLiked(), likeVo.getDisliked());
		}
	}

	/**
	 * 기존 좋아요/싫어요 반응을 취소(삭제)합니다. * @param boardNo 댓글/게시글 번호 (댓글인 경우 사용)
	 * 
	 * @param userId   좋아요/싫어요를 취소하는 사용자 ID (DB 스키마의 'like_id' 컬럼에 해당)
	 * @param recipeId 관련된 레시피 ID (레시피인 경우 사용)
	 * @return 삭제된 행의 수 (일반적으로 1)
	 */
	public int deleteReaction(Integer boardNo, String userId, Integer recipeId) {
		String deleteSql = "DELETE FROM CookBoardLike WHERE like_id = ? AND ";
		Object[] deleteParams;

		if (recipeId != null && recipeId != 0 && (boardNo == null || boardNo == 0)) { // 레시피 반응 삭제
			deleteSql += "recipe_id = ? AND board_no IS NULL";
			deleteParams = new Object[] { userId, recipeId };
		} else if (boardNo != null && boardNo != 0 && (recipeId == null || recipeId == 0)) { // 댓글 반응 삭제
			deleteSql += "board_no = ? AND recipe_id IS NULL";
			deleteParams = new Object[] { userId, boardNo };
		} else {
			System.err.println("경고: deleteReaction - 레시피 또는 댓글 반응을 명확히 구분할 수 없습니다. boardNo: " + boardNo + ", recipeId: "
					+ recipeId);
			return 0; // 오류 처리
		}
		return jdbcTemplate.update(deleteSql, deleteParams);
	}

	/**
	 * 특정 레시피의 총 좋아요 개수를 조회합니다. * @param recipeId 레시피 번호
	 * 
	 * @return 좋아요 개수
	 */
	public int getRecipeLikeCount(int recipeId) {
		String sql = "SELECT COUNT(*) FROM CookBoardLike WHERE recipe_id = ? AND board_no IS NULL AND liked = '1'";
		return jdbcTemplate.queryForObject(sql, Integer.class, recipeId);
	}

	/**
	 * 특정 레시피의 총 싫어요 개수를 조회합니다. * @param recipeId 레시피 번호
	 * 
	 * @return 싫어요 개수
	 */
	public int getRecipeDislikeCount(int recipeId) {
		String sql = "SELECT COUNT(*) FROM CookBoardLike WHERE recipe_id = ? AND board_no IS NULL AND disliked = '1'";
		return jdbcTemplate.queryForObject(sql, Integer.class, recipeId);
	}

	/**
	 * 특정 댓글(게시글)의 총 좋아요 개수를 조회합니다. * @param boardNo 댓글/게시글 번호
	 * 
	 * @return 좋아요 개수
	 */
	public int getCommentLikeCount(int boardNo) {
		String sql = "SELECT COUNT(*) FROM CookBoardLike WHERE board_no = ? AND recipe_id IS NULL AND liked = '1'";
		return jdbcTemplate.queryForObject(sql, Integer.class, boardNo);
	}

	/**
	 * 특정 댓글(게시글)의 총 싫어요 개수를 조회합니다. * @param boardNo 댓글/게시글 번호
	 * 
	 * @return 싫어요 개수
	 */
	public int getCommentDislikeCount(int boardNo) {
		String sql = "SELECT COUNT(*) FROM CookBoardLike WHERE board_no = ? AND recipe_id IS NULL AND disliked = '1'";
		return jdbcTemplate.queryForObject(sql, Integer.class, boardNo);
	}

	/*
	 * 댓글 내용을 수정합니다. 'userid' 파라미터는 로그인한 사용자 ID이며, 댓글 소유권 확인에 사용됩니다.
	 */
	public int updateComment(String userid, BoardVo boardVo, String new_content) {
		String sql = "UPDATE cookboard SET content = ? WHERE board_no = ? AND userid = ?";
		int result = jdbcTemplate.update(sql, new_content, boardVo.getBoard_no(), userid);
		return result;
	}
}