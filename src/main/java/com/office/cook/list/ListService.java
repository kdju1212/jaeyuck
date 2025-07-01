package com.office.cook.list;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ListService {

	@Autowired
	ListDao listDao;

	private static final long VIEW_COOKIE_EXPIRATION_HOURS = 24;

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

	/*
	 * public RecipeVo getCookById(int recipeId) { int updatedRows =
	 * listDao.incrementReadCount(recipeId);
	 * System.out.println("DEBUG_VIEWS: recipeId " + recipeId +
	 * "의 조회수 증가 시도. 업데이트된 행: " + updatedRows); RecipeVo recipe =
	 * listDao.getRecipeById(recipeId); if (recipe != null) {
	 * recipe.setSteps(listDao.getRecipeSteps(recipeId));
	 * recipe.setIngredients(listDao.getRecipeIngredients(recipeId)); } return
	 * recipe; }
	 */
	@Transactional // 조회수 증가 및 레시피 조회는 하나의 트랜잭션으로 묶는 것이 좋습니다.
	public RecipeVo getCookById(int recipeId, HttpServletRequest request, HttpServletResponse response) { // ⭐ 파라미터 변경 ⭐
		// 1. 조회수 증가 로직 (쿠키 기반 중복 방지)
		String cookieName = "recipe_view_history"; // 쿠키 이름
		boolean canIncrementView = true; // 조회수를 증가시킬 수 있는지 여부

		Cookie[] cookies = request.getCookies();
		String existingViewHistory = null;

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					existingViewHistory = cookie.getValue();
					break;
				}
			}
		}

		if (existingViewHistory != null && !existingViewHistory.isEmpty()) {
			// "레시피ID=타임스탬프|레시피ID=타임스탬프" 형식으로 저장된 기록들을 분리
			String[] views = existingViewHistory.split("\\|"); // '|' 기준으로 분리
			for (String viewRecord : views) {
				String[] parts = viewRecord.split("=");
				if (parts.length == 2) {
					try {
						int viewedRecipeId = Integer.parseInt(parts[0]);
						long viewTimestamp = Long.parseLong(parts[1]);

						// 현재 레시피 ID와 일치하고, 만료 시간(24시간)이 지나지 않았다면
						if (viewedRecipeId == recipeId) {
							long currentTime = new Date().getTime();
							long expirationTime = viewTimestamp + TimeUnit.HOURS.toMillis(VIEW_COOKIE_EXPIRATION_HOURS);
							if (currentTime < expirationTime) {
								canIncrementView = false; // 이미 조회했고, 아직 만료되지 않았으므로 조회수 증가 안 함
								System.out.println("DEBUG_VIEWS: recipeId " + recipeId + "는 이미 최근에 조회되었습니다 (쿠키 기반).");
								break;
							}
						}
					} catch (NumberFormatException e) {
						System.err
								.println("DEBUG_VIEWS: Invalid cookie format: " + viewRecord + " - " + e.getMessage());
						// 잘못된 형식의 쿠키는 무시하고 진행
					}
				}
			}
		}

		if (canIncrementView) {
			// 조회수 증가
			int updatedRows = listDao.incrementReadCount(recipeId);
			System.out.println("DEBUG_VIEWS: recipeId " + recipeId + "의 조회수 증가 시도. 업데이트된 행: " + updatedRows);

			// 조회수 증가 후 새로운 쿠키 값 생성 및 설정
			String newViewRecord = recipeId + "=" + new Date().getTime(); // "레시피ID=현재시간밀리초"
			String updatedViewHistory = (existingViewHistory == null || existingViewHistory.isEmpty()) ? newViewRecord
					: existingViewHistory + "|" + newViewRecord;

			// 너무 많은 기록이 쌓이지 않도록 오래된 기록은 삭제 (옵션)
			// 예시: 최대 50개 레시피 기록 유지. 오래된 것부터 지움.
			String[] updatedViewsArray = updatedViewHistory.split("\\|");
			if (updatedViewsArray.length > 50) {
				updatedViewHistory = String.join("|",
						Arrays.copyOfRange(updatedViewsArray, updatedViewsArray.length - 50, updatedViewsArray.length));
			}

			Cookie newCookie = new Cookie(cookieName, updatedViewHistory);
			newCookie.setMaxAge((int) TimeUnit.HOURS.toSeconds(VIEW_COOKIE_EXPIRATION_HOURS)); // 쿠키 만료 시간 설정 (초 단위)
			newCookie.setPath("/"); // 모든 경로에서 쿠키 유효
			newCookie.setHttpOnly(true); // XSS 공격 방지 (JavaScript에서 접근 불가)
			// newCookie.setSecure(true); // HTTPS 에서만 전송 (운영 환경에서는 권장)
			response.addCookie(newCookie); // 응답에 쿠키 추가
		}

		// 2. 레시피 상세 정보 가져오기 (기존 로직)
		// ⭐ 아래 메서드 이름들이 현재 DAO의 이름과 일치하는지 다시 확인하세요 ⭐
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

		return listDao.isBookmarked(cook_no, userid);
	}

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
