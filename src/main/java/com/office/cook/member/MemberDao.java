package com.office.cook.member;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDao {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	PasswordEncoder passwordEncoder;

	/*
	 * 중복체크
	 */
	public boolean isUserMember(String userid) {

		String sql = "SELECT COUNT(*) FROM member " + "WHERE userid = ?";

		int result = jdbcTemplate.queryForObject(sql, Integer.class, userid);
		// sql : SQL문 , Integer.class : 쿼리 실행후 반환되는 데이터 타입,
		// u_m_id : 관리자가 입력한 아이디로서 매개변수에 있는 String u_m_id를 가져옴.
		// queryForObject()가 1을 반환한다면 관리자가 입력한 아이디는 이미 사용중인
		// 아이디로 회원가입을 할 수 없고, 0이면 사용중인 아이디가 아니므로 회원가입이 가능합니다.

		return result > 0 ? true : false;
	}

	/*
	 * user 정보 불러오기 (로그인)
	 */
	public MemberVo selectUser(MemberVo memberVo) {

		String sql = "SELECT * FROM member " + "WHERE userid = ?";

		List<MemberVo> memberVos = null;

		try {

			RowMapper<MemberVo> rowMapper = BeanPropertyRowMapper.newInstance(MemberVo.class);
			memberVos = jdbcTemplate.query(sql, rowMapper, memberVo.getUserid());
			// matches는 암호화된 비밀번호를 복원함 // loginForm pw , 암호화된 비밀번호
			if (!passwordEncoder.matches(memberVo.getPwd(), memberVos.get(0).getPwd()))
				memberVos.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return memberVos.size() > 0 ? memberVos.get(0) : null;

	}

	/*
	 * userid로만 user 정보 불러오기
	 */
	public MemberVo selectUser(String userid) {
		// System.out.println("[UserMemberDao] selectUser()");

		String sql = "SELECT * FROM member " + "WHERE userid = ?";

		List<MemberVo> memberVos = null;

		try {

			RowMapper<MemberVo> rowMapper = BeanPropertyRowMapper.newInstance(MemberVo.class);
			memberVos = jdbcTemplate.query(sql, rowMapper, userid);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return memberVos.size() > 0 ? memberVos.get(0) : null;

	}

	/*
	 * 회원가입
	 */
	public int insertUserAccount(MemberVo memberVo) {

		// reg_date와 mod_date는 NOW()로 자동 입력되므로 ? 파라미터에서 제외
		String sql = "INSERT INTO member(user_no,name, userid, pwd, email, phone, admin, gender, reg_date, mod_date) "
				+ "VALUES(user_seq.NEXTVAL,?, ?, ?, ?, ?, ?, ?, SYSDATE, SYSDATE)";

		int result = -1;

		try {
			// reg_date와 mod_date는 전달하지 않음
			result = jdbcTemplate.update(sql, memberVo.getName(), memberVo.getUserid(),
					passwordEncoder.encode(memberVo.getPwd()), memberVo.getEmail(), memberVo.getPhone(),
					memberVo.getAdmin(), memberVo.getGender());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/*
	 * 계정 수정 (비밀번호 제외)
	 */
	public int memberUpdateAccount(MemberVo memberVo) {

		String sql = "UPDATE member SET name = ?, " + "email = ?, " + "phone = ?, " + "gender = ?, "
				+ "mod_date = SYSDATE " + "WHERE userid = ?";

		int result = -1;

		try {

			result = jdbcTemplate.update(sql, memberVo.getName(), memberVo.getEmail(), memberVo.getPhone(),
					memberVo.getGender(), memberVo.getUserid());

		} catch (Exception e) {
			e.printStackTrace();

		}

		return result;
	}

	/*
	 * 중복체크 (위랑 동일인데 없애도 될듯)
	 */
	public int checkUserId(String userid) {
		// SQL 쿼리를 실행하여 아이디가 중복되는지 체크
		String sql = "SELECT COUNT(*) FROM member WHERE userid = ?";
		return jdbcTemplate.queryForObject(sql, Integer.class, userid);
	}

	/*
	 * 탈퇴
	 */
	public int deleteConfirm(String userid) {
		String sql = "delete from member where userid = ?";
		int result = -1;
		try {

			result = jdbcTemplate.update(sql, userid);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/*
	 * 비밀번호 확인 메서드
	 */
	public int deletePwdCheck(MemberVo memberVo, String userid) {
		String sql = "SELECT pwd FROM member WHERE userid = ?";
		List<MemberVo> memberVos = null;

		try {
			// RowMapper를 사용하여 MemberVo 객체로 결과 매핑
			RowMapper<MemberVo> rowMapper = BeanPropertyRowMapper.newInstance(MemberVo.class);

			// 해당 userid로 비밀번호를 조회
			memberVos = jdbcTemplate.query(sql, rowMapper, userid);

			// 회원이 존재하지 않으면 -1 반환
			if (memberVos.isEmpty()) {
				return -1; // 회원을 찾을 수 없음
			}

			// 입력된 비밀번호와 저장된 비밀번호를 비교
			if (!passwordEncoder.matches(memberVo.getPwd(), memberVos.get(0).getPwd())) {
				return 0; // 비밀번호 불일치
			}

			// 비밀번호가 일치하면 1 반환
			return 1; // 비밀번호 확인 성공

		} catch (Exception e) {
			// 예외 처리
			e.printStackTrace();
			return -2; // 예외 발생
		}
	}

	/*
	 * 비밀번호 변경
	 */
	public int memberPwdUpdateConfirm(MemberVo memberVo, String new_pwd) {

		String sql = "update member set pwd = ?,mod_date = SYSDATE where userid = ?";

		int result = -1;
		try {

			result = jdbcTemplate.update(sql, passwordEncoder.encode(new_pwd), memberVo.getUserid());

		} catch (Exception e) {
			e.printStackTrace();

		}

		return result;
	}

	/*
	 * id,name,email로 member 찾기
	 */
	public MemberVo selectUser(String userid, String name, String email) {

		String sql = "SELECT * FROM member WHERE userid = ? AND name = ? AND email = ?";

		List<MemberVo> memberVos = null;

		try {

			RowMapper<MemberVo> rowMapper = BeanPropertyRowMapper.newInstance(MemberVo.class);
			memberVos = jdbcTemplate.query(sql, rowMapper, userid, name, email);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return memberVos.size() > 0 ? memberVos.get(0) : null;

	}

	/*
	 * 초기화된것으로 비밀번호 변경 
	 */
	public int updatePassword(String userid, String newPassword) {

		String sql = "update member SET pwd = ?, mod_date = sysdate WHERE userid = ?";

		int result = -1;

		try {

			result = jdbcTemplate.update(sql, passwordEncoder.encode(newPassword), userid);

		} catch (Exception e) {
			e.printStackTrace();

		}
		return result;
	}

}
