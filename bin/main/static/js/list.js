/**
 * 
 */
function redirectToInfo(cook_no, cookName) {
	fetch(`${contextPath}/list/incrementReadCount?cook_no=${cook_no}&cookName=${encodeURIComponent(cookName)}`)
		.then(response => {
			if (response.ok) {
				// read_count가 증가한 경우에만 상세 페이지로 이동
				window.location.href = `${contextPath}/list/details?cook_no=${cook_no}&cookName=${encodeURIComponent(cookName)}`;
			} else {
				// 요청 실패 시 경고 메시지 표시
				alert("read_count 증가에 실패했습니다.");
			}
		})
		.catch(error => {
			console.error("서버 통신 중 오류 발생:", error);
			alert("서버와의 통신 중 문제가 발생했습니다.");
		});
}
