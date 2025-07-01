package com.office.cook.list;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListService {

	@Autowired
	ListDao listDao;

	/*
	 * 요리 목록을 조회하는 메서드
	 */
	public List<RecipeVo> getCookList(int page, int pageSize) {
		int offset = (page - 1) * pageSize;
		return listDao.getCookList(offset, pageSize);
	}

	/*
	 * 레시피 개수
	 */
	public int getTotalCookCount() {
		return listDao.getTotalCookCount(); // 전체 아이템 수를 반환
	}

	/*
	 * 요리 이름으로 상세 정보를 가져오는 메서드
	 */
	public RecipeVo getCookByName(int recipeId, String title) {
		RecipeVo recipe = listDao.getRecipeByIdAndName(recipeId, title);
		if (recipe != null) {
			recipe.setSteps(listDao.getRecipeSteps(recipeId));
			recipe.setIngredients(listDao.getRecipeIngredients(recipeId));
		}
		return recipe;
	}

	public RecipeVo getCookById(int recipeId) {
		RecipeVo recipe = listDao.getRecipeById(recipeId);
		if (recipe != null) {
			recipe.setSteps(listDao.getRecipeSteps(recipeId));
			recipe.setIngredients(listDao.getRecipeIngredients(recipeId));
		}
		return recipe;
	}

	/*
	 * 북마크
	 */
	public boolean toggleBookmark(int recipeId, String userId) {
		// DAO에 직접 두 파라미터를 넘깁니다.
		int bookmarkCount = listDao.checkBookmarkExists(recipeId, userId); // DAO 메서드 시그니처도 변경해야 함

		if (bookmarkCount > 0) {
			listDao.deleteBookmark(recipeId, userId); // DAO 메서드 시그니처도 변경해야 함
			System.out.println("DEBUG: 북마크 삭제 - recipeId: " + recipeId + ", userId: " + userId);
			return false;
		} else {
			listDao.insertBookmark(recipeId, userId); // DAO 메서드 시그니처도 변경해야 함
			System.out.println("DEBUG: 북마크 추가 - recipeId: " + recipeId + ", userId: " + userId);
			return true;
		}
	}

	/*
	 * public int BookMark(String pageUrl, String userid, String title, int
	 * recipeId) { return listDao.insertBookmark(pageUrl, userid, title, recipeId);
	 * }
	 */
	/*
	 * 북마크 존재 여부
	 */
	 public boolean isBookmarked(int cook_no, String userid) {
	 
	 return listDao.isBookmarked(cook_no, userid); }
	 
	/*
	 * 조회수를 증가시키는 메서드
	 */
	public void incrementReadCount(int recipeId) {
		listDao.incrementReadCount(recipeId);
	}

	/*
	 * 조회수 순 상위 10개 요리 가져오기
	 */
	public List<RecipeVo> getTopCooksByReadCount() {
		return listDao.getTopCooksByReadCount();
	}

	/*
	 * 사용자 ID로 북마크 목록을 가져오는 메서드
	 */
	public List<RecipeVo> getBookmarks(String userid) {
		return listDao.getBookmarks(userid);
	}

	/*
	 * 레시피 등록 메소드
	 */
	public void registerRecipe(RecipeVo recipe) {
		// 1. 레시피 정보 저장 → recipe_id 반환
		int recipeId = listDao.insertRecipe(recipe);

		// 2. 재료 저장
		for (RecipeIngredientVo ing : recipe.getIngredients()) {
			ing.setRecipeId(recipeId);
			listDao.insertIngredient(ing);
		}

		// 3. 단계 저장
		for (RecipeStepVo step : recipe.getSteps()) {
			step.setRecipeId(recipeId);
			listDao.insertStep(step);
		}
	}

	/*
	 * 레시피 목록을 조회하는 메서드
	 */
	public List<RecipeVo> getRecipeList() {
		return listDao.getRecipeList();
	}

	public RecipeVo getRecipeById(int recipeId) {
		return listDao.getRecipeById(recipeId);
	}

	public List<RecipeStepVo> getStepsByRecipeId(int recipeId) {
		return listDao.getStepsByRecipeId(recipeId);
	}

	public List<RecipeIngredientVo> getIngredientsByRecipeId(int recipeId) {
		return listDao.getIngredientsByRecipeId(recipeId);
	}

	public boolean deleteRecipeById(int recipeId) {
		try {
			return listDao.deleteRecipeById(recipeId) > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * 레시피 수정
	 */
	public boolean updateRecipe(RecipeVo recipe) {
		try {
			// 1. 기본 정보 수정
			int updated = listDao.updateRecipe(recipe);

			// 2. 재료 삭제 후 재등록
			listDao.deleteIngredientsByRecipeId(recipe.getRecipeId());
			for (RecipeIngredientVo ing : recipe.getIngredients()) {
				listDao.insertIngredient(ing);
			}

			// 3. 단계 삭제 후 재등록
			listDao.deleteStepsByRecipeId(recipe.getRecipeId());
			for (RecipeStepVo step : recipe.getSteps()) {
				listDao.insertStep(step);
			}

			return updated > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * 카테고리별로 레시피 목록을 페이지네이션하여 가져오기
	 */
	public List<RecipeVo> getRecipesByCategory(String category, int page, int pageSize) {
		int offset = (page - 1) * pageSize;
		return listDao.getRecipesByCategory(category, offset, pageSize);
	}

	/*
	 * 카테고리별로 레시피 총 개수
	 */
	public int getTotalRecipeCountByCategory(String category) {
		return listDao.getTotalRecipeCountByCategory(category);
	}

	/*
	 * 요리 이름 검색
	 */
	public List<RecipeVo> searchCooksByName(String keyword) {
		return listDao.searchCooksByName(keyword);
	}
}
