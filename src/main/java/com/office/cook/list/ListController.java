package com.office.cook.list;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.office.cook.board.BoardService;
import com.office.cook.board.BoardVo;
import com.office.cook.member.MemberVo;

@Controller
@RequestMapping("/list")
public class ListController {

	@Autowired
	ListService listService;

	@Autowired
	BoardService boardService; // 댓글을 처리하는 서비스 추가

	/*
	 * 요리 목록을 조회하는 메소드
	 */
	@GetMapping("")
	public String getCookList(@RequestParam(defaultValue = "1") int page, // 페이지 번호 (기본값 1)
			@RequestParam(defaultValue = "18") int pageSize, // 페이지 크기 (기본값 10)
			Model model) {

		// 페이지네이션을 처리한 레시피 목록을 가져옵니다.
		List<ListVo> cookList = listService.getCookList(page, pageSize);
		// System.out.println("CookList="+cookList);

		// 총 레시피 개수를 가져옵니다.
		int totalCookCount = listService.getTotalCookCount();

		// 총 페이지 수 계산 (총 레시피 수 / 페이지 크기)
		int totalPage = (int) Math.ceil((double) totalCookCount / pageSize);

		// 모델에 레시피 목록, 페이지 정보, 페이지 크기 등을 추가하여 JSP로 전달
		model.addAttribute("cookList", cookList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("pageSize", pageSize);

		return "main"; // main.jsp로 데이터 전달
	}

	
	
	/*
	 * 상세정보 , 댓글
	 */
	//@RequestMapping("/details")
	@GetMapping("/details")
	public String getCookDetails(@RequestParam("cook_no") int cook_no, @RequestParam("cookName") String cookName,
			@RequestParam(value = "page", defaultValue = "1") int page, // 기본 페이지 1
			Model model, HttpSession session) {

		String userid = null;
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		if (loginedMemberVo != null) {
			userid = loginedMemberVo.getUserid();
		}

		// 요리 상세 정보 가져오기
		ListVo cookInfo = listService.getCookByName(cook_no, cookName);

		if (cookInfo != null) {
			// step1~step20 필드를 리스트로 변환
			List<String> steps = new ArrayList<>();
			for (int i = 1; i <= 20; i++) {
				try {
					// Reflection으로 필드 접근
					Field field = cookInfo.getClass().getDeclaredField("step" + i);
					field.setAccessible(true);
					String stepValue = (String) field.get(cookInfo);
					if (stepValue != null && !stepValue.isEmpty()) {
						steps.add(stepValue);
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			// 댓글 페이지네이션 관련 변수 설정
			int pageSize = 10; // 한 페이지에 10개의 댓글
			int offset = (page - 1) * pageSize; // 댓글 목록의 시작 위치
			int totalComments = boardService.getTotalCommentsCount(cookName, cook_no); // 전체 댓글 수
			int totalPages = (int) Math.ceil((double) totalComments / pageSize); // 총 페이지 수

			// 댓글 목록을 페이지별로 가져오기
			List<BoardVo> boardList = boardService.getCommentsByCookName(cookName, cook_no, offset, pageSize);

			// 북마크 상태 확인
			boolean bookmarkExists = listService.isBookmarked(cook_no, userid);

			// 모델에 데이터 추가
			model.addAttribute("cookInfo", cookInfo);
			model.addAttribute("steps", steps); // 추가된 steps 리스트
			model.addAttribute("boardList", boardList); // 댓글 목록
			model.addAttribute("currentPage", page); // 현재 페이지
			model.addAttribute("totalPages", totalPages); // 총 페이지 수
			model.addAttribute("bookmarkExists", bookmarkExists); // 총 페이지 수

		}
		return "/list/details";
	}

	/*
	 * 북마크
	 */
	@PostMapping("/bookmark")
	public String saveBookmark(@RequestParam("pageURL") String pageURL, @RequestParam("userid") String userid,
			@RequestParam("CKG_NM") String CKG_NM, @RequestParam("cook_no") int cook_no, Model model) {

		try {
			String encodedPageURL = URLEncoder.encode(pageURL, "UTF-8");
			String encodedCookName = URLEncoder.encode(CKG_NM, "UTF-8");
			String encodedCook_no = URLEncoder.encode(String.valueOf(cook_no), "UTF-8");
			// 리다이렉트 URL에 인코딩된 요리 이름을 추가
			// 북마크 정보 저장 로직 추가 (예: DB에 저장)
			int result = listService.BookMark(encodedPageURL, userid, CKG_NM, cook_no);
			// 저장 성공 후, 사용자를 다른 페이지로 리다이렉트
			// model.addAttribute("message", "북마크가 성공적으로 저장되었습니다.");
			return "redirect:/list/details?cook_no=" + encodedCook_no + "&cookName=" + encodedCookName;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			// 인코딩 오류 발생 시 에러 페이지로 이동
			return "error";
		}

	}

	/*
	 * 조회수를 증가시키는 메서드
	 */
	@GetMapping("/incrementReadCount")
	@ResponseBody
	public String incrementReadCount(@RequestParam("cookName") String cookName, @RequestParam("cook_no") int cook_no) {
		listService.incrementReadCount(cookName, cook_no);
		return "success";
	}

	/*
	 * 랭킹 페이지 추가
	 */
	@GetMapping("/ranking")
	public String getRanking(Model model) {
		List<ListVo> topCooks = listService.getTopCooksByReadCount();
		model.addAttribute("cookList", topCooks);
		return "list/ranking"; // ranking.jsp로 이동
	}

	/*
	 * 검색 요청 처리
	 */
	@GetMapping("/search")
	public String search(@RequestParam("word") String word, Model model) {

		// 검색 결과 가져오기
		List<ListVo> searchResults = listService.searchCooksByName(word);

		// 검색 결과를 모델에 추가
		model.addAttribute("searchResults", searchResults);
		// 검색어를 모델에 추가
		model.addAttribute("word", word);

		// searchResult.jsp로 이동
		return "search/searchResult";
	}

	/*
	 * 북마크 목록을 가져오는 메서드
	 */
	@GetMapping("/myBookmark")
	public String myBookmark(HttpSession session, Model model) {
		// 세션에서 로그인된 사용자 ID 가져오기
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		// 사용자 ID로 북마크 목록 가져오기
		List<ListVo> bookmarks = listService.getBookmarks(loginedMemberVo.getUserid());
		model.addAttribute("bookmarks", bookmarks);

		return "list/mybookmark"; // myBookmark.jsp로 포워딩
	}

	/*
	 * 레시피 등록 페이지로 이동
	 */
	@RequestMapping("/registerForm")
	public String showRegisterForm() {
		return "register/registerForm"; // "register/registerForm.jsp" 파일을 찾습니다.
	}

	/*
	 * 레시피 등록
	 */
	@PostMapping("/register")
	public String registerRecipe(ListVo recipe) {
		MultipartFile file = recipe.getCKG_IMG_FILE(); // 업로드된 파일 받기

		if (file != null && !file.isEmpty()) {
			String uploadDir = "C:/library/upload/"; // 파일을 저장할 경로

			// 원래 파일 이름과 확장자 추출
			String originalFileName = file.getOriginalFilename();
			String fileExtension = ""; // 확장자를 저장할 변수

			// 확장자가 존재하면 분리
			if (originalFileName != null && originalFileName.contains(".")) {
				fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
			}

			// UUID 생성 후 파일 이름에 추가
			String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

			File saveFile = new File(uploadDir, uniqueFileName);

			try {
				// 파일을 서버에 저장
				file.transferTo(saveFile);

				// DB에 저장할 파일 경로를 recipe 객체에 설정
				recipe.setCKG_IMG_URL(uniqueFileName); // 고유 파일 이름을 CKG_IMG_URL에 설정
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// select에서 받은 값
		String kind = recipe.getCKG_KND_ACTO_NM(); // 예: "1", "2", "3", ...

		// 숫자 값에 따라 실제 값(한식, 중식 등)으로 변환
		switch (kind) {
		case "1":
			recipe.setCKG_KND_ACTO_NM("한식");
			break;
		case "2":
			recipe.setCKG_KND_ACTO_NM("중식");
			break;
		case "3":
			recipe.setCKG_KND_ACTO_NM("일식");
			break;
		case "4":
			recipe.setCKG_KND_ACTO_NM("양식");
			break;
		case "5":
			recipe.setCKG_KND_ACTO_NM("기타");
			break;
		}

		// DB에 레시피 정보 저장
		listService.registerRecipe(recipe);

		return "redirect:/list"; // 성공 시 리디렉션
	}

	/*
	 * 레시피 목록을 조회하는 메소드
	 */
	@GetMapping("recipeList")
	public String getRecipeList(Model model) {
		List<ListVo> recipeList = listService.getRecipeList();
		model.addAttribute("recipeList", recipeList); // 데이터를 JSP로 전달
		return "register/adminRegister";
	}

	/*
	 * 레시피 상세 정보 확인
	 */
	@RequestMapping("/recipeDetails")
	public String getRecipeDetails(@RequestParam("cookName") String cookName, @RequestParam("recipe_no") int recipe_no,
			Model model) {
		// 요리 상세 정보 가져오기
		ListVo recipeInfo = listService.getRecipeByName(cookName, recipe_no);

		if (recipeInfo != null) {
			// step1~step20 필드를 리스트로 변환
			List<String> steps = new ArrayList<>();
			for (int i = 1; i <= 20; i++) {
				try {
					// Reflection으로 필드 접근
					Field field = recipeInfo.getClass().getDeclaredField("step" + i);
					field.setAccessible(true);
					String stepValue = (String) field.get(recipeInfo);
					if (stepValue != null && !stepValue.isEmpty()) {
						steps.add(stepValue);
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			model.addAttribute("recipeInfo", recipeInfo);
			model.addAttribute("steps", steps); // 추가된 steps 리스트
		}
		return "/register/recipeDetails";
	}

	/*
	 * 레시피 등록
	 */
	@PostMapping("/registerRecipe")
	public String adminRegisterRecipe(ListVo recipe) {
		MultipartFile file = recipe.getCKG_IMG_FILE(); // 업로드된 파일 받기

		if (file != null && !file.isEmpty()) {
			String uploadDir = "C:/library/upload/"; // 파일을 저장할 경로

			// 원래 파일 이름과 확장자 추출
			String originalFileName = file.getOriginalFilename();
			String fileExtension = ""; // 확장자를 저장할 변수

			// 확장자가 존재하면 분리
			if (originalFileName != null && originalFileName.contains(".")) {
				fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
			}

			// UUID 생성 후 파일 이름에 추가
			String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

			File saveFile = new File(uploadDir, uniqueFileName);

			try {
				// 파일을 서버에 저장
				file.transferTo(saveFile);
				// DB에 저장할 파일 경로를 recipe 객체에 설정
				recipe.setCKG_IMG_URL(uniqueFileName); // 파일 이름을 CKG_IMG_URL에 설정
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// select에서 받은 값
		String kind = recipe.getCKG_KND_ACTO_NM(); // 예: "1", "2", "3", ...

		// 숫자 값에 따라 실제 값(한식, 중식 등)으로 변환
		switch (kind) {
		case "1":
			recipe.setCKG_KND_ACTO_NM("한식");
			break;
		case "2":
			recipe.setCKG_KND_ACTO_NM("중식");
			break;
		case "3":
			recipe.setCKG_KND_ACTO_NM("일식");
			break;
		case "4":
			recipe.setCKG_KND_ACTO_NM("양식");
			break;
		case "5":
			recipe.setCKG_KND_ACTO_NM("기타");
			break;
		}

		// DB에 레시피 정보 저장
		listService.adminRegisterRecipe(recipe);

		return "redirect:/list"; // 성공 시 리디렉션
	}

	/*
	 * 레시피 등록 페이지에서 삭제
	 */
	@RequestMapping("/deleteRecipe")
	@ResponseBody
	public Map<String, Object> deleteRecipe(@RequestParam String cookName, @RequestParam int recipe_no) {
		Map<String, Object> response = new HashMap<>();
		try {
			// 레시피 삭제 처리
			boolean isDeleted = listService.deleteRecipeByName(cookName, recipe_no);

			// 삭제 성공 여부에 따라 응답 설정
			if (isDeleted) {
				response.put("success", true);
			} else {
				response.put("success", false);
				response.put("message", "레시피 삭제에 실패했습니다.");
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "삭제 중 오류가 발생했습니다.");
		}
		return response; // JSON 응답
	}

	/*
	 * 레시피 상세 정보에서 삭제
	 */
	@PostMapping("/deleteCooks")
	public String deleteCooks(@RequestParam("cook_no") int cook_no, @RequestParam("CKG_NM") String cookName,
			Model model) {

		// 서비스 호출하여 삭제 처리
		boolean isDeleted = listService.deleteCookByName(cook_no, cookName);

		if (isDeleted) {
			// 삭제 성공 시 레시피 목록 페이지로 이동
			return "redirect:/list";
		} else {
			// 삭제 실패 시 에러 메시지를 모델에 추가
			model.addAttribute("errorMessage", "레시피 삭제에 실패했습니다. 다시 시도해주세요.");
			return "list/details"; // 현재 페이지로 다시 이동
		}
	}

	/*
	 * 레시피 상세 정보 수정 폼에 띄우기
	 */
	@RequestMapping("/modifyCooks")
	public String getModifyCooks(@RequestParam("cook_no") int cook_no, @RequestParam("CKG_NM") String cookName,
			Model model) {
		// 요리 상세 정보 가져오기
		ListVo cookInfo = listService.getCookByName(cook_no, cookName);

		if (cookInfo != null) {
			// step1~step20 필드를 리스트로 변환
			List<String> steps = new ArrayList<>();
			for (int i = 1; i <= 20; i++) {
				try {
					// Reflection으로 필드 접근
					Field field = cookInfo.getClass().getDeclaredField("step" + i);
					field.setAccessible(true);
					String stepValue = (String) field.get(cookInfo);
					if (stepValue != null && !stepValue.isEmpty()) {
						steps.add(stepValue);
					}
				} catch (NoSuchFieldException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			model.addAttribute("cookInfo", cookInfo);
			model.addAttribute("steps", steps); // 추가된 steps 리스트
		}
		return "/list/modifyCooks";
	}

	/*
	 * 레시피 수정
	 */
	@PostMapping("/updateCooks")
	public String updateCooks(@RequestParam("oldName") String oldName, ListVo cook, Model model) {
		MultipartFile file = cook.getCKG_IMG_FILE(); // 업로드된 파일 받기

		if (file != null && !file.isEmpty()) {
			String uploadDir = "C:/library/upload/"; // 파일을 저장할 경로

			// 원래 파일 이름과 확장자 추출
			String originalFileName = file.getOriginalFilename();
			String fileExtension = ""; // 확장자를 저장할 변수

			// 확장자가 존재하면 분리
			if (originalFileName != null && originalFileName.contains(".")) {
				fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
			}

			// UUID 생성 후 파일 이름에 추가
			String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

			File saveFile = new File(uploadDir, uniqueFileName);

			try {
				// 파일을 서버에 저장
				file.transferTo(saveFile);
				// DB에 저장할 파일 경로를 recipe 객체에 설정
				cook.setCKG_IMG_URL(uniqueFileName); // 파일 이름을 CKG_IMG_URL에 설정
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// select에서 받은 값
		String kind = cook.getCKG_KND_ACTO_NM(); // 예: "1", "2", "3", ...

		// 숫자 값에 따라 실제 값(한식, 중식 등)으로 변환
		switch (kind) {
		case "1":
			cook.setCKG_KND_ACTO_NM("한식");
			break;
		case "2":
			cook.setCKG_KND_ACTO_NM("중식");
			break;
		case "3":
			cook.setCKG_KND_ACTO_NM("일식");
			break;
		case "4":
			cook.setCKG_KND_ACTO_NM("양식");
			break;
		case "5":
			cook.setCKG_KND_ACTO_NM("기타");
			break;
		}

		// DB 업데이트
		boolean isUpdated = listService.updateCooks(oldName, cook);

		if (!isUpdated) {
			model.addAttribute("errorMessage", "레시피 수정에 실패했습니다.");
			return "/list/modifyCooks"; // 실패 시 수정 폼으로 돌아가기
		}

		String cookName = cook.getCKG_NM();
		int cook_no = cook.getCook_no();

		try {
			// URL 인코딩
			String encodedCookName = URLEncoder.encode(cookName, "UTF-8");
			String encodedCook_no = URLEncoder.encode(String.valueOf(cook_no), "UTF-8");

			return "redirect:/list/details?cook_no=" + encodedCook_no + "&cookName=" + encodedCookName; // 성공 시 리디렉션
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			model.addAttribute("errorMessage", "URL 인코딩에 실패했습니다.");
			return "/list/modifyCooks"; // 실패 시 수정 폼으로 돌아가기
		}
	}

	/*
	 * 카테고리 요리찾기
	 */
	@RequestMapping("/filter")
	public String getCooksByCategory(@RequestParam("category") String category,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "18") int pageSize, Model model) {

		// 총 레시피 개수를 가져옵니다.
		int totalCookCount = listService.getTotalCookCountByCategory(category);

		// 총 페이지 수 계산 (총 레시피 수 / 페이지 크기)
		int totalPage = (int) Math.ceil((double) totalCookCount / pageSize);

		// 현재 페이지 유효성 검사
		if (page < 1)
			page = 1;
		if (page > totalPage)
			page = totalPage;

		// 페이지에 맞는 레시피 목록 가져오기
		List<ListVo> cookList = listService.getCookListByCategory(category, page, pageSize);

		// 모델에 레시피 목록, 페이지 정보 등을 추가
		model.addAttribute("category", category); // 카테고리 정보
		model.addAttribute("cookList", cookList); // 레시피 목록
		model.addAttribute("currentPage", page); // 현재 페이지
		model.addAttribute("totalPage", totalPage); // 총 페이지 수
		model.addAttribute("totalCookCount", totalCookCount); // 총 레시피 개수
		model.addAttribute("pageSize", pageSize); // 페이지 크기

		return "list/category"; 
	}

}
