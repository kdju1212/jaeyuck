/**
 * 
 */
function redirectToInfo(message_no) {
	window.location.href = `${contextPath}/message/detail?message_no=${message_no}`;
}

function titleCheck() {
	// 제목 입력 필드 값 가져오기
	var get_id = document.frm.get_id.value.trim(); // 공백도 고려하여 제목 값 가져오기
	if (get_id === "") {
		// 제목이 비어있다면 경고 메시지
		alert("수신자 ID를 입력해야 합니다");
		document.frm.get_id.focus(); // 제목 필드에 포커스를 맞춤
		return false; // 폼 제출을 막음
	}
	// 제목 입력 필드 값 가져오기
	var title = document.frm.title.value.trim(); // 공백도 고려하여 제목 값 가져오기
	if (title === "") {
		// 제목이 비어있다면 경고 메시지
		alert("제목을 입력해야 합니다");
		document.frm.title.focus(); // 제목 필드에 포커스를 맞춤
		return false; // 폼 제출을 막음
	}
	return true; // 제목이 올바르게 입력되었으면 폼을 제출
}
