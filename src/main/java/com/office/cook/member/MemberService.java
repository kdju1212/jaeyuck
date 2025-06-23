package com.office.cook.member;

import java.security.SecureRandom;
import java.util.Date;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

	@Autowired
	MemberDao memberDao;
	@Autowired
	JavaMailSenderImpl javaMailSenderImpl;

	/*
	 * 로그인 확인
	 */
	public MemberVo loginConfirm(MemberVo memberVo) {

		MemberVo loginedMemberVo = memberDao.selectUser(memberVo);

		return loginedMemberVo;

	}

	/*
	 * 회원가입 확인
	 */
	public int joinConfirm(MemberVo memberVo) {
		// System.out.println("[UserMemberService] createAccountConfirm()");
		// 중복 아이디를 확인합니다. isMember가 true이면 이미 사용중인 아이디이고,
		// false이면 사용가능한 아이디 입니다.
		boolean isMember = memberDao.isUserMember(memberVo.getUserid());

		if (!isMember) {
			int result = memberDao.insertUserAccount(memberVo);

			if (result > 0)
				// ok
				return 1;

			else
				// fail
				return 0;

		} else {
			// exis
			return -1;

		}
	}// end of createAccountConfirm()

	/*
	 * 계정 수정 확인
	 */
	public int memberUpdateConfirm(MemberVo memberVo) {
		// System.out.println("[UserMemberService] modifyAccountConfirm()");

		return memberDao.memberUpdateAccount(memberVo);

	}

	/*
	 * 로그인 계정 확인
	 */
	public MemberVo getLoginedMemberVo(String userid) {

		return memberDao.selectUser(userid);

	}

	/*
	 * 아이디 중복확인
	 */
	public boolean isUserIdAvailable(String userid) {
		// DAO를 통해 아이디 중복 여부 확인
		int count = memberDao.checkUserId(userid);
		return count == 0; // 중복되지 않으면 사용 가능 (count가 0이면 사용 가능)
	}

	/*
	 * 계정 탈퇴
	 */
	public int deleteConfirm(String userid) {
		// System.out.println("Service deleteConfirm 호출");

		try {
			// DAO의 deleteConfirm 메서드를 호출하여 회원을 삭제
			int result = memberDao.deleteConfirm(userid);

			// 삭제가 성공적이면 result를 반환
			if (result > 0) {
				return result; // 탈퇴 성공
			} else {
				return 0; // 탈퇴 실패 (삭제된 행이 없을 때)
			}
		} catch (Exception e) {
			// 예외 처리: 예외 발생 시 0을 반환
			e.printStackTrace();
			return 0; // 예외 발생 시 실패 처리
		}
	}

	/*
	 * 계정탈퇴 전 비밀번호 받기
	 */
	public int deletePwdCheck(MemberVo memberVo, String userid) {
		try {
			// DAO의 deletePwdCheck 메서드를 호출하여 비밀번호를 확인
			return memberDao.deletePwdCheck(memberVo, userid);
		} catch (Exception e) {
			// 예외 처리: 예외 발생 시 -1 반환
			e.printStackTrace();
			return -1; // 예외 발생 시 처리되지 않았다는 의미로 -1 반환
		}
	}

	/*
	 * 비밀번호 변경
	 */
	public int memberPwdUpdateConfirm(MemberVo memberVo, String new_pwd) {

		return memberDao.memberPwdUpdateConfirm(memberVo, new_pwd);
	}

	/*
	 * 비밀번호 찾기
	 */
	public int findPwdConfirm(MemberVo memberVo) {

		MemberVo selectedUserMemberVo = memberDao.selectUser(memberVo.getUserid(), memberVo.getName(),
				memberVo.getEmail());

		int result = 0;

		if (selectedUserMemberVo != null) {

			String newPassword = createNewPassword();
			result = memberDao.updatePassword(memberVo.getUserid(), newPassword);

			if (result > 0)
				sendNewPasswordByMail(memberVo.getEmail(), newPassword);
		}

		return result;

	}

	/*
	 * 무작위 비밀번호 만들기
	 */
	private String createNewPassword() {
		System.out.println("[UserMemberService] createNewPassword()");

		char[] chars = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
				'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

		StringBuffer stringBuffer = new StringBuffer();
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.setSeed(new Date().getTime());

		int index = 0;
		int length = chars.length;
		for (int i = 0; i < 8; i++) {
			index = secureRandom.nextInt(length);

			if (index % 2 == 0)
				stringBuffer.append(String.valueOf(chars[index]).toUpperCase());
			else
				stringBuffer.append(String.valueOf(chars[index]).toLowerCase());

		}

		System.out.println("[UserMemberService] NEW PASSWORD: " + stringBuffer.toString());

		return stringBuffer.toString();

	}

	/*
	 * 메일 보내기
	 */
	private void sendNewPasswordByMail(String toMailAddr, String newPassword) {

		final MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {

			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				// true 인자는멀티파트 메시지를 활성해주는 역할.
				final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				// mimeMessageHelper.setTo("djkim0509@gmail.com");
				mimeMessageHelper.setTo(toMailAddr);
				mimeMessageHelper.setSubject("새 비밀번호 안내입니다.");
				mimeMessageHelper.setText("새 비밀번호 : " + newPassword, true);
				// 위의 문장 setText(... , true)에서 true는 본문을 HTML 형식으로 처리하도록 지정함.

			}

		};
		javaMailSenderImpl.send(mimeMessagePreparator);

	}

}
