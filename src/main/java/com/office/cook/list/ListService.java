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
	public List<ListVo> getCookList(int page, int pageSize) {

		return listDao.getCookList(page, pageSize); // 데이터베이스에서 페이지에 맞는 데이터 반환
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
	public ListVo getCookByName(int cook_no, String cookName) {
		return listDao.getCookByName(cook_no, cookName);
	}

	/*
	 * 북마크
	 */
	public int BookMark(String pageURL, String userid, String CKG_NM, int cook_no) {
		// System.out.println("Service bookmark");
		return listDao.BookMark(pageURL, userid, CKG_NM, cook_no);
	}

	/*
	 * 북마크 존재 여부
	 */
	public boolean isBookmarked(int cook_no, String userid) {

		return listDao.isBookmarked(cook_no, userid);
	}

	/*
	 * 조회수를 증가시키는 메서드
	 */
	public void incrementReadCount(String cookName, int cook_no) {
		listDao.incrementReadCount(cookName, cook_no);
	}

	/*
	 * 조회수 순 상위 10개 요리 가져오기
	 */
	public List<ListVo> getTopCooksByReadCount() {
		return listDao.getTopCooksByReadCount();
	}

	/*
	 * 요리 이름 검색
	 */
	public List<ListVo> searchCooksByName(String word) {
		return listDao.searchCooksByName(word);
	}

	/*
	 * 사용자 ID로 북마크 목록을 가져오는 메서드
	 */
	public List<ListVo> getBookmarks(String userId) {
		return listDao.getBookmarks(userId);
	}

	/*
	 * 레시피 등록 메소드
	 */
	public void registerRecipe(ListVo recipe) {
		listDao.insertRecipe(recipe);
	}

	/*
	 * 레시피 목록을 조회하는 메서드
	 */
	public List<ListVo> getRecipeList() {
		return listDao.getRecipeList();
	}

	/*
	 * 요리 이름으로 상세 정보를 가져오는 메서드
	 */
	public ListVo getRecipeByName(String cookName, int recipe_no) {
		return listDao.getRecipeByName(cookName, recipe_no);
	}

	/*
	 * 레시피 등록 메소드
	 */
	public void adminRegisterRecipe(ListVo recipe) {
		listDao.adminInsertRecipe(recipe);
	}

	/*
	 * 레시피 등록 페이지에서 삭제
	 */
	public boolean deleteRecipeByName(String cookName, int recipe_no) {
		try {
			return listDao.deleteRecipeByName(cookName, recipe_no); // DAO 메서드를 호출하여 삭제 처리
		} catch (Exception e) {
			e.printStackTrace();
			return false; // 예외 발생 시 false 반환
		}
	}

	/*
	 * 레시피 상세 정보에서 삭제
	 */
	public boolean deleteCookByName(int cook_no, String cookName) {
		int result = listDao.deleteCookByName(cook_no, cookName);
		return result > 0; // 삭제 성공 여부 반환
	}

	/*
	 * 레시피 수정
	 */
	public boolean updateCooks(String oldName, ListVo cook) {
		int rowsAffected = listDao.updateCooks(oldName, cook);
		return rowsAffected > 0;
	}

	/*
	 *  카테고리별로 레시피 목록을 페이지네이션하여 가져오기
	 */
	public List<ListVo> getCookListByCategory(String category, int page, int pageSize) {
		return listDao.findCooksByCategoryWithPagination(category, page, pageSize);
	}

	/*
	 *  카테고리별로 레시피 총 개수
	 */
	public int getTotalCookCountByCategory(String category) {
		return listDao.findTotalCookCountByCategory(category);
	}
}
