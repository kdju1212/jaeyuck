package com.office.cook.list;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ListDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	/*
	 * 요리 목록
	 */
	public List<ListVo> getCookList(int page, int pageSize) {
		// 페이지네이션 범위 계산
		int startRow = (page - 1) * pageSize + 1;
		int endRow = page * pageSize;

		// 페이지네이션 쿼리
		String sql = "SELECT cook_no, CKG_NM, CKG_IMG_URL, read_count " + "FROM ("
				+ "    SELECT cook_no, CKG_NM, CKG_IMG_URL, read_count, "
				+ "           ROW_NUMBER() OVER (ORDER BY cook_no) AS RN " + "    FROM cooks" + ") "
				+ "WHERE RN BETWEEN ? AND ?";

		// BeanPropertyRowMapper를 사용하여 결과를 ListVo 객체로 자동 매핑
		return jdbcTemplate.query(sql, new Object[] { startRow, endRow }, new BeanPropertyRowMapper<>(ListVo.class));
	}

	/*
	 * 레시피 갯수
	 */
	public int getTotalCookCount() {
		String sql = "SELECT COUNT(*) FROM cooks";
		return jdbcTemplate.queryForObject(sql, Integer.class);
	}

	/*
	 * 요리 이름으로 상세 정보를 가져오는 메서드
	 */
	public ListVo getCookByName(int cook_no, String cookName) {
		String sql = "SELECT * FROM cooks WHERE cook_no = ? AND CKG_NM = ?";
		try {
			// 항상 하나의 결과를 받아옴
			return jdbcTemplate.queryForObject(sql, new Object[] { cook_no, cookName }, (rs, rowNum) -> {
				ListVo listVo = new ListVo();
				listVo.setCook_no(rs.getInt("cook_no"));
				listVo.setCKG_NM(rs.getString("CKG_NM"));
				listVo.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
				listVo.setCKG_KND_ACTO_NM(rs.getString("CKG_KND_ACTO_NM"));
				listVo.setCKG_MTRL_CN(rs.getString("CKG_MTRL_CN"));
				listVo.setCKG_INBUN_NM(rs.getString("CKG_INBUN_NM"));
				listVo.setCKG_TIME_NM(rs.getString("CKG_TIME_NM"));
				listVo.setRead_count(rs.getInt("read_count"));
				listVo.setStep1(rs.getString("step1"));
				listVo.setStep2(rs.getString("step2"));
				listVo.setStep3(rs.getString("step3"));
				listVo.setStep4(rs.getString("step4"));
				listVo.setStep5(rs.getString("step5"));
				listVo.setStep6(rs.getString("step6"));
				listVo.setStep7(rs.getString("step7"));
				listVo.setStep8(rs.getString("step8"));
				listVo.setStep9(rs.getString("step9"));
				listVo.setStep10(rs.getString("step10"));
				listVo.setStep11(rs.getString("step11"));
				listVo.setStep12(rs.getString("step12"));
				listVo.setStep13(rs.getString("step13"));
				listVo.setStep14(rs.getString("step14"));
				listVo.setStep15(rs.getString("step15"));
				listVo.setStep16(rs.getString("step16"));
				listVo.setStep17(rs.getString("step17"));
				listVo.setStep18(rs.getString("step18"));
				listVo.setStep19(rs.getString("step19"));
				listVo.setStep20(rs.getString("step20"));
				return listVo;
			});
		} catch (EmptyResultDataAccessException e) {
			// 이 경우에는 예외가 발생하지 않아야 한다는 것을 알고 있음
			// 예외가 발생하는 것은 데이터가 없을 때만 발생하므로, 예외 처리 로직은 필요 없을 수도 있음
			// 디버깅을 위해 로그를 찍거나, 예외 처리를 더 상세하게 할 수 있습니다.
			throw new RuntimeException("No matching cook found for name: " + cookName, e);
		}
	}

	/*
	 * 북마크
	 */
	public int BookMark(String pageURL, String userid, String CKG_NM, int cook_no) {

		// 먼저, 해당 userid와 CKG_NM에 해당하는 북마크가 이미 존재하는지 확인
		String checkSql = "SELECT COUNT(*) FROM CookBookMark WHERE userid = ? AND CKG_NM = ? AND cook_no = ?";
		int count = 0;

		try {
			count = jdbcTemplate.queryForObject(checkSql, new Object[] { userid, CKG_NM, cook_no }, Integer.class);
		} catch (EmptyResultDataAccessException e) {
			// 결과가 없으면 count는 0
			count = 0;
		}

		// 북마크가 이미 존재하는 경우, 삭제 처리
		if (count > 0) {
			// 이미 북마크가 존재하면 삭제
			String deleteSql = "DELETE FROM CookBookMark WHERE userid = ? AND CKG_NM = ? AND cook_no = ?";
			int rowsAffected = jdbcTemplate.update(deleteSql, userid, CKG_NM, cook_no);

			// 삭제 성공 시 -1 반환 (삭제된 북마크가 존재한 경우)
			if (rowsAffected > 0) {
				return -1; // -1: 북마크 삭제 성공
			}
			return 0; // 삭제 실패
		}

		// 북마크가 존재하지 않으면 새로운 북마크 추가
		String insertSql = "INSERT INTO CookBookMark (pageURL, userid, CKG_NM, cook_no) VALUES (?, ?, ?, ?)";
		int rowsAffected = jdbcTemplate.update(insertSql, pageURL, userid, CKG_NM, cook_no);

		// 추가가 성공하면 1을 반환 (새로운 북마크가 추가된 경우)
		if (rowsAffected > 0) {
			return 1; // 1: 북마크 추가 성공
		}

		return 0; // 실패한 경우 0 반환
	}

	/*
	 * 북마크 존재 여부
	 */
	public boolean isBookmarked(int cook_no, String userid) {
		String sql = "SELECT COUNT(*) FROM CookBookmark WHERE cook_no = ? AND userid = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, cook_no, userid);
		return count != null && count > 0;
	}

	/*
	 * 조회수를 증가시키는 메서드
	 */
	public void incrementReadCount(String cookName, int cook_no) {
		String sql = "UPDATE cooks SET read_count = read_count + 1 WHERE CKG_NM = ? AND cook_no = ?";
		jdbcTemplate.update(sql, cookName, cook_no);
	}

	/*
	 * 조회수 상위 10개 가져오기
	 */
	public List<ListVo> getTopCooksByReadCount() {
		String sql = "SELECT cook_no, CKG_NM, CKG_IMG_URL, READ_COUNT FROM (SELECT cook_no, CKG_NM, CKG_IMG_URL, READ_COUNT FROM cooks ORDER BY READ_COUNT DESC) WHERE ROWNUM <= 10";
		return jdbcTemplate.query(sql, (rs, rowNum) -> {
			ListVo cook = new ListVo();
			cook.setCook_no(rs.getInt("cook_no"));
			cook.setCKG_NM(rs.getString("CKG_NM"));
			cook.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
			cook.setRead_count(rs.getInt("READ_COUNT"));
			return cook;
		});
	}

	/*
	 * 요리 이름 LIKE 검색
	 */
	public List<ListVo> searchCooksByName(String word) {
		String sql = "SELECT cook_no, CKG_NM, CKG_IMG_URL, READ_COUNT FROM cooks WHERE CKG_NM LIKE ?";
		return jdbcTemplate.query(sql, new Object[] { "%" + word + "%" }, (rs, rowNum) -> {
			ListVo cook = new ListVo();
			cook.setCook_no(rs.getInt("cook_no"));
			cook.setCKG_NM(rs.getString("CKG_NM"));
			cook.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
			cook.setRead_count(rs.getInt("READ_COUNT"));
			return cook;
		});
	}

	public List<ListVo> findBookmarksByUserId(String userId) {
		String sql = "SELECT c.* " + "FROM cooks c "
				+ "JOIN cookbookmark cb ON c.CKG_NM = cb.CKG_NM AND c.cook_no = cb.cook_no" + "WHERE cb.USERID = ?";
		return jdbcTemplate.query(sql, new Object[] { userId }, new RowMapper<ListVo>() {
			@Override
			public ListVo mapRow(ResultSet rs, int rowNum) throws SQLException {
				ListVo cook = new ListVo();
				cook.setCook_no(rs.getInt("cook_no"));
				cook.setCKG_NM(rs.getString("CKG_NM"));
				cook.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
				return cook;
			}
		});
	}

	/*
	 * 북마크 데이터를 가져오는 메서드
	 */
	public List<ListVo> getBookmarks(String userId) {
		String sql = "SELECT c.cook_no, c.CKG_NM, c.CKG_IMG_URL ,c.read_count " + "FROM cookbookmark cb "
				+ "JOIN cooks c ON cb.CKG_NM = c.CKG_NM AND c.cook_no = cb.cook_no " + "WHERE cb.userid = ?";

		return jdbcTemplate.query(sql, new Object[] { userId }, new RowMapper<ListVo>() {
			@Override
			public ListVo mapRow(ResultSet rs, int rowNum) throws SQLException {
				ListVo listVo = new ListVo();
				listVo.setCook_no(rs.getInt("cook_no"));
				listVo.setCKG_NM(rs.getString("CKG_NM"));
				listVo.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
				listVo.setRead_count(rs.getInt("read_count"));
				
				return listVo;
			}
		});
	}

	/*
	 * 레시피 등록 처리
	 */
	public void insertRecipe(ListVo recipe) {
		String sql = "INSERT INTO CookRecipe ("
				+ "recipe_no, CKG_NM, CKG_KND_ACTO_NM, CKG_MTRL_CN, CKG_INBUN_NM, CKG_TIME_NM, CKG_IMG_URL, "
				+ "step1, step2, step3, step4, step5, step6, step7, step8, step9, "
				+ "step10, step11, step12, step13, step14, step15, step16, step17, step18, " + "step19, step20) "
				+ "VALUES (cookRecipe_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		jdbcTemplate.update(sql, recipe.getCKG_NM(), recipe.getCKG_KND_ACTO_NM(), recipe.getCKG_MTRL_CN(),
				recipe.getCKG_INBUN_NM(), recipe.getCKG_TIME_NM(), recipe.getCKG_IMG_URL(), recipe.getStep1(),
				recipe.getStep2(), recipe.getStep3(), recipe.getStep4(), recipe.getStep5(), recipe.getStep6(),
				recipe.getStep7(), recipe.getStep8(), recipe.getStep9(), recipe.getStep10(), recipe.getStep11(),
				recipe.getStep12(), recipe.getStep13(), recipe.getStep14(), recipe.getStep15(), recipe.getStep16(),
				recipe.getStep17(), recipe.getStep18(), recipe.getStep19(), recipe.getStep20());
	}

	/*
	 * 레시피 목록을 조회하는 메서드
	 */
	public List<ListVo> getRecipeList() {
		String sql = "SELECT recipe_no, CKG_NM, CKG_IMG_URL FROM cookrecipe";

		// RowMapper를 사용하여 결과를 ListVO 객체로 변환
		return jdbcTemplate.query(sql, new RowMapper<ListVo>() {
			@Override
			public ListVo mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
				ListVo recipe = new ListVo();
				recipe.setRecipe_no(rs.getInt("recipe_no"));
				recipe.setCKG_NM(rs.getString("CKG_NM"));
				recipe.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
				return recipe;
			}
		});
	}

	/*
	 * 요리 이름으로 상세 정보를 가져오는 메서드
	 */
	public ListVo getRecipeByName(String cookName, int recipe_no) {
		String sql = "SELECT * FROM cookrecipe WHERE CKG_NM = ? AND recipe_no = ?";
		try {
			// 항상 하나의 결과를 받아옴
			return jdbcTemplate.queryForObject(sql, new Object[] { cookName, recipe_no }, (rs, rowNum) -> {
				ListVo listVo = new ListVo();
				listVo.setRecipe_no(rs.getInt("recipe_no"));
				listVo.setCKG_NM(rs.getString("CKG_NM"));
				listVo.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
				listVo.setCKG_KND_ACTO_NM(rs.getString("CKG_KND_ACTO_NM"));
				listVo.setCKG_MTRL_CN(rs.getString("CKG_MTRL_CN"));
				listVo.setCKG_INBUN_NM(rs.getString("CKG_INBUN_NM"));
				listVo.setCKG_TIME_NM(rs.getString("CKG_TIME_NM"));
				listVo.setStep1(rs.getString("step1"));
				listVo.setStep2(rs.getString("step2"));
				listVo.setStep3(rs.getString("step3"));
				listVo.setStep4(rs.getString("step4"));
				listVo.setStep5(rs.getString("step5"));
				listVo.setStep6(rs.getString("step6"));
				listVo.setStep7(rs.getString("step7"));
				listVo.setStep8(rs.getString("step8"));
				listVo.setStep9(rs.getString("step9"));
				listVo.setStep10(rs.getString("step10"));
				listVo.setStep11(rs.getString("step11"));
				listVo.setStep12(rs.getString("step12"));
				listVo.setStep13(rs.getString("step13"));
				listVo.setStep14(rs.getString("step14"));
				listVo.setStep15(rs.getString("step15"));
				listVo.setStep16(rs.getString("step16"));
				listVo.setStep17(rs.getString("step17"));
				listVo.setStep18(rs.getString("step18"));
				listVo.setStep19(rs.getString("step19"));
				listVo.setStep20(rs.getString("step20"));
				return listVo;
			});
		} catch (EmptyResultDataAccessException e) {
			// 이 경우에는 예외가 발생하지 않아야 한다는 것을 알고 있음
			// 예외가 발생하는 것은 데이터가 없을 때만 발생하므로, 예외 처리 로직은 필요 없을 수도 있음
			// 디버깅을 위해 로그를 찍거나, 예외 처리를 더 상세하게 할 수 있습니다.
			throw new RuntimeException("No matching cook found for name: " + cookName, e);
		}
	}

	/*
	 * 레시피 등록 처리
	 */
	public void adminInsertRecipe(ListVo recipe) {
		String sql = "INSERT INTO Cooks ("
				+ "cook_no, CKG_NM, CKG_KND_ACTO_NM, CKG_MTRL_CN, CKG_INBUN_NM, CKG_TIME_NM, CKG_IMG_URL, "
				+ "step1, step2, step3, step4, step5, step6, step7, step8, step9, "
				+ "step10, step11, step12, step13, step14, step15, step16, step17, step18, step19, step20) "
				+ "VALUES (cook_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		int result = jdbcTemplate.update(sql, recipe.getCKG_NM(), recipe.getCKG_KND_ACTO_NM(), recipe.getCKG_MTRL_CN(),
				recipe.getCKG_INBUN_NM(), recipe.getCKG_TIME_NM(), recipe.getCKG_IMG_URL(), recipe.getStep1(),
				recipe.getStep2(), recipe.getStep3(), recipe.getStep4(), recipe.getStep5(), recipe.getStep6(),
				recipe.getStep7(), recipe.getStep8(), recipe.getStep9(), recipe.getStep10(), recipe.getStep11(),
				recipe.getStep12(), recipe.getStep13(), recipe.getStep14(), recipe.getStep15(), recipe.getStep16(),
				recipe.getStep17(), recipe.getStep18(), recipe.getStep19(), recipe.getStep20());

		// 레시피 등록 성공 시, cookrecipe에서 해당 CKG_NM에 대한 데이터를 삭제
		if (result > 0) {
			String deleteSql = "DELETE FROM cookrecipe WHERE CKG_NM = ? AND recipe_no = ?";
			jdbcTemplate.update(deleteSql, recipe.getCKG_NM(), recipe.getRecipe_no());
		} else {
			System.out.println("레시피 등록 실패: " + result); // 실패 원인 확인을 위한 로그 추가
		}
	}

	/*
	 * 레시피 등록 페이지에서 삭제
	 */
	public boolean deleteRecipeByName(String cookName, int recipe_no) {
		String sql = "DELETE FROM cookrecipe WHERE CKG_NM = ? AND recipe_no = ?";

		try {
			int rowsAffected = jdbcTemplate.update(sql, cookName, recipe_no);
			return rowsAffected > 0; // 삭제된 행이 있으면 true 반환
		} catch (Exception e) {
			e.printStackTrace();
			return false; // 예외 발생 시 false 반환
		}
	}

	/*
	 * 레시피 상세 정보에서 삭제
	 */
	public int deleteCookByName(int cook_no, String cookName) {
		String sql = "DELETE FROM Cooks WHERE cook_no = ? AND CKG_NM = ?";
		return jdbcTemplate.update(sql, cook_no, cookName);
	}

	/*
	 * 레시피 수정
	 */
	public int updateCooks(String oldName, ListVo cook) {
		String sql = "UPDATE cooks SET CKG_NM = ?, CKG_IMG_URL = ?, CKG_INBUN_NM = ?, CKG_TIME_NM = ?, CKG_KND_ACTO_NM = ?, "
				+ "CKG_MTRL_CN = ?, step1 = ?, step2 = ?, step3 = ?, step4 = ?, step5 = ?, step6 = ?, step7 = ?, step8 = ?, "
				+ "step9 = ?, " + "step10 = ?, " + "step11 = ?, " + "step12 = ?, " + "step13 = ?, " + "step14 = ?, "
				+ "step15 = ?, " + "step16 = ?, " + "step17 = ?, " + "step18 = ?, " + "step19 = ?, " + "step20 = ? "
				+ "WHERE cook_no = ? AND CKG_NM = ?";
		return jdbcTemplate.update(sql, cook.getCKG_NM(), cook.getCKG_IMG_URL(), cook.getCKG_INBUN_NM(),
				cook.getCKG_TIME_NM(), cook.getCKG_KND_ACTO_NM(), cook.getCKG_MTRL_CN(), cook.getStep1(),
				cook.getStep2(), cook.getStep3(), cook.getStep4(), cook.getStep5(), cook.getStep6(), cook.getStep7(),
				cook.getStep8(), cook.getStep9(), cook.getStep10(), cook.getStep11(), cook.getStep12(),
				cook.getStep13(), cook.getStep14(), cook.getStep15(), cook.getStep16(), cook.getStep17(),
				cook.getStep18(), cook.getStep19(), cook.getStep20(), cook.getCook_no(), oldName);
	}

	/*
	 * 카테고리로 요리 찾기
	 */
	public List<ListVo> findCooksByCategoryWithPagination(String category, int startRow, int endRow) {
		// SQL 쿼리 작성
		String sql = "SELECT cook_no, CKG_NM, CKG_IMG_URL, read_count " + "FROM ("
				+ "    SELECT cook_no, CKG_NM, CKG_IMG_URL, read_count, "
				+ "           ROW_NUMBER() OVER (ORDER BY cook_no) AS RN " + "    FROM cooks "
				+ "    WHERE CKG_KND_ACTO_NM = ? " + ") " + "WHERE RN BETWEEN ? AND ?";

		// 쿼리 실행
		return jdbcTemplate.query(sql, new Object[] { category, startRow, endRow }, new RowMapper<ListVo>() {
			@Override
			public ListVo mapRow(ResultSet rs, int rowNum) throws SQLException {
				ListVo listVo = new ListVo();
				listVo.setCook_no(rs.getInt("cook_no"));
				listVo.setCKG_NM(rs.getString("CKG_NM"));
				listVo.setCKG_IMG_URL(rs.getString("CKG_IMG_URL"));
				listVo.setRead_count(rs.getInt("read_count"));
				return listVo;
			}
		});
	}

	/*
	 * 카테고리별 레시피의 총 개수를 가져오는 메서드
	 */
	public int findTotalCookCountByCategory(String category) {
		String sql = "SELECT COUNT(*) FROM cooks WHERE CKG_KND_ACTO_NM = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { category }, Integer.class);
	}
}
