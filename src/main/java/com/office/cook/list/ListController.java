package com.office.cook.list;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.office.cook.board.BoardService;
import com.office.cook.board.BoardVo;
import com.office.cook.member.MemberVo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/list")
public class ListController {

	@Autowired
	ListService listService;

	@Autowired
	BoardService boardService; // 댓글을 처리하는 서비스 추가

	@Autowired
	private Cloudinary cloudinary;

	/*
	 * 정규화된 레시피 목록을 조회하는 메소드
	 */
	@GetMapping("")
	public String getCookList(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "18") int pageSize, Model model) {

		// 1. 페이지 처리된 레시피 목록 가져오기
		List<RecipeVo> cookList = listService.getCookList(page, pageSize);

		// 2. 총 레시피 수 및 페이지 계산
		int totalCookCount = listService.getTotalCookCount();
		int totalPage = (int) Math.ceil((double) totalCookCount / pageSize);

		// 3. JSP 전달
		// model.addAttribute("cookList", cookList);
		model.addAttribute("recipeList", cookList); // ✅ HTML에 맞춰 변수명 일치
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("pageSize", pageSize);

		return "main"; // Thymeleaf의 main.html
	}

//	@GetMapping("/details")
//	public String getCookDetails(@RequestParam("recipeId") int recipeId,
//			@RequestParam(value = "page", defaultValue = "1") int page, Model model, HttpSession session) {
//
//		String userid = null;
//		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
//		if (loginedMemberVo != null) {
//			userid = loginedMemberVo.getUserid();
//		}
//
//		if (loginedMemberVo != null) {
//			System.out.println("DEBUG_DETAILS_FINAL: model.addAttribute 직전 loginedMemberVo.userid: "
//					+ loginedMemberVo.getUserid());
//		} else {
//			System.out.println(
//					"DEBUG_DETAILS_FINAL: model.addAttribute 직전 loginedMemberVo가 NULL입니다 (세션이 NULL이거나 memberVo가 NULL).");
//		}
//
//		// ✅ 레시피 상세 정보 조회 (recipe + steps + ingredients)
//		RecipeVo recipe = listService.getCookById(recipeId);
//		if (recipe == null) {
//			// 해당 레시피가 없으면 목록 페이지로 리다이렉트
//			return "redirect:/list";
//		}
//
//		
//
//		if (recipe != null) {
//			System.out.println("DEBUG_RECIPE: recipe.userid (레시피 작성자 ID): " + recipe.getUserid());
//		} else {
//			System.out.println("DEBUG_RECIPE: recipe is NULL.");
//		}
//
//		// ✨ 레시피의 좋아요/싫어요 수 조회 ✨
//		// boardService에서 레시피 전용 좋아요/싫어요 조회 메서드를 호출합니다.
//		// 이 메서드들이 recipeId에 대한 좋아요/싫어요 수를 정확히 반환해야 합니다.
//		int likeCount = boardService.getRecipeLikeCount(recipeId);
//		int dislikeCount = boardService.getRecipeDislikeCount(recipeId);
//
//		// ✅ 댓글 페이지네이션 처리
//		int pageSize = 10;
//		int offset = (page - 1) * pageSize;
//		int totalComments = boardService.getTotalCommentsCount(recipeId);
//		int totalPages = (int) Math.ceil((double) totalComments / pageSize);
//		List<BoardVo> boardList = boardService.getCommentsByCookId(recipeId, offset, pageSize);
//
//		System.out.println("가져온 댓글 개수: " + boardList.size());
//		for (int i = 0; i < boardList.size(); i++) {
//			BoardVo comment = boardList.get(i);
//			if (comment == null) {
//				System.err.println("!!!!! 경고: " + i + "번째 댓글 객체가 NULL 입니다 !!!!!!!!!");
//			} else {
//				System.out.println("댓글 " + i + ": board_no=" + comment.getBoard_no() + ", likeCount="
//						+ comment.getLikeCount() + ", dislikeCount=" + comment.getDislikeCount());
//			}
//		}
//
//		// ✅ 북마크 확인
//		boolean bookmarkExists = listService.isBookmarked(recipeId, userid);
//
//		// ✅ 모델에 데이터 전달
//		model.addAttribute("loginedMemberVo", loginedMemberVo);
//		model.addAttribute("recipe", recipe); // RecipeVo 객체 자체 전달
//		model.addAttribute("steps", recipe.getSteps()); // 레시피 단계 리스트
//		model.addAttribute("ingredients", recipe.getIngredients()); // 레시피 재료 리스트
//		// model.addAttribute("boardList", boardList); // 댓글 리스트
//		model.addAttribute("comments", boardList);
//		model.addAttribute("currentPage", page);
//		model.addAttribute("totalPages", totalPages);
//		model.addAttribute("bookmarkExists", bookmarkExists);
//		// ✨ 레시피의 좋아요/싫어요 수 모델에 추가 ✨
//		model.addAttribute("likeCount", likeCount);
//		model.addAttribute("dislikeCount", dislikeCount);
//
//		// 상세 페이지 템플릿 경로 반환
//		return "/list/details";
//	}
	@GetMapping("/details")
	public String getCookDetails(@RequestParam("recipeId") int recipeId,
			@RequestParam(value = "page", defaultValue = "1") int page, Model model, HttpSession session,
			HttpServletRequest request, // ⭐ 파라미터 추가 ⭐
			HttpServletResponse response) {

		String userid = null;
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		if (loginedMemberVo != null) {
			userid = loginedMemberVo.getUserid();
		}

		if (loginedMemberVo != null) {
			System.out.println("DEBUG_DETAILS_FINAL: model.addAttribute 직전 loginedMemberVo.userid: "
					+ loginedMemberVo.getUserid());
		} else {
			System.out.println(
					"DEBUG_DETAILS_FINAL: model.addAttribute 직전 loginedMemberVo가 NULL입니다 (세션이 NULL이거나 memberVo가 NULL).");
		}

		// ✅ 레시피 상세 정보 조회 (recipe + steps + ingredients)
		RecipeVo recipe = listService.getCookById(recipeId, request, response);
		if (recipe == null) {
			// 해당 레시피가 없으면 목록 페이지로 리다이렉트
			return "redirect:/list";
		}

		if (recipe != null) {
			System.out.println("DEBUG_RECIPE: recipe.userid (레시피 작성자 ID): " + recipe.getUserid());
		} else {
			System.out.println("DEBUG_RECIPE: recipe is NULL.");
		}

		// ✨ 레시피의 좋아요/싫어요 수 조회 ✨
		// boardService에서 레시피 전용 좋아요/싫어요 조회 메서드를 호출합니다.
		// 이 메서드들이 recipeId에 대한 좋아요/싫어요 수를 정확히 반환해야 합니다.
		int likeCount = boardService.getRecipeLikeCount(recipeId);
		int dislikeCount = boardService.getRecipeDislikeCount(recipeId);

		// ✅ 댓글 페이지네이션 처리
		int pageSize = 10;
		int offset = (page - 1) * pageSize;
		int totalComments = boardService.getTotalCommentsCount(recipeId);
		int totalPages = (int) Math.ceil((double) totalComments / pageSize);
		List<BoardVo> boardList = boardService.getCommentsByCookId(recipeId, offset, pageSize);

		System.out.println("가져온 댓글 개수: " + boardList.size());
		for (int i = 0; i < boardList.size(); i++) {
			BoardVo comment = boardList.get(i);
			if (comment == null) {
				System.err.println("!!!!! 경고: " + i + "번째 댓글 객체가 NULL 입니다 !!!!!!!!!");
			} else {
				System.out.println("댓글 " + i + ": board_no=" + comment.getBoard_no() + ", likeCount="
						+ comment.getLikeCount() + ", dislikeCount=" + comment.getDislikeCount());
			}
		}

		// ✅ 북마크 확인
		boolean bookmarkExists = listService.isBookmarked(recipeId, userid);

		// ✅ 모델에 데이터 전달
		model.addAttribute("loginedMemberVo", loginedMemberVo);
		model.addAttribute("recipe", recipe); // RecipeVo 객체 자체 전달
		model.addAttribute("steps", recipe.getSteps()); // 레시피 단계 리스트
		model.addAttribute("ingredients", recipe.getIngredients()); // 레시피 재료 리스트
		// model.addAttribute("boardList", boardList); // 댓글 리스트
		model.addAttribute("comments", boardList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("bookmarkExists", bookmarkExists);
		// ✨ 레시피의 좋아요/싫어요 수 모델에 추가 ✨
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("dislikeCount", dislikeCount);

		// 상세 페이지 템플릿 경로 반환
		return "/list/details";
	}

	/*
	 * 레시피 북마크 저장
	 */
	@PostMapping("/toggleBookmark")
	@ResponseBody
	public Map<String, Object> toggleBookmark(@RequestParam("recipeId") int recipeId, HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		// ✅✅✅ 변경된 부분 시작 ✅✅✅
		String userId = null;
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo"); // loginedMemberVo로 가져오기
		if (loginedMemberVo != null) {
			userId = loginedMemberVo.getUserid(); // MemberVo에서 userId 추출
			System.out.println("DEBUG: ListController - toggleBookmark: 세션에서 가져온 userId: " + userId); // 디버그 로그 추가
		} else {
			System.out.println("DEBUG: ListController - toggleBookmark: 세션에서 loginedMemberVo를 찾을 수 없습니다."); // 디버그 로그 추가
		}
		// ✅✅✅ 변경된 부분 끝 ✅✅✅

		if (userId == null) {
			response.put("success", false);
			response.put("message", "로그인이 필요합니다.");
			return response;
		}

		try {
			boolean isBookmarked = listService.toggleBookmark(recipeId, userId);

			response.put("success", true);
			response.put("bookmarked", isBookmarked);
			response.put("message", isBookmarked ? "북마크에 추가되었습니다." : "북마크에서 제거되었습니다.");
			System.out.println("DEBUG: ListController - toggleBookmark: 북마크 처리 성공 - isBookmarked: " + isBookmarked);

		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "북마크 처리 중 서버 오류가 발생했습니다: " + e.getMessage());
			System.err.println("ERROR: ListController - toggleBookmark: 북마크 처리 중 예외 발생: " + e.getMessage());
			e.printStackTrace();
		}
		return response;
	}

	/*
	 * @PostMapping("/bookmark") public String saveBookmark(@RequestParam("pageURL")
	 * String pageURL, @RequestParam("userid") String userid,
	 * 
	 * @RequestParam("CKG_NM") String title, @RequestParam("cook_no") int recipeId,
	 * Model model) {
	 * 
	 * try { // URL 인코딩 String encodedPageURL = URLEncoder.encode(pageURL, "UTF-8");
	 * String encodedTitle = URLEncoder.encode(title, "UTF-8"); String
	 * encodedRecipeId = URLEncoder.encode(String.valueOf(recipeId), "UTF-8");
	 * 
	 * // 북마크 저장 int result = listService.BookMark(encodedPageURL, userid, title,
	 * recipeId);
	 * 
	 * // 리다이렉트 return "redirect:/list/details?cook_no=" + encodedRecipeId +
	 * "&cookName=" + encodedTitle;
	 * 
	 * } catch (UnsupportedEncodingException e) { e.printStackTrace(); return
	 * "error"; } }
	 */
	/*
	 * 레시피 조회수 증가
	 */
	@GetMapping("/incrementReadCount")
	@ResponseBody
	public String incrementReadCount(@RequestParam("cook_no") int recipeId) {
		listService.incrementReadCount(recipeId);
		return "success";
	}

	/*
	 * 레시피 조회수 랭킹
	 */
	@GetMapping("/ranking")
	public String getRanking(Model model) {
		List<RecipeVo> topCooks = listService.getTopCooksByReadCount();
		model.addAttribute("cookList", topCooks);
		return "list/ranking"; // ranking.html
	}

	/*
	 * 내 북마크 목록 보기
	 */
	@GetMapping("/myBookmark")
	public String myBookmark(HttpSession session, Model model) {
		// 로그인 사용자 확인
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		if (loginedMemberVo == null) {
			return "redirect:/member/login";
		}

		String userid = loginedMemberVo.getUserid();

		// 북마크된 레시피 목록 가져오기
		List<RecipeVo> bookmarks = listService.getBookmarks(userid);
		model.addAttribute("bookmarks", bookmarks);

		return "list/mybookmark"; // mybookmark.html
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
	public String registerRecipe(@RequestParam("title") String title, @RequestParam("kind") String kindCode,
			@RequestParam("tip") String tip, @RequestParam("material_name[]") List<String> materialNames,
			@RequestParam("material_amount[]") List<String> materialAmounts,
			@RequestParam("step_text[]") List<String> stepDescriptions,
			@RequestParam("complete_photo") MultipartFile completePhoto,
			@RequestParam(value = "submitType", required = false) String submitType, HttpServletRequest request,
			HttpSession session) { // ✨ HttpSession 주입 추가

		// ✅ 1. 대표 이미지 업로드 (기존과 동일)
		String completeImageUrl = null;
		try {
			if (completePhoto != null && !completePhoto.isEmpty()) {
				Map<String, Object> uploadResult = cloudinary.uploader().upload(completePhoto.getBytes(),
						ObjectUtils.emptyMap());
				completeImageUrl = (String) uploadResult.get("secure_url");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ✅ 2. 음식 종류 코드 매핑 (기존과 동일)
		String kind;
		switch (kindCode) {
		case "1":
			kind = "한식";
			break;
		case "2":
			kind = "중식";
			break;
		case "3":
			kind = "일식";
			break;
		case "4":
			kind = "양식";
			break;
		default:
			kind = "기타";
			break;
		}

		// ✅ 3. RecipeVo 구성
		RecipeVo recipe = new RecipeVo();
		recipe.setTitle(title);
		recipe.setKind(kind);
		recipe.setTip(tip);
		recipe.setCompleteImgUrl(completeImageUrl);

		// ✨✨✨ 중요: 로그인한 사용자 ID 설정 추가 ✨✨✨
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		if (loginedMemberVo != null) {
			recipe.setUserid(loginedMemberVo.getUserid());
			System.out.println("DEBUG_REGISTER: 레시피 등록 시 설정될 userid: " + loginedMemberVo.getUserid());
		} else {
			// 로그인하지 않은 상태에서 레시피 등록 요청이 오는 경우 (보통은 막아야 함)
			System.out.println("DEBUG_REGISTER: 레시피 등록 시 로그인된 사용자 없음. userid를 설정하지 않습니다.");
			// 필요에 따라 여기에 로그인 페이지로 리다이렉트하는 등의 로직을 추가할 수 있습니다.
			// return "redirect:/member/loginForm";
		}
		// ✨✨✨ 여기까지 추가 ✨✨✨

		// ✅ 4. 재료 리스트 구성 (기존과 동일)
		List<RecipeIngredientVo> ingredients = new ArrayList<>();
		for (int i = 0; i < materialNames.size(); i++) {
			RecipeIngredientVo ing = new RecipeIngredientVo();
			ing.setName(materialNames.get(i));
			ing.setAmount(materialAmounts.get(i));
			ingredients.add(ing);
		}
		recipe.setIngredients(ingredients);

		// ✅ 5. 조리 단계 구성 + 이미지 업로드 (기존과 동일)
		List<RecipeStepVo> steps = new ArrayList<>();
		for (int i = 0; i < stepDescriptions.size(); i++) {
			RecipeStepVo step = new RecipeStepVo();
			step.setStepOrder(i + 1);
			step.setDescription(stepDescriptions.get(i));

			MultipartFile stepPhoto = ((MultipartHttpServletRequest) request).getFile("step_photo_" + (i + 1));
			if (stepPhoto != null && !stepPhoto.isEmpty()) {
				try {
					Map<String, Object> stepUpload = cloudinary.uploader().upload(stepPhoto.getBytes(),
							ObjectUtils.emptyMap());
					step.setStepImgUrl((String) stepUpload.get("secure_url"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			steps.add(step);
		}
		recipe.setSteps(steps);

		// ✅ 6. 저장 (기존과 동일)
		listService.registerRecipe(recipe);

		return "redirect:/list";
	}

	/*
	 * 관리자용 레시피 목록 조회
	 */
	@GetMapping("/recipeList")
	public String getRecipeList(Model model) {
		List<RecipeVo> recipeList = listService.getRecipeList();
		model.addAttribute("recipeList", recipeList); // Thymeleaf에 전달
		return "register/adminRegister"; // templates/register/adminRegister.html
	}

	/*
	 * 관리자 레시피 상세 조회
	 */
	@GetMapping("/recipeDetails")
	public String getRecipeDetails(@RequestParam("recipe_no") int recipeId, Model model) {

		// 1. 기본 정보
		RecipeVo recipeInfo = listService.getRecipeById(recipeId);

		if (recipeInfo != null) {
			// 2. 단계별 정보
			List<RecipeStepVo> steps = listService.getStepsByRecipeId(recipeId);

			// 3. 재료 정보
			List<RecipeIngredientVo> ingredients = listService.getIngredientsByRecipeId(recipeId);

			// 4. 모델에 전달
			model.addAttribute("recipeInfo", recipeInfo);
			model.addAttribute("steps", steps);
			model.addAttribute("ingredients", ingredients);
		}

		return "register/recipeDetails"; // templates/register/recipeDetails.html
	}

	@PostMapping("/registerRecipe")
	public String adminRegisterRecipe(@RequestParam("title") String title, @RequestParam("kind") String kindCode,
			@RequestParam("tip") String tip, @RequestParam("material_name[]") List<String> materialNames,
			@RequestParam("material_amount[]") List<String> materialAmounts,
			@RequestParam("step_text[]") List<String> stepDescriptions,
			@RequestParam("complete_photo") MultipartFile completePhoto, HttpServletRequest request) {
		// ✅ Cloudinary 업로드
		String completeImageUrl = null;
		try {
			if (completePhoto != null && !completePhoto.isEmpty()) {
				Map<String, Object> uploadResult = cloudinary.uploader().upload(completePhoto.getBytes(),
						ObjectUtils.emptyMap());
				completeImageUrl = (String) uploadResult.get("secure_url");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ✅ 음식 종류 매핑
		String kind;
		switch (kindCode) {
		case "1":
			kind = "한식";
			break;
		case "2":
			kind = "중식";
			break;
		case "3":
			kind = "일식";
			break;
		case "4":
			kind = "양식";
			break;
		default:
			kind = "기타";
			break;
		}

		// ✅ RecipeVo 조립
		RecipeVo recipe = new RecipeVo();
		recipe.setTitle(title);
		recipe.setKind(kind);
		recipe.setTip(tip);
		recipe.setCompleteImgUrl(completeImageUrl);

		// ✅ 재료 조립
		List<RecipeIngredientVo> ingredients = new ArrayList<>();
		for (int i = 0; i < materialNames.size(); i++) {
			RecipeIngredientVo ing = new RecipeIngredientVo();
			ing.setName(materialNames.get(i));
			ing.setAmount(materialAmounts.get(i));
			ingredients.add(ing);
		}
		recipe.setIngredients(ingredients);

		// ✅ 단계 조립
		List<RecipeStepVo> steps = new ArrayList<>();
		for (int i = 0; i < stepDescriptions.size(); i++) {
			RecipeStepVo step = new RecipeStepVo();
			step.setStepOrder(i + 1);
			step.setDescription(stepDescriptions.get(i));

			MultipartFile stepPhoto = ((MultipartHttpServletRequest) request).getFile("step_photo_" + (i + 1));
			if (stepPhoto != null && !stepPhoto.isEmpty()) {
				try {
					Map<String, Object> uploadStep = cloudinary.uploader().upload(stepPhoto.getBytes(),
							ObjectUtils.emptyMap());
					step.setStepImgUrl((String) uploadStep.get("secure_url"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			steps.add(step);
		}
		recipe.setSteps(steps);

		// ✅ 저장
		listService.registerRecipe(recipe); // 일반 사용자와 동일하게 저장

		return "redirect:/list"; // 등록 후 메인으로 이동
	}

	/*
	 * 정규화 구조 기반: 레시피 삭제 (AJAX)
	 */
	@PostMapping("/deleteRecipe") // HTML 폼의 th:action 경로와 정확히 일치해야 합니다.
	public String deleteRecipe(@RequestParam("recipeId") int recipeId, HttpSession session) {

		System.out.println("DEBUG: deleteRecipe 메서드 호출됨. recipeId: " + recipeId); // 디버그 로그 추가

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		// 1. 로그인 여부 확인
		if (loginedMemberVo == null) {
			System.out.println("DEBUG: 로그인되지 않은 사용자.");
			return "redirect:/member/loginForm"; // 로그인 페이지로 리다이렉트
		}

		RecipeVo recipe = listService.getRecipeById(recipeId); // 수정된 getCookById 사용 (이름 확인)

		// 2. 레시피 존재 여부 확인
		if (recipe == null) {
			System.out.println("DEBUG: 레시피 ID " + recipeId + "에 해당하는 레시피 없음.");
			return "redirect:/list"; // 레시피 없으면 목록 페이지로
		}

		// 3. 권한 확인: 관리자이거나 레시피 작성자인지
		if (!loginedMemberVo.getUserid().equals("admin") && !loginedMemberVo.getUserid().equals(recipe.getUserid())) {
			System.out.println(
					"DEBUG: 권한 없는 삭제 시도. 로그인 ID: " + loginedMemberVo.getUserid() + ", 레시피 작성자: " + recipe.getUserid());
			return "redirect:/list/details?recipeId=" + recipeId + "&error=unauthorized"; // 권한 없음 에러
		}

		// 4. 서비스 계층으로 삭제 요청 전달
		int result = listService.deleteRecipe(recipeId); // ListService에 deleteRecipe 메서드 구현 필요

		if (result > 0) {
			System.out.println("DEBUG: 레시피 " + recipeId + " 삭제 성공!");
			return "redirect:/list"; // 삭제 성공 시 목록 페이지로 이동
		} else {
			System.out.println("DEBUG: 레시피 " + recipeId + " 삭제 실패!");
			return "redirect:/list/details?recipeId=" + recipeId + "&error=deletionFailed"; // 실패 시 상세 페이지로 돌아가기
		}
	}

	@RequestMapping("/modifyCooks")
	public String getModifyCooks(@RequestParam("recipeId") int recipeId, Model model) {
		// 기본 정보
		RecipeVo recipeInfo = listService.getRecipeById(recipeId);

		if (recipeInfo != null) {
			// 재료 목록
			List<RecipeIngredientVo> ingredients = listService.getIngredientsByRecipeId(recipeId);
			// 단계 목록
			List<RecipeStepVo> steps = listService.getStepsByRecipeId(recipeId);

			model.addAttribute("recipeInfo", recipeInfo);
			model.addAttribute("ingredients", ingredients);
			model.addAttribute("steps", steps);
		}

		return "list/modifyCooks"; // → templates/list/modifyCooks.html
	}

	@PostMapping("/modify")
	public String updateCooks(@RequestParam("recipeId") int recipeId, @RequestParam("title") String title,
			@RequestParam("kind") String kind, 
			//@RequestParam("difficulty") String difficulty, // 난이도 필드 (스키마에 있다면 사용)
			@RequestParam("tip") String tip,

			// 완성 사진 관련
			@RequestParam(value = "complete_photo", required = false) MultipartFile completePhoto,
			@RequestParam("currentCompleteImgUrl") String currentCompleteImgUrl,
			@RequestParam("deleteImageFlag") String deleteImageFlag,

			// 기존 재료 (ID, 이름, 수량)
			@RequestParam(value = "existingIngredientIds", required = false) List<Integer> existingIngredientIds,
			@RequestParam(value = "existingIngredientNames", required = false) List<String> existingIngredientNames,
			@RequestParam(value = "existingIngredientAmounts", required = false) List<String> existingIngredientAmounts,

			// 새로 추가된 재료
			@RequestParam(value = "newIngredientNames", required = false) List<String> newIngredientNames,
			@RequestParam(value = "newIngredientAmounts", required = false) List<String> newIngredientAmounts,

			// 삭제된 재료 ID (콤마로 구분된 문자열)
			@RequestParam(value = "deletedIngredientIds", required = false) String deletedIngredientIdsStr,

			// 기존 단계 (ID, 설명, 기존 이미지 URL, 삭제 플래그)
			@RequestParam(value = "existingStepIds", required = false) List<Integer> existingStepIds,
			@RequestParam(value = "existingStepDescriptions", required = false) List<String> existingStepDescriptions,
			@RequestParam(value = "existingStepCurrentImgUrls", required = false) List<String> existingStepCurrentImgUrls,
			@RequestParam(value = "existingStepDeleteImageFlags", required = false) List<String> existingStepDeleteImageFlags,

			// 새로 추가된 단계
			@RequestParam(value = "newStepDescriptions", required = false) List<String> newStepDescriptions,

			// 모든 단계의 이미지 파일 (기존 수정 or 새로 추가 모두 step_photo[]로 넘어옴)
			@RequestParam(value = "step_photo", required = false) List<MultipartFile> stepPhotos,

			// 삭제된 단계 ID (콤마로 구분된 문자열)
			@RequestParam(value = "deletedStepIds", required = false) String deletedStepIdsStr,

			HttpServletRequest request, Model model) {

		try {
			System.out.println("DEBUG: updateCooks 메서드 시작. RecipeId: " + recipeId);

			// ====================================================================================
			// 1. RecipeVo 기본 정보 구성 및 완성 이미지 처리
			// ====================================================================================
			RecipeVo recipe = new RecipeVo();
			recipe.setRecipeId(recipeId);
			recipe.setTitle(title);
			recipe.setKind(kind);
			// ⭐ 스키마에 difficulty 컬럼이 없다면 이 줄을 주석 처리하거나, DB 스키마에 추가해야 합니다.
			// recipe.setDifficulty(difficulty);
			recipe.setTip(tip);

			// 완성 요리 이미지 처리 로직
			if ("Y".equals(deleteImageFlag)) {
				recipe.setCompleteImgUrl(null);
				System.out.println("DEBUG: 완성 이미지 삭제 플래그 'Y' 감지. 이미지 URL null로 설정.");
			} else if (completePhoto != null && !completePhoto.isEmpty()) {
				Map<String, Object> uploadResult = cloudinary.uploader().upload(completePhoto.getBytes(),
						ObjectUtils.emptyMap());
				recipe.setCompleteImgUrl((String) uploadResult.get("secure_url"));
				System.out.println("DEBUG: 새로운 완성 이미지 업로드: " + recipe.getCompleteImgUrl());
			} else {
				recipe.setCompleteImgUrl(currentCompleteImgUrl);
				System.out.println("DEBUG: 기존 완성 이미지 유지: " + recipe.getCompleteImgUrl());
			}

			// ====================================================================================
			// 2. 재료 처리 (기존 수정, 새로 추가)
			// ====================================================================================
			List<RecipeIngredientVo> ingredientsToProcess = new ArrayList<>();

			if (existingIngredientIds != null) {
				for (int i = 0; i < existingIngredientIds.size(); i++) {
					RecipeIngredientVo ing = new RecipeIngredientVo();
					ing.setIngredientId(existingIngredientIds.get(i));
					ing.setRecipeId(recipeId);
					ing.setName(existingIngredientNames.get(i));
					ing.setAmount(existingIngredientAmounts.get(i));
					ingredientsToProcess.add(ing);
					System.out.println(
							"DEBUG: 기존 재료 추가 (ID: " + ing.getIngredientId() + ", Name: " + ing.getName() + ")");
				}
			}

			if (newIngredientNames != null) {
				for (int i = 0; i < newIngredientNames.size(); i++) {
					RecipeIngredientVo ing = new RecipeIngredientVo();
					ing.setRecipeId(recipeId);
					ing.setName(newIngredientNames.get(i));
					ing.setAmount(newIngredientAmounts.get(i));
					ingredientsToProcess.add(ing);
					System.out.println("DEBUG: 새 재료 추가 (Name: " + ing.getName() + ")");
				}
			}
			recipe.setIngredients(ingredientsToProcess);

			// 삭제된 재료 ID 목록 파싱
			List<Integer> deletedIngredientIdList = new ArrayList<>();
			if (deletedIngredientIdsStr != null && !deletedIngredientIdsStr.isEmpty()) {
				String[] ids = deletedIngredientIdsStr.split(",");
				for (String id : ids) {
					try {
						deletedIngredientIdList.add(Integer.parseInt(id.trim()));
						System.out.println("DEBUG: 삭제될 재료 ID 감지: " + id.trim());
					} catch (NumberFormatException e) {
						System.err.println("WARN: 유효하지 않은 삭제된 재료 ID: " + id + " - " + e.getMessage());
					}
				}
			}

			// ====================================================================================
			// 3. 단계 처리 (기존 수정, 새로 추가, 이미지 업로드/삭제)
			// ====================================================================================
			List<RecipeStepVo> stepsToProcess = new ArrayList<>();
			int currentStepPhotoIndex = 0;

			if (existingStepIds != null) {
				for (int i = 0; i < existingStepIds.size(); i++) {
					RecipeStepVo step = new RecipeStepVo();
					step.setStepId(existingStepIds.get(i));
					step.setRecipeId(recipeId);
					step.setStepOrder(i + 1);
					step.setDescription(existingStepDescriptions.get(i));

					String currentStepImgUrl = existingStepCurrentImgUrls.get(i);
					String deleteStepImageFlag = existingStepDeleteImageFlags.get(i);

					MultipartFile stepPhoto = null;
					if (stepPhotos != null && currentStepPhotoIndex < stepPhotos.size()) {
						stepPhoto = stepPhotos.get(currentStepPhotoIndex);
						if (stepPhoto.isEmpty()) {
							stepPhoto = null;
						}
					}

					if ("Y".equals(deleteStepImageFlag)) {
						step.setStepImgUrl(null);
						System.out.println("DEBUG: 단계 " + step.getStepId() + " 이미지 삭제 플래그 'Y' 감지.");
					} else if (stepPhoto != null) {
						try {
							Map<String, Object> uploadStep = cloudinary.uploader().upload(stepPhoto.getBytes(),
									ObjectUtils.emptyMap());
							step.setStepImgUrl((String) uploadStep.get("secure_url"));
							System.out
									.println("DEBUG: 단계 " + step.getStepId() + " 새로운 이미지 업로드: " + step.getStepImgUrl());
						} catch (IOException e) {
							System.err.println(
									"ERROR: 단계 이미지 업로드 중 오류 발생 (Step ID: " + step.getStepId() + "): " + e.getMessage());
							step.setStepImgUrl(currentStepImgUrl);
						}
					} else {
						step.setStepImgUrl(currentStepImgUrl);
						System.out.println("DEBUG: 단계 " + step.getStepId() + " 기존 이미지 유지: " + step.getStepImgUrl());
					}
					stepsToProcess.add(step);
					currentStepPhotoIndex++;
				}
			}

			if (newStepDescriptions != null) {
				int startIndex = (existingStepIds != null) ? existingStepIds.size() : 0;
				for (int i = 0; i < newStepDescriptions.size(); i++) {
					RecipeStepVo step = new RecipeStepVo(); // ⭐ Type mismatch: RecipeVo cannot be converted to RecipeStepVo
					// ⭐ 위에 RecipeVo로 잘못 선언되어 있습니다. RecipeStepVo로 수정해야 합니다.
					// RecipeStepVo step = new RecipeStepVo(); // <-- 이렇게 수정
					step.setRecipeId(recipeId);
					step.setStepOrder(startIndex + i + 1);
					step.setDescription(newStepDescriptions.get(i));

					MultipartFile newStepPhoto = null;
					if (stepPhotos != null && currentStepPhotoIndex < stepPhotos.size()) {
						newStepPhoto = stepPhotos.get(currentStepPhotoIndex);
						if (newStepPhoto.isEmpty()) {
							newStepPhoto = null;
						}
					}

					if (newStepPhoto != null) {
						try {
							Map<String, Object> uploadStep = cloudinary.uploader().upload(newStepPhoto.getBytes(),
									ObjectUtils.emptyMap());
							step.setStepImgUrl((String) uploadStep.get("secure_url"));
							System.out.println(
									"DEBUG: 새 단계 " + step.getStepOrder() + " 이미지 업로드: " + step.getStepImgUrl());
						} catch (IOException e) {
							System.err.println("ERROR: 새 단계 이미지 업로드 중 오류 발생 (Step Order: " + step.getStepOrder() + "): "
									+ e.getMessage());
							step.setStepImgUrl(null);
						}
					} else {
						step.setStepImgUrl(null);
						System.out.println("DEBUG: 새 단계 " + step.getStepOrder() + " 이미지 없음.");
					}
					stepsToProcess.add(step);
					currentStepPhotoIndex++;
				}
			}
			recipe.setSteps(stepsToProcess);

			// 삭제된 단계 ID 목록 파싱
			List<Integer> deletedStepIdList = new ArrayList<>();
			if (deletedStepIdsStr != null && !deletedStepIdsStr.isEmpty()) {
				String[] ids = deletedStepIdsStr.split(",");
				for (String id : ids) {
					try {
						deletedStepIdList.add(Integer.parseInt(id.trim()));
						System.out.println("DEBUG: 삭제될 단계 ID 감지: " + id.trim());
					} catch (NumberFormatException e) {
						System.err.println("WARN: 유효하지 않은 삭제된 단계 ID: " + id + " - " + e.getMessage());
					}
				}
			}

			// ====================================================================================
			// 4. 서비스 호출
			// ====================================================================================
			boolean isUpdated = listService.updateRecipe(recipe, deletedIngredientIdList, deletedStepIdList);

			if (!isUpdated) {
				System.out.println("DEBUG: 레시피 수정 실패.");
				model.addAttribute("errorMessage", "레시피 수정에 실패했습니다.");
				return "list/modifyCooks";
			}

			System.out.println("DEBUG: 레시피 수정 성공! RecipeId: " + recipeId);
			return "redirect:/list/details?recipeId=" + recipeId;

		} catch (IOException e) {
			System.err.println("ERROR: 파일 업로드 중 오류 발생: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("errorMessage", "파일 업로드 중 오류 발생");
			return "list/modifyCooks";
		} catch (Exception e) {
			System.err.println("ERROR: 레시피 수정 중 알 수 없는 오류 발생: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("errorMessage", "레시피 수정 중 오류 발생");
			return "list/modifyCooks";
		}
	}

	@RequestMapping("/filter")
	public String getCooksByCategory(@RequestParam("category") String category,
			@RequestParam(value = "page", defaultValue = "1") int page,
			@RequestParam(value = "pageSize", defaultValue = "18") int pageSize, Model model) {
		// 총 개수 조회
		int totalCookCount = listService.getTotalRecipeCountByCategory(category);
		int totalPage = (int) Math.ceil((double) totalCookCount / pageSize);

		if (page < 1)
			page = 1;
		if (page > totalPage)
			page = totalPage;

		// 레시피 목록 조회
		List<RecipeVo> cookList = listService.getRecipesByCategory(category, page, pageSize);

		model.addAttribute("category", category);
		model.addAttribute("cookList", cookList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("totalCookCount", totalCookCount);
		model.addAttribute("pageSize", pageSize);

		return "list/category";
	}

	/*
	 * 레시피 검색
	 */
	@GetMapping("/search")
	public String search(@RequestParam("word") String word, Model model) {

		// 검색 결과 가져오기 (recipe.title 기준)
		List<RecipeVo> searchResults = listService.searchCooksByName(word);

		model.addAttribute("searchResults", searchResults);
		model.addAttribute("word", word);

		return "search/searchResult"; // searchResult.html
	}

}
