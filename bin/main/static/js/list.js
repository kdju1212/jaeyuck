// /js/list.js

document.addEventListener('DOMContentLoaded', function() {
	const cookItems = document.querySelectorAll('.cook-item.clickable');

	cookItems.forEach(item => {
		item.addEventListener('click', function() {
			// data-* 속성에서 값 읽어오기
			const recipeId = this.dataset.recipeId;       // data-recipe-id
			const recipeTitle = this.dataset.recipeTitle; // data-recipe-title

			// 조회수 증가 및 상세 페이지로 이동하는 로직 호출
			// 함수 이름을 'handleRecipeClick' 등으로 변경하는 것을 고려할 수 있습니다.
			// 여기서는 기존 함수 이름을 사용하되 내부 로직을 수정합니다.

			// 1. 조회수 증가 요청
			// 컨트롤러의 incrementReadCount 메서드가 recipeId만 받도록 수정했을 경우
			fetch(`${contextPath}/list/incrementReadCount?cook_no=${recipeId}`)
				.then(response => {
					if (response.ok) {
						// 2. 조회수 증가 성공 후 상세 페이지로 이동
						// 컨트롤러의 getCookDetails 메서드가 recipeId와 title을 받도록 수정했을 경우
						window.location.href = `${contextPath}/list/details?recipeId=${recipeId}&title=${encodeURIComponent(recipeTitle)}`;
					} else {
						// 요청 실패 시 경고 메시지 표시
						alert("조회수 증가에 실패했습니다.");
					}
				})
				.catch(error => {
					console.error("서버 통신 중 오류 발생:", error);
					alert("서버와의 통신 중 문제가 발생했습니다.");
				});
		});
	});

	// 만약 다른 곳에서 여전히 `redirectToInfo` 함수를 직접 호출한다면, 
	// 아래와 같이 함수를 정의하여 일관성을 유지할 수 있습니다.
	// 하지만 가급적 위와 같이 이벤트 리스너 방식으로 변경하는 것을 권장합니다.
	/*
	function redirectToInfo(recipeId, recipeTitle) {
		fetch(`${contextPath}/list/incrementReadCount?recipeId=${recipeId}`)
			.then(response => {
				if (response.ok) {
					window.location.href = `${contextPath}/list/details?recipeId=${recipeId}&title=${encodeURIComponent(recipeTitle)}`;
				} else {
					alert("조회수 증가에 실패했습니다.");
				}
			})
			.catch(error => {
				console.error("서버 통신 중 오류 발생:", error);
				alert("서버와의 통신 중 문제가 발생했습니다.");
			});
	}
	*/
});