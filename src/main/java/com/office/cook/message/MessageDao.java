package com.office.cook.message;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.office.cook.member.MemberVo;

@Repository
public class MessageDao {

	@Autowired
	JdbcTemplate jdbcTemplate;

	/*
	 * id로 받은 메시지 출력
	 */
	public List<MessageVo> getMessagesByPage(String userId, int offset, int pageSize) {
		String sql = "SELECT * FROM ("
				+ "  SELECT ROW_NUMBER() OVER (ORDER BY m.send_date DESC) AS rn, m.* FROM message m "
				+ "  WHERE m.get_id = ? " + ") WHERE rn > ? AND rn <= ?";

		return jdbcTemplate.query(sql, new Object[] { userId, offset, offset + pageSize }, (rs, rowNum) -> {
			MessageVo message = new MessageVo();
			message.setMessage_no(rs.getInt("message_no"));
			message.setSend_id_name(rs.getString("send_id_name"));
			message.setTitle(rs.getString("title"));
			message.setSend_date(rs.getString("send_date"));
			return message;
		});
	}

	/*
	 * 메시지 총 개수 가져오기
	 */
	public int getTotalMessageCount(String getId) {
		String sql = "SELECT COUNT(*) FROM message WHERE get_id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { getId }, Integer.class);
	}

	/*
	 * 보낸 쪽지 개수
	 */
	public int sendTotalMessageCount(String userid) {
		String sql = "SELECT COUNT(*) FROM message WHERE send_id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] { userid }, Integer.class);
	}

	/*
	 * id로 보낸 메시지 출력
	 */
	public List<MessageVo> sendMessagesByPage(String userid, int offset, int pageSize) {

		String sql = "SELECT * FROM ("
				+ "  SELECT ROW_NUMBER() OVER (ORDER BY m.send_date DESC) AS rn, m.* FROM message m "
				+ "  WHERE m.send_id = ? " + ") WHERE rn > ? AND rn <= ?";

		return jdbcTemplate.query(sql, new Object[] { userid, offset, offset + pageSize }, (rs, rowNum) -> {
			MessageVo message = new MessageVo();
			message.setMessage_no(rs.getInt("message_no"));
			message.setGet_id_name(rs.getString("get_id_name"));
			message.setTitle(rs.getString("title"));
			message.setSend_date(rs.getString("send_date"));
			return message;
		});
	}

	/*
	 * 메시지 상세보기
	 */
	public MessageVo getMessageByNo(int message_no) {
		String sql = "SELECT * FROM message WHERE message_no = ?";

		List<MessageVo> messageVos = null;
		try {
			RowMapper<MessageVo> rowMapper = BeanPropertyRowMapper.newInstance(MessageVo.class);
			messageVos = jdbcTemplate.query(sql, rowMapper, message_no);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return messageVos.size() > 0 ? messageVos.get(0) : null;
	}

	/*
	 * 메시지 작성 확인
	 */
	public int messageSend(MessageVo messageVo) {

		String user_sql = "SELECT user_no, name FROM member WHERE userid = ?";

		// 첫 번째 SQL 실행하여 send_user 조회
		MemberVo send_user = null;
		try {
			send_user = jdbcTemplate.queryForObject(user_sql, new Object[] { messageVo.getSend_id() },
					new RowMapper<MemberVo>() {
						@Override
						public MemberVo mapRow(ResultSet rs, int rowNum) throws SQLException {
							MemberVo member = new MemberVo();
							member.setUser_no(rs.getInt("user_no"));
							member.setName(rs.getString("name"));
							return member;
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
			return -1; // 오류 발생 시 처리
		}

		// 두 번째 SQL 실행하여 get_user 조회
		MemberVo get_user = null;
		try {
			get_user = jdbcTemplate.queryForObject(user_sql, new Object[] { messageVo.getGet_id() },
					new RowMapper<MemberVo>() {
						@Override
						public MemberVo mapRow(ResultSet rs, int rowNum) throws SQLException {
							MemberVo member = new MemberVo();
							member.setUser_no(rs.getInt("user_no"));
							member.setName(rs.getString("name"));
							return member;
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
			return -1; // 오류 발생 시 처리
		}

		// message 테이블에 데이터 삽입
		String sql = "INSERT INTO message (message_no, send_id_no, get_id_no, send_id_name, get_id_name, send_id, get_id,title, content, send_date) "
				+ "VALUES (message_seq.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, sysdate)";

		int result = -1;
		try {
			result = jdbcTemplate.update(sql, send_user.getUser_no(), get_user.getUser_no(), send_user.getName(),
					get_user.getName(), messageVo.getSend_id(), messageVo.getGet_id(), messageVo.getTitle(),
					messageVo.getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/*
	 * 쪽지 삭제
	 */
	public int messageDelete(int message_no) {

		String sql = "delete from message where message_no = ?";

		int result = jdbcTemplate.update(sql, message_no);

		return result;
	}

}
