package com.office.cook.list;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Repository
public class ListDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	private Cloudinary cloudinary;

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

	// 3. 단계 저장

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

	public int deleteRecipeComments(int recipeId) {
		String sql = "DELETE FROM cookboard WHERE recipe_id = ?"; // ckg_no가 recipe_id라고 가정
		return jdbcTemplate.update(sql, recipeId);
	}

	public int deleteRecipeLikes(int recipeId) {
		String sql = "DELETE FROM cookboardlike WHERE recipe_id = ?";
		return jdbcTemplate.update(sql, recipeId);
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

	/* 레시피 수정 */
	private RowMapper<RecipeVo> recipeRowMapper = new RowMapper<RecipeVo>() {
		@Override
		public RecipeVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			RecipeVo recipe = new RecipeVo();
			recipe.setRecipeId(rs.getInt("recipe_id"));
			recipe.setTitle(rs.getString("title"));
			recipe.setKind(rs.getString("kind"));
			// recipe.setDifficulty(rs.getString("difficulty")); // ⭐ 스키마에 difficulty 컬럼이
			// 없으므로 제외
			recipe.setTip(rs.getString("tip"));
			recipe.setCompleteImgUrl(rs.getString("complete_img_url"));
			recipe.setRead_count(rs.getInt("read_count"));
			recipe.setCreatedAt(rs.getTimestamp("created_at"));
			recipe.setUserid(rs.getString("userid"));
			return recipe;
		}
	};

	private RowMapper<RecipeIngredientVo> ingredientRowMapper = new RowMapper<RecipeIngredientVo>() {
		@Override
		public RecipeIngredientVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			RecipeIngredientVo ingredient = new RecipeIngredientVo();
			ingredient.setIngredientId(rs.getInt("ingredient_id"));
			ingredient.setRecipeId(rs.getInt("recipe_id"));
			ingredient.setName(rs.getString("name"));
			ingredient.setAmount(rs.getString("amount"));
			return ingredient;
		}
	};

	private RowMapper<RecipeStepVo> stepRowMapper = new RowMapper<RecipeStepVo>() {
		@Override
		public RecipeStepVo mapRow(ResultSet rs, int rowNum) throws SQLException {
			RecipeStepVo step = new RecipeStepVo(); // ⭐ 이 부분은 RecipeStepVo로 수정되어야 합니다.
			// RecipeStepVo step = new RecipeStepVo(); // <-- 이렇게 수정
			step.setStepId(rs.getInt("step_id"));
			step.setRecipeId(rs.getInt("recipe_id"));
			step.setStepOrder(rs.getInt("step_order"));
			step.setDescription(rs.getString("description"));
			step.setStepImgUrl(rs.getString("step_img_url"));
			return step;
		}
	};

	private RowMapper<String> stepImageUrlRowMapper = new RowMapper<String>() {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString("step_img_url");
		}
	};

	/*
	 * 레시피 기본 정보 업데이트 (modify) 이 메서드 내부에서 기존 완성 이미지 URL이 변경/삭제되면 Cloudinary에서도
	 * 삭제됩니다.
	 */
	public int updateRecipe(RecipeVo recipe) {
		System.out.println("DAO: Updating recipe: " + recipe.getRecipeId());
		// 업데이트 전 기존 완성 이미지 URL 가져와서 Cloudinary에서 삭제 (새 이미지가 있다면)
		String oldCompleteImgUrl = getCompleteImgUrlByRecipeId(recipe.getRecipeId());
		if (oldCompleteImgUrl != null && !oldCompleteImgUrl.isEmpty()
				&& (recipe.getCompleteImgUrl() == null || !oldCompleteImgUrl.equals(recipe.getCompleteImgUrl()))) {
			deleteImageFromCloudinary(oldCompleteImgUrl);
		}

		String sql = "UPDATE recipe SET title = ?, kind = ?, tip = ?, complete_img_url = ? WHERE recipe_id = ?";
		// ⭐ 스키마에 difficulty 컬럼이 없으므로, SQL에서 difficulty를 제외합니다.
		// 만약 difficulty 컬럼을 추가했다면, SQL과 파라미터에 추가해야 합니다.
		return jdbcTemplate.update(sql, recipe.getTitle(), recipe.getKind(), recipe.getTip(),
				recipe.getCompleteImgUrl(), recipe.getRecipeId());
	}

	/*
	 * 특정 ID 목록에 해당하는 재료 삭제 (modify)
	 */
	public int deleteIngredientsByIds(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}
		String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
		String sql = "DELETE FROM recipe_ingredient WHERE ingredient_id IN (" + inSql + ")";
		System.out.println("DAO: Deleting ingredients by specific IDs: " + ids);
		return jdbcTemplate.update(sql, ids.toArray());
	}

	/*
	 * 특정 ID 목록에 해당하는 단계 삭제 (modify) - 이미지 함께 삭제
	 */
	public int deleteStepsByIds(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}
		System.out.println("DAO: Deleting steps by specific IDs: " + ids);
		// 삭제할 단계들의 이미지 URL을 먼저 조회
		List<String> imageUrlsToDelete = getStepImageUrlsByIds(ids);
		if (imageUrlsToDelete != null && !imageUrlsToDelete.isEmpty()) {
			for (String imageUrl : imageUrlsToDelete) {
				deleteImageFromCloudinary(imageUrl); // Cloudinary에서 이미지 삭제
			}
		}
		String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
		String sql = "DELETE FROM recipe_step WHERE step_id IN (" + inSql + ")";
		return jdbcTemplate.update(sql, ids.toArray());
	}

	/*
	 * 새 재료 삽입
	 */
	public int insertIngredient(RecipeIngredientVo ingredient) {
		System.out.println("DAO: Inserting new ingredient for recipe: " + ingredient.getRecipeId());
		// SQL 쿼리에 RETURNING ingredient_id 추가
		String sql = "INSERT INTO recipe_ingredient (recipe_id, name, amount) VALUES (?, ?, ?) RETURNING ingredient_id"; // ⭐
																															// 이
																															// 부분
																															// 수정
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int rowsAffected = jdbcTemplate.update(connection -> {
			// PreparedStatement를 생성할 때 Statement.RETURN_GENERATED_KEYS를 사용할 필요가 없습니다.
			// 왜냐하면 SQL 쿼리 자체에 RETURNING 절을 명시했기 때문입니다.
			// 다만, GeneratedKeyHolder를 사용하려면 connection.prepareStatement(sql,
			// Statement.RETURN_GENERATED_KEYS)
			// 또는 connection.prepareStatement(sql, new String[]{"ingredient_id"}) 형태가 필요합니다.
			// RETURNING 절이 명시되어 있다면 Statement.RETURN_GENERATED_KEYS를 사용해도 되지만,
			// 더 명시적으로는 `new String[]{"ingredient_id"}`를 사용하는 것이 좋습니다.

			// 여기서는 RETURNING 절을 사용했으므로 Statement.RETURN_GENERATED_KEYS를 유지해도 무방합니다.
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // 또는 new
																										// String[]{"ingredient_id"}
			ps.setInt(1, ingredient.getRecipeId());
			ps.setString(2, ingredient.getName());
			ps.setString(3, ingredient.getAmount());
			return ps;
		}, keyHolder);

		if (rowsAffected > 0 && keyHolder.getKey() != null) {
			// getKey()는 이제 단일 키 (ingredient_id)를 반환할 것입니다.
			ingredient.setIngredientId(keyHolder.getKey().intValue());
			System.out.println("DAO: New ingredient inserted with ID: " + ingredient.getIngredientId());
		}
		return rowsAffected;
	}

	/*
	 * 새 단계 삽입
	 */
	public int insertStep(RecipeStepVo step) {
		System.out.println("DAO: Inserting new step for recipe: " + step.getRecipeId());
		// SQL 쿼리에 RETURNING step_id 추가
		String sql = "INSERT INTO recipe_step (recipe_id, step_order, description, step_img_url) VALUES (?, ?, ?, ?) RETURNING step_id"; // ⭐
																																			// 이
																																			// 부분
																																			// 수정
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int rowsAffected = jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); // 또는 new
																										// String[]{"step_id"}
			ps.setInt(1, step.getRecipeId());
			ps.setInt(2, step.getStepOrder());
			ps.setString(3, step.getDescription());
			ps.setString(4, step.getStepImgUrl());
			return ps;
		}, keyHolder);

		if (rowsAffected > 0 && keyHolder.getKey() != null) {
			step.setStepId(keyHolder.getKey().intValue());
			System.out.println("DAO: New step inserted with ID: " + step.getStepId());
		}
		return rowsAffected;
	}

	/*
	 * 레시피 ID로 모든 재료 삭제 (서비스의 "전체 삭제 후 재삽입" 전략용)
	 */
	public void deleteIngredientsByRecipeId(int recipeId) {
		String sql = "DELETE FROM recipe_ingredient WHERE recipe_id = ?";
		jdbcTemplate.update(sql, recipeId);
	}

	/*
	 * 레시피 ID로 모든 단계 삭제 (서비스의 "전체 삭제 후 재삽입" 전략용) 이 메서드 호출 시 해당 단계 이미지도 Cloudinary에서
	 * 삭제됩니다.
	 */
	public void deleteStepsByRecipeId(int recipeId) {
		List<String> imageUrlsToDelete = getStepImageUrlsByRecipeId(recipeId);
		if (imageUrlsToDelete != null && !imageUrlsToDelete.isEmpty()) {
			for (String imageUrl : imageUrlsToDelete) {
				deleteImageFromCloudinary(imageUrl); // Cloudinary에서 이미지 삭제
			}
		}
		String sql = "DELETE FROM recipe_step WHERE recipe_id = ?";
		jdbcTemplate.update(sql, recipeId);
	}

	// --- Private Helper Methods (Cloudinary 이미지 관리 및 DB 조회) ---

	// 특정 레시피의 완성 이미지 URL 조회
	private String getCompleteImgUrlByRecipeId(int recipeId) {
		String sql = "SELECT complete_img_url FROM recipe WHERE recipe_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, String.class, recipeId);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	// 특정 단계의 이미지 URL 조회 (단일 조회)
	private String getStepImageUrlById(int stepId) {
		System.out.println("DAO: Fetching step image URL for step ID: " + stepId);
		String sql = "SELECT step_img_url FROM recipe_step WHERE step_id = ?";
		try {
			return jdbcTemplate.queryForObject(sql, String.class, stepId);
		} catch (EmptyResultDataAccessException e) {
			System.out.println("DAO: No image URL found for step ID: " + stepId);
			return null;
		}
	}

	// 특정 레시피에 속한 모든 단계 이미지 URL들 조회
	private List<String> getStepImageUrlsByRecipeId(int recipeId) {
		String sql = "SELECT step_img_url FROM recipe_step WHERE recipe_id = ?";
		return jdbcTemplate.query(sql, stepImageUrlRowMapper, recipeId);
	}

	// 특정 ID 목록에 해당하는 단계 이미지 URL들 조회 (다중 조회)
	private List<String> getStepImageUrlsByIds(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<>();
		}
		System.out.println("DAO: Fetching step image URLs for IDs: " + ids);
		String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
		String sql = "SELECT step_img_url FROM recipe_step WHERE step_id IN (" + inSql + ")";
		return jdbcTemplate.query(sql, ids.toArray(), stepImageUrlRowMapper);
	}

	// Cloudinary 이미지 삭제 유틸리티 메서드
	public void deleteImageFromCloudinary(String imageUrl) {
		if (imageUrl != null && !imageUrl.isEmpty()) {
			try {
				String publicId = extractPublicIdFromCloudinaryUrl(imageUrl);
				if (publicId != null) {
					cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
					System.out.println("Cloudinary image deleted: " + publicId);
				}
			} catch (IOException e) {
				System.err.println("Cloudinary image deletion failed: " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("Error extracting public ID or deleting image: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	// Cloudinary URL에서 public_id를 추출하는 헬퍼 메서드
	private String extractPublicIdFromCloudinaryUrl(String url) {
		if (url == null || url.isEmpty()) {
			return null;
		}
		int uploadIndex = url.indexOf("/upload/");
		if (uploadIndex == -1) {
			return null;
		}

		String partAfterUpload = url.substring(uploadIndex + "/upload/".length());

		int versionIndex = partAfterUpload.indexOf("v");
		if (versionIndex != -1 && partAfterUpload.substring(versionIndex).matches("v\\d+/.*")) {
			int slashAfterVersion = partAfterUpload.indexOf('/', versionIndex);
			if (slashAfterVersion != -1) {
				partAfterUpload = partAfterUpload.substring(slashAfterVersion + 1);
			}
		}

		int dotIndex = partAfterUpload.lastIndexOf('.');
		if (dotIndex != -1) {
			return partAfterUpload.substring(0, dotIndex);
		}
		return partAfterUpload;
	}
}
