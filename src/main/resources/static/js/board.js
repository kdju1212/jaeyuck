// 댓글 등록 폼 제출 전 검증
function validateCommentForm() {
	var content = document.getElementById("content").value.trim(); // 댓글 내용 가져오기
	if (content === "") {
		alert("댓글을 입력해주세요.");
		return false; // 폼 제출을 막음
	}
	return true; // 폼 제출 진행
}

// 댓글 작성 textarea 클릭 시 로그인 여부 확인
function checkLoginBeforeComment() {
	var userId = document.getElementById("userId").value; // 로그인한 사용자의 ID 가져오기
	if (!userId) { // 로그인되지 않았다면
		var isLogin = confirm("로그인이 필요합니다. 로그인하시겠습니까?");
		if (isLogin) {
			// 로그인 페이지로 리디렉션
			window.location.href = contextPath + "/member/loginForm"; // contextPath 변수를 사용
			return false;
		} else {
			content.blur(); // 아무 동작도 하지 않고 그대로 페이지에 남아있음
			return false;
		}
	}
}

// 북마크 추가 로직
function bookmark(event) {
	event.preventDefault(); // 이벤트의 기본 동작 방지 (버튼 클릭 시 다른 동작 방지)

	var userId = document.getElementById("userId").value; // 로그인한 사용자의 ID 가져오기
	if (!userId || userId === "") {
		var isLogin = confirm("로그인이 필요합니다. 로그인하시겠습니까?");
		if (isLogin) {
			window.location.href = contextPath + "/member/loginForm";
			return;
		} else {
			return false;
		}
	}

	// 북마크 추가 로직
	var CKG_NM = document.getElementById("CKG_NM").value;
	var pageURL = window.location.href;
	var cook_no = document.getElementById("cook_no").value;

	// 숨겨진 필드 설정
	document.getElementById("pageURL").value = pageURL;
	document.getElementById("userId").value = userId;
	document.getElementById("CKG_NM").value = CKG_NM;
	document.getElementById("cook_no").value = cook_no;

	// 명확히 폼 선택 후 제출
	document.getElementById("bookmarkForm").submit();
}

function editComment(content,board_no) {
	var contentText = document.getElementById("content-text-" + content + "-" + board_no);
	var contentTextarea = document.getElementById("content-edit-" + content+ "-"+board_no);
	var updateForm = document.getElementById("updateForm-" + content+ "-"+board_no);
	var updateButton = document.getElementById("updateButton-" + content+ "-"+board_no);

	contentText.style.display = "none";
	contentTextarea.style.display = "block";

	updateButton.value = "제출";
	updateButton.onclick = function() {
		// 수정된 내용
		var newContent = contentTextarea.value;

		// 이미 존재하는 'new_content' hidden input이 있으면 값을 변경, 없으면 새로 생성
		var hiddenContentField = updateForm.querySelector('input[name="new_content"]');
		if (!hiddenContentField) {
			hiddenContentField = document.createElement("input");
			hiddenContentField.type = "hidden";
			hiddenContentField.name = "new_content";
			updateForm.appendChild(hiddenContentField);
		}
		hiddenContentField.value = newContent;  // textarea의 값을 hidden input에 반영

		// 폼 제출
		updateForm.submit();
	};

	return false;  // 기본 폼 제출을 방지하고 자바스크립트로 처리
}

function shareTwitter() {
    var sendText = "나의 레시피"; // 전달할 텍스트
	var pageURL = window.location.href;
    window.open("https://twitter.com/intent/tweet?text=" + sendText + "&url=" + pageURL);
}

function shareFacebook() {
	var pageURL = window.location.href;
	console.log(pageURL);
    window.open("http://www.facebook.com/sharer/sharer.php?u=" + pageURL);
}

function clipBoard() {
	let dummy = document.createElement("input");
	const url = location.href;

	document.body.appendChild(dummy);
	dummy.value = url;
	dummy.select();
	document.execCommand("copy");
	alert("URL을 클립보드에 복사했습니다.");
	document.body.removeChild(dummy);
}


function preventEnter(event) {
	var form = document.getElementById('commentForm');
	if (event.keyCode == 13) {
		event.preventDefault();
		form.submit();
	}
} 