package com.office.cook.member;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/member")
public class MemberController {

	@Autowired
	MemberService memberService;

	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public String home() {
		// System.out.println("[MemberController] home()");

		String nextPage = "member/home";

		return nextPage;

	}

	/*
	 * 로그인
	 */
	@GetMapping("/loginForm")
	public String loginForm() {
		// System.out.println("[AdminMemberController] loginFrom()");

		String nextPage = "member/Login";

		return nextPage;
	}

	/*
	 * 로그인 확인
	 */
	// @RequestMapping(value = "/loginConfirm",
	// method = RequestMethod.POST)
	@PostMapping("/loginConfirm")
	public String loginConfirm(MemberVo memberVo, HttpSession session, Model model) {
		// System.out.println("[UserMemberController] loginConfirm()");

		// login ok
		// String nextPage = "member/login_ok";
		String nextPage = "redirect:/list";

		MemberVo loginedMemberVo = memberService.loginConfirm(memberVo);

		if (loginedMemberVo == null) {
			// login ng
			model.addAttribute("message", "아이디 또는 비밀번호가 일치하지 않습니다.");
			nextPage = "member/Login";

		} else {
			session.setAttribute("loginedMemberVo", loginedMemberVo);
			session.setMaxInactiveInterval(60 * 30);

		}

		return nextPage;

	}

	/*
	 * 회원 가입
	 */
	@GetMapping("/join")
	public String createAccountForm() {
		// System.out.println("[MemberController] createAccountForm()");

		String nextPage = "member/join";

		return nextPage;
	}

	/*
	 * 회원 가입 확인
	 */
	// @RequestMapping(value = "/createAccountConfirm",
	// method = RequestMethod.POST)
	@PostMapping("/joinConfirm")
	public String joinConfirm(MemberVo memberVo) {
		// System.out.println("[UserMemberController] createAccountConfirm()");

		// join ok
		String nextPage = "member/Login";

		int result = memberService.joinConfirm(memberVo);

		if (result <= 0)
			// join ng
			nextPage = "member/join";

		return nextPage;

	}

	/*
	 * 아이디 중복체크
	 */
	@RequestMapping("/idCheck")
	public String idCheck(@RequestParam("userid") String userid, Model model) {
		// 중복 체크 서비스 호출
		boolean isAvailable = memberService.isUserIdAvailable(userid);

		// 결과를 모델에 담아 JSP로 전달
		model.addAttribute("userid", userid);
		model.addAttribute("isAvailable", isAvailable ? -1 : 1); // 1은 중복, -1은 사용 가능

		return "member/idcheck"; // 중복 여부에 대한 결과를 보여줄 JSP
	}

	/*
	 * 로그아웃 확인
	 */
	@GetMapping("/logoutConfirm")
	public String logoutConfirm(HttpSession session) {
		// System.out.println("[UserMemberController] logoutConfirm()");

		// String nextPage = "redirect:/"; 가능함
		String nextPage = "redirect:/list";

		session.removeAttribute("loginedMemberVo");
		session.invalidate();

		return nextPage;

	}

	/*
	 * 계정 수정
	 */
	@GetMapping("/memberUpdateForm")
	public String memberUpdateForm(HttpSession session) {
		// System.out.println("[MemberController] modifyAccountForm()");

		String nextPage = "member/memberUpdate";

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		if (loginedMemberVo == null)
			nextPage = "redirect:/member/loginForm";

		return nextPage;

	}

	/*
	 * 회원 정보 수정 확인
	 */

	@PostMapping("/memberUpdateConfirm")
	public String memberUpdateConfirm(MemberVo memberVo, HttpSession session) {

		String nextPage = "member/mypage";

		int result = memberService.memberUpdateConfirm(memberVo);

		if (result > 0) {
			MemberVo loginedMemberVo = memberService.getLoginedMemberVo(memberVo.getUserid());

			session.setAttribute("loginedMemberVo", loginedMemberVo);
			session.setMaxInactiveInterval(60 * 30);

		} else {
			nextPage = "member/modify_account_ng";

		}

		return nextPage;
	}

	/*
	 * 비밀번호 수정
	 */
	@GetMapping("/pwdUpdate")
	public String pwdUpdate(HttpSession session) {

		String nextPage = "member/pwdupdate";

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		if (loginedMemberVo == null)
			nextPage = "redirect:/member/loginForm";

		return nextPage;
	}

	/*
	 * 비밀번호 수정 확인
	 */
	@PostMapping("/pwdUpdateConfirm")
	public String pwdUpadteConfirm(HttpSession session, MemberVo memberVo, @RequestParam("new_pwd") String new_pwd,
			Model model) {
		String nextPage = "member/ng";

		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		int pwdCheck = memberService.deletePwdCheck(memberVo, loginedMemberVo.getUserid());

		if (pwdCheck == 1) {
			int result = memberService.memberPwdUpdateConfirm(memberVo, new_pwd);

			if (result > 0) {
				loginedMemberVo = memberService.getLoginedMemberVo(memberVo.getUserid());

				// 30분 움직임 없으면 세션 만료
				// session.setAttribute("loginedMemberVo", loginedMemberVo);
				// session.setMaxInactiveInterval(60 * 30);

				session.removeAttribute("loginedMemberVo");
				session.invalidate();

				nextPage = "member/Login";

			}
		} else if (pwdCheck == 0) {
			// 비밀번호 틀렸을때

			nextPage = "member/pwdupdate";
			model.addAttribute("message", "비밀번호가 일치하지 않습니다.");

		}
		return nextPage;
	}

	/*
	 * 계정 탈퇴
	 */
	@PostMapping("/deleteConfirm")
	public String deleteConfirm(MemberVo memberVo, HttpSession session) {
		String nextPage = "member/ng"; // 실패 시 이동할 페이지 (ng 페이지)

		// 세션에서 로그인한 사용자 정보 가져오기
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");

		if (loginedMemberVo != null) {
			// 비밀번호 확인 서비스 호출
			int pwdCheck = memberService.deletePwdCheck(memberVo, loginedMemberVo.getUserid());

			// 비밀번호가 맞으면 탈퇴 처리
			if (pwdCheck == 1) {
				// 회원 탈퇴 서비스 호출
				int result = memberService.deleteConfirm(loginedMemberVo.getUserid());

				if (result > 0) {
					// 탈퇴 성공 시 세션에서 로그인 정보 제거
					session.removeAttribute("loginedMemberVo");
					nextPage = "redirect:/list"; // 탈퇴 후 홈으로 리다이렉트
				} else {
					// 탈퇴 실패 시 실패 메시지
					nextPage = "member/ng"; // 실패 페이지
				}
			} else if (pwdCheck == 0) {
				// 비밀번호 불일치 처리
				nextPage = "member/ng"; // 비밀번호 불일치 시 ng 페이지

				session.setAttribute("errorMessage", "비밀번호가 일치하지 않습니다.");
			} else if (pwdCheck == -1) {
				nextPage = "member/ng"; // 회원을 찾을 수 없을 때 ng 페이지

				session.setAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
			}
		}

		return nextPage; // 페이지 리턴 (성공 또는 실패에 따라 다름)
	}

	/*
	 * 마이페이지로 이동
	 */
	@GetMapping("/myPage")
	public String myPage() {
		return "member/mypage";
	}

	/*
	 * 탈퇴 비밀번호 확인으로 이동
	 */
	@GetMapping("/deletePwdCheck")
	public String deletePwdCheck(HttpSession session) {
		MemberVo loginedMemberVo = (MemberVo) session.getAttribute("loginedMemberVo");
		return "member/deletePwdCheck";
	}

	/*
	 * 비밀번호 찾기 폼
	 */
	@GetMapping("/findPwdForm")
	public String findePwdForm() {

		String nextPage = "member/findpwdform";

		return nextPage;

	}

	/*
	 * 비밀번호 찾기 확인
	 */
	@PostMapping("/findPwdConfirm")
	public String findPwdConfirm(MemberVo memberVo) {

		String nextPage = "member/Login";

		int result = memberService.findPwdConfirm(memberVo);

		if (result <= 0)
			nextPage = "member/ng";

		return nextPage;

	}

}
