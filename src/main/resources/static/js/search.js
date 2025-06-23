function submitSearchForm(event) {
	event.preventDefault(); // 기본 동작 방지 (이미지 클릭 후 폼 제출 방지)

	var searchWord = document.getElementById("word").value.trim(); // 검색어를 가져옵니다.

	// 폼 제출
	document.getElementById("searchForm").submit(); // 검색 폼 제출
}

function checkLoginBeforeRegister() {
	var userId = document.getElementById("userId").value; // 로그인한 사용자의 ID 가져오기
	if (!userId) { // 로그인되지 않았다면
		var isLogin = confirm("로그인이 필요합니다. 로그인하시겠습니까?");
		if (isLogin) {
			// 로그인 페이지로 리디렉션
			window.location.href = contextPath + "/member/loginForm"; // contextPath 변수를 사용
		} else {
			// 아무 동작도 하지 않고 그대로 페이지에 남아있음
			return false;
		}
	} else {
		// 로그인 되어있다면, 등록 폼으로 리디렉션
		window.location.href = contextPath + "/list/registerForm";
	}
}

function fnEnterkey(event) {
	var form = document.getElementById('searchForm');
	if (event.keyCode == 13) {
		event.preventDefault();
		form.submit();
	}
} 