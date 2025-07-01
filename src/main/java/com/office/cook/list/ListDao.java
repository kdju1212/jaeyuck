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
	public List<RecipeVo> getCookList(int offset, int pageSize) {
		String sql = "SELECT * FROM recipe ORDER BY recipe_id DESC LIMIT ? OFFSET ?";
		return jdbcTemplate.query(sql, new Object[] { pageSize, offset }, new BeanPropertyRowMapper<>(RecipeVo.class));
	}

	/*
	 * 레시피 갯수
	 */
	public int getTotalCookCount() {
		return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM recipe", Integer.class);
	}

	public RecipeVo getRecipeByIdAndName(int recipeId, String title) {
		String sql = "SELECT * FROM recipe WHERE recipe_id = ? AND title = ?";
		List<RecipeVo> list = jdbcTemplate.query(sql, new Object[] { recipeId, title },
				new BeanPropertyRowMapper<>(RecipeVo.class));
		return list.isEmpty() ? null : list.get(0);
	}

	public List<RecipeStepVo> getRecipeSteps(int recipeId) {
		String sql = "SELECT * FROM recipe_step WHERE recipe_id = ? ORDER BY step_order ASC";
		return jdbcTemplate.query(sql, new Object[] { recipeId }, new BeanPropertyRowMapper<>(RecipeStepVo.class));
	}

	public List<RecipeIngredientVo> getRecipeIngredients(int recipeId) {
		String sql = "SELECT * FROM recipe_ingredient WHERE recipe_id = ?";
		return jdbcTemplate.query(sql, new Object[] { recipeId },
				new BeanPropertyRowMapper<>(RecipeIngredientVo.class));
	}

	public int checkBookmarkExists(int recipeId, String userId) {
		String sql = "SELECT COUNT(*) FROM cookbookmark WHERE recipe_id = ? AND userid = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, recipeId, userId);
		return (count != null) ? count : 0;
	}

	public int insertBookmark(int recipeId, String userId) { // title, pageUrl 파라미터 제거
		String sql = "INSERT INTO cookbookmark (recipe_id, userid, bookmark_date) VALUES (?, ?, CURRENT_TIMESTAMP)";
		// SQL 쿼리도 ckg_nm, pageurl 컬럼 제거 및 bookmark_date 추가
		return jdbcTemplate.update(sql, recipeId, userId);
	}

	public int deleteBookmark(int recipeId, String userId) {
		String sql = "DELETE FROM cookbookmark WHERE recipe_id = ? AND userid = ?";
		return jdbcTemplate.update(sql, recipeId, userId);
	}

	public boolean isBookmarked(int recipeId, String userid) {
		String sql = "SELECT COUNT(*) FROM cookbookmark WHERE recipe_id = ? AND userid = ?";
		Integer count = jdbcTemplate.queryForObject(sql, Integer.class, recipeId, userid);
		return count != null && count > 0;
	}

	/*
	 * 조회수를 증가시키는 메서드
	 */
	public int incrementReadCount(int recipeId) {
		String sql = "UPDATE recipe SET read_count = read_count + 1 WHERE recipe_id = ?";
		// ⭐ jdbcTemplate.update()를 사용하여 SQL 쿼리를 실행합니다 ⭐
		// update() 메서드는 DML (INSERT, UPDATE, DELETE) 쿼리를 실행하고, 영향을 받은 행의 수를 반환합니다.
		return jdbcTemplate.update(sql, recipeId);
	}

	/*
	 * 조회수 상위 10개 가져오기
	 */
	public List<RecipeVo> getTopCooksByReadCount() {
		String sql = "SELECT * FROM recipe ORDER BY read_count DESC LIMIT 10";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecipeVo.class));
	}

	/*
	 * 북마크 데이터를 가져오는 메서드
	 */
	public List<RecipeVo> getBookmarks(String userid) {
		String sql = """
				    SELECT r.*
				    FROM cookbookmark b
				    JOIN recipe r ON b.cook_no = r.recipe_id
				    WHERE b.userid = ?
				    ORDER BY r.recipe_id DESC
				""";

		return jdbcTemplate.query(sql, new Object[] { userid }, new BeanPropertyRowMapper<>(RecipeVo.class));
	}

	/*
	 * 레시피 등록 처리
	 */
	// 1. 레시피 기본 정보 저장
	public int insertRecipe(RecipeVo recipe) {
		// ✨ 변경된 SQL: userid 컬럼 추가 ✨
		String sql = "INSERT INTO recipe (title, kind, tip, complete_img_url, userid) VALUES (?, ?, ?, ?, ?) RETURNING recipe_id";

		// ✨ 변경된 파라미터: recipe.getUserid() 추가 ✨
		return jdbcTemplate.queryForObject(sql, new Object[] { recipe.getTitle(), recipe.getKind(), recipe.getTip(),
				recipe.getCompleteImgUrl(), recipe.getUserid() // ✨ 여기에 userid 값을 추가합니다! ✨
		}, Integer.class);
	}

	/*
	 * 레시피 목록을 조회하는 메서드
	 */
	public List<RecipeVo> getRecipeList() {
		String sql = "SELECT * FROM recipe ORDER BY recipe_id DESC";
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(RecipeVo.class));
	}

	// 2. 재료 저장
	public void insertIngredient(RecipeIngredientVo ing) {
		String sql = "INSERT INTO recipe_ingredient (recipe_id, name, amount) VALUES (?, ?, ?)";
		jdbcTemplate.update(sql, ing.getRecipeId(), ing.getName(), ing.getAmount());
	}

	// 3. 단계 저장
	public void insertStep(RecipeStepVo step) {
		String sql = "INSERT INTO recipe_step (recipe_id, step_order, description, step_img_url) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(sql, step.getRecipeId(), step.getStepOrder(), step.getDescription(), step.getStepImgUrl());
	}

	public RecipeVo getRecipeById(int recipeId) {
		String sql = "SELECT * FROM recipe WHERE recipe_id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { recipeId }, new BeanPropertyRowMapper<>(RecipeVo.class));
	}

	public List<RecipeStepVo> getStepsByRecipeId(int recipeId) {
		String sql = "SELECT * FROM recipe_step WHERE recipe_id = ? ORDER BY step_order ASC";
		return jdbcTemplate.query(sql, new Object[] { recipeId }, new BeanPropertyRowMapper<>(RecipeStepVo.class));
	}

	public List<RecipeIngredientVo> getIngredientsByRecipeId(int recipeId) {
		String sql = "SELECT * FROM recipe_ingredient WHERE recipe_id = ?";
		return jdbcTemplate.query(sql, new Object[] { recipeId },
				new BeanPropertyRowMapper<>(RecipeIngredientVo.class));
	}

	public int deleteRecipeById(int recipeId) {
		String sql = "DELETE FROM recipe WHERE recipe_id = ?";
		return jdbcTemplate.update(sql, recipeId);
	}

	/*
	 * 레시피 수정
	 */
	public int updateRecipe(RecipeVo recipe) {
		String sql = "UPDATE recipe SET title = ?, kind = ?, tip = ?, complete_img_url = ? WHERE recipe_id = ?";
		return jdbcTemplate.update(sql, recipe.getTitle(), recipe.getKind(), recipe.getTip(),
				recipe.getCompleteImgUrl(), recipe.getRecipeId());
	}

	public void deleteIngredientsByRecipeId(int recipeId) {
		String sql = "DELETE FROM recipe_ingredient WHERE recipe_id = ?";
		jdbcTemplate.update(sql, recipeId);
	}

	public void deleteStepsByRecipeId(int recipeId) {
		String sql = "DELETE FROM recipe_step WHERE recipe_id = ?";
		jdbcTemplate.update(sql, recipeId);
	}

	/*
	 * 카테고리로 요리 찾기
	 */

	public List<RecipeVo> getRecipesByCategory(String category, int offset, int limit) {
		String sql = "SELECT * FROM recipe WHERE kind = ? ORDER BY recipe_id DESC LIMIT ? OFFSET ?";
		return jdbcTemplate.query(sql, new Object[] { category, limit, offset },
				new BeanPropertyRowMapper<>(RecipeVo.class));
	}

	/*
	 * 카테고리별 레시피의 총 개수를 가져오는 메서드
	 */
	public int getTotalRecipeCountByCategory(String category) {
		String sql = "SELECT COUNT(*) FROM recipe WHERE kind = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { category }, Integer.class);
	}

	/*
	 * 요리 이름 LIKE 검색
	 */
	public List<RecipeVo> searchCooksByName(String keyword) {
		String sql = "SELECT * FROM recipe WHERE title ILIKE ? ORDER BY recipe_id DESC";
		return jdbcTemplate.query(sql, new Object[] { "%" + keyword + "%" },
				new BeanPropertyRowMapper<>(RecipeVo.class));
	}
}
