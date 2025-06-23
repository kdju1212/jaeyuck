function registerForm(event) {
	let form = document.register_form;

	if (form.CKG_NM.value == '') {
		alert('요리 이름을 입력해주세요.');
		form.CKG_NM.focus();
		event.preventDefault(); // 폼 제출을 막습니다
		return false;
	} else if (form.CKG_INBUN_NM.value == '') {
		alert('요리 인분을 입력해주세요.');
		form.CKG_INBUN_NM.focus();
		event.preventDefault(); // 폼 제출을 막습니다
		return false;
	} else if (form.CKG_TIME_NM.value == '') {
		alert('소요 시간을 입력해주세요.');
		form.CKG_TIME_NM.focus();
		event.preventDefault(); // 폼 제출을 막습니다
		return false;
	} else if (form.CKG_MTRL_CN.value == '') {
		alert('재료를 입력해주세요.');
		form.CKG_MTRL_CN.focus();
		event.preventDefault(); // 폼 제출을 막습니다
		return false;
	} else if (form.step1.value == '') {
		alert('레시피를 순서에 맞게 입력해주세요.');
		form.step1.focus();
		event.preventDefault(); // 폼 제출을 막습니다
		return false;
	}

	return true; // 모든 입력이 유효한 경우 폼을 제출
}

function redirectToRecipeDetails(recipe_no, cookName) {
	window.location.href = '/cook/list/recipeDetails?recipe_no='
		+ encodeURIComponent(recipe_no)
		+ '&cookName='
		+ encodeURIComponent(cookName);
}

function deleteRecipe(recipe_no, cookName, event) {
	// 기본 이벤트(폼 제출 등) 방지
	event.preventDefault();

	// 삭제 확인 메시지
	let result = confirm('레시피를 삭제 하시겠습니까?');

	if (result) {
		// 서버에 AJAX 요청을 보내어 레시피 삭제 처리
		fetch(`/cook/list/deleteRecipe?recipe_no=${recipe_no}&cookName=${encodeURIComponent(cookName)}`, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}
		})
			.then(response => response.json()) // 서버의 JSON 응답을 받음
			.then(data => {
				if (data.success) {
					alert('레시피가 성공적으로 삭제되었습니다.');
					window.location.href = '/cook/list/recipeList';  // 레시피 목록 페이지로 리다이렉트
				} else {
					alert('삭제에 실패했습니다. 다시 시도해주세요.');
				}
			})
			.catch(error => {
				alert('삭제 중 오류가 발생했습니다. 다시 시도해주세요.');
				console.error('Error:', error); // 오류 로그 출력
			});
	}
}

// "사진 변경" 버튼 클릭 시 기존 이미지를 숨기고 새 이미지 입력 필드를 표시하는 함수
function hideImageRow() {
	const changeImageBtn = document.getElementById('changeImageBtn');
	if (changeImageBtn) {
		changeImageBtn.addEventListener('click', function() {
			const currentRow = this.closest('div'); // 버튼이 포함된 div 찾기
			if (currentRow) {
				currentRow.style.display = 'none'; // 현재 div 숨기기
			}

			// 숨겨진 파일 업로드 입력 필드를 포함한 div를 표시
			const changeImageRow = document.getElementById('changeImageRow');
			if (changeImageRow) {
				changeImageRow.style.display = 'block';
			}
		});
	}
}

// 페이지 로드 시 "사진 변경" 버튼 이벤트 연결
document.addEventListener('DOMContentLoaded', hideImageRow);