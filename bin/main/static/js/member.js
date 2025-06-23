/**
 * member.js 파일 
 */
function loginCheck() {
	if (document.frm.userid.value.length == 0) {
		alert("아이디를 써주세요");
		frm.userid.focus();
		return false;
	}
	if (document.frm.pwd.value == "") {
		alert("암호는 반드시 입력해야 합니다.");
		frm.pwd.focus();
		return false;
	}
	return true;
}


//기존 member 테이블에 중복된 아이디가 있는지 체크함.
function idCheck() {
	if (document.frm.userid.value == "") {
		alert('아이디를 입력하여 주십시오.');
		document.frm.userid.focus();
		return;
	}
	var url = "idCheck?userid=" + document.frm.userid.value;  // Spring MVC의 요청 URL
	window.open(url, "_blank_1",
		"toolbar=no, menubar=no, scrollbars=yes, resizable=no, width=450, height=440");
}

function idok(userid) {
	// 부모 창의 아이디와 reid 필드에 값을 설정
	opener.document.frm.userid.value = userid;
	opener.document.frm.reid.value = userid;  // reid 값을 설정
	self.close();  // 팝업 창 닫기
}



function joinCheck() {
	if (document.frm.name.value.length == 0) {
		alert("이름을 입력해주세요.");
		frm.name.focus();
		return false;
	}
	if (document.frm.userid.value.length == 0) {
		alert("아이디를 입력해주세요.");
		frm.userid.focus();
		return false;
	}
	/*
	// 아이디 제한할때
	if (document.frm.userid.value.length < 4) {
		alert("아이디는 4글자이상이어야 합니다.");
		frm.userid.focus();
		return false;
	}
	*/
	if (document.frm.pwd.value == "") {
		alert("암호는 반드시 입력해야 합니다.");
		frm.pwd.focus();
		return false;
	}
	if (document.frm.pwd.value != document.frm.pwd_check.value) {
		alert("암호가 일치하지 않습니다.");
		frm.pwd.focus();
		return false;
	}
	if (document.frm.reid.value.length == 0) {
		alert("중복 체크를 하지 않았습니다.");
		frm.userid.focus();
		return false;
	}

	return true;
}
function redirectToLogin() {
	alert("로그인이 필요합니다! 로그인 페이지로 이동합니다."); // 팝업 창 띄우기
	window.location.href = '/Cook/LoginServlet'; // 로그인 페이지로 이동
}

// 일반적인 리다이렉트 함수
function redirectTo(url) {
	window.location.href = url; // 페이지를 지정한 URL로 리다이렉트
}
function redirectToDelete() {
	// Confirm 창 띄우기
	var result = confirm("정말 탈퇴 하시겠습니까?");

	// 사용자가 '확인'을 클릭한 경우에만 페이지 이동
	if (result) {
		// 비밀번호 입력값 가져오기
		var password = document.getElementById("pwd").value;

		// 비밀번호가 입력되었는지 확인
		if (password === "") {
			alert("비밀번호를 입력해주세요.");
			return; // 비밀번호가 없으면 함수 종료
		}

		// 비밀번호가 입력되었으면 폼 제출
		document.frm.submit(); // 폼 제출
	}
}

function logoutCheck() {
	var check = confirm("로그아웃 하시겠습니까?")
	if (!check) {
		return false;
	}
}

function findPasswordForm() {

	let form = document.findpwdform;

	if (form.userid.value == '') {
		alert('아이디를 입력해 주세요');
		form.userid.focus();

	} else if (form.name.value == '') {
		alert('이름을 입력해 주세요.');
		form.name.focus();

	} else if (form.email.value == '') {
		alert('mail을 입력해 주세요.');
		form.email.focus();

	} else {
		form.submit();

	}

}

document.addEventListener("DOMContentLoaded", function() {
	const dropdowns = document.querySelectorAll(".dropdown");

	dropdowns.forEach(function(dropdown) {
		dropdown.addEventListener("click", function(e) {
			e.stopPropagation(); // 클릭 이벤트 전파 방지
			const menu = this.querySelector(".dropdown-menu");

			// 다른 드롭다운 메뉴는 모두 닫고 현재 메뉴만 토글
			document.querySelectorAll(".dropdown-menu").forEach(function(otherMenu) {
				if (otherMenu !== menu) {
					otherMenu.classList.remove("show");
				}
			});

			menu.classList.toggle("show");
		});
	});

	document.addEventListener("click", function() {
		// 페이지의 다른 곳을 클릭하면 모든 드롭다운 메뉴 닫기
		document.querySelectorAll(".dropdown-menu").forEach(function(menu) {
			menu.classList.remove("show");
		});
	});
});
