// board.js

// Global variables passed from recipe_details.html
// These are initialized in the <script layout:fragment="scripts"> block of recipe_details.html
// For example:
// var contextPath = /*[[@{/}]]*/'';
// var loginedUserId = $('#loggedInUserId').val();

/**
 * Checks if the user is logged in.
 * @returns {boolean} True if logged in, false otherwise.
 */
function checkLogin() {
	if (!loginedUserId || String(loginedUserId).trim() === "" || String(loginedUserId).trim() === "null") {
		alert("로그인이 필요합니다."); // Login required
		window.location.href = contextPath + "/member/loginForm";
		return false;
	}
	return true;
}

/**
 * Handles click events for like/dislike buttons.
 * Distinguishes between recipe reactions and comment reactions to collect appropriate data.
 * @param {Event} event - The click event object.
 */
function handleReactionClick(event) {
	const $clickedButton = $(event.currentTarget); // jQuery object for the clicked button

	// Check login status
	if (!checkLogin()) {
		return; // If not logged in, stop function execution
	}

	let dataToSend = {}; // Object to hold data for the server request

	// Determine which type of reaction button was clicked
	// 1. Recipe like/dislike button (an <input type="submit"> within a form)
	if ($clickedButton.closest('.like-dislike-section').length > 0 &&
		$clickedButton.is('input[type="submit"].reaction-button')) {

		const $form = $clickedButton.closest('form'); // Find the parent form
		dataToSend = {
			// For recipe reactions, only send recipe_id. Do NOT send board_no.
			recipe_id: $form.find('input[name="recipe_id"]').val(),
			userId: $form.find('input[name="userId"]').val(),
			reactionType: $form.find('input[name="reactionType"]').val(),
			target_type: 'recipe' // Explicitly set target type to 'recipe'
		};

		console.log("DEBUG: Recipe like/dislike data collected:", dataToSend);

	}
	// 2. Comment like/dislike button (a <button> with data-target-type="comment")
	else if ($clickedButton.data('target-type') === 'comment') {
		dataToSend = {
			// For comment reactions, only send board_no. Do NOT send recipe_id.
			board_no: $clickedButton.data('board-no'),
			userId: $clickedButton.data('user-id'),
			reactionType: $clickedButton.data('reaction-type'),
			target_type: $clickedButton.data('target-type') // 'comment'
		};

		console.log("DEBUG: Comment like/dislike data collected:", dataToSend);
	} else {
		// Unexpected .reaction-button click (e.g., HTML structure error)
		console.error("DEBUG: Unknown .reaction-button click or unclear data origin:", $clickedButton);
		alert("좋아요/싫어요 처리 중 알 수 없는 오류가 발생했습니다. 개발자 도구를 확인해주세요."); // Unknown error during like/dislike processing. Check developer tools.
		return; // Stop if the button type is unrecognized
	}

	// Validate essential data before sending to the server
	if (!dataToSend.userId || !dataToSend.reactionType || !dataToSend.target_type) {
		console.error("DEBUG: Essential data (userId, reactionType, target_type) missing:", dataToSend);
		alert("좋아요/싫어요 처리에 필요한 필수 정보가 부족합니다. 로그인 상태를 확인해주세요."); // Essential information missing. Please check login status.
		return;
	}
	// Ensure either board_no or recipe_id is present based on target_type
	if (dataToSend.target_type === 'recipe' && !dataToSend.recipe_id) {
		console.error("DEBUG: Essential data (recipe_id) missing for recipe reaction:", dataToSend);
		alert("레시피 정보가 부족하여 좋아요/싫어요 요청을 처리할 수 없습니다."); // Recipe information missing. Cannot process like/dislike request.
		return;
	}
	if (dataToSend.target_type === 'comment' && !dataToSend.board_no) {
		console.error("DEBUG: Essential data (board_no) missing for comment reaction:", dataToSend);
		alert("댓글 정보가 부족하여 좋아요/싫어요 요청을 처리할 수 없습니다."); // Comment information missing. Cannot process like/dislike request.
		return;
	}


	// AJAX request part
	$.ajax({
		url: contextPath + "/board/processReaction", // Controller URL
		type: "POST",
		data: dataToSend, // Use the collected dataToSend object
		success: function(response) {
			if (response.success) {
				// UI update logic: update counts based on target_type
				if (dataToSend.target_type === 'recipe') {
					$("#recipe-like-count-" + dataToSend.recipe_id).text(response.newLikeCount);
					$("#recipe-dislike-count-" + dataToSend.recipe_id).text(response.newDislikeCount);
				} else if (dataToSend.target_type === 'comment') {
					$("#comment-like-count-" + dataToSend.board_no).text(response.newLikeCount);
					$("#comment-dislike-count-" + dataToSend.board_no).text(response.newDislikeCount);
				}
				// Optional: Add logic for changing button state or icon here
			} else {
				alert("처리 실패: " + response.message); // Processing failed
			}
		},
		error: function(xhr, status, error) {
			console.error("좋아요/싫어요 처리 중 오류 발생:", status, error, xhr.responseText); // Error during like/dislike processing
			try {
				let errorResponse = JSON.parse(xhr.responseText);
				alert("서버 통신 중 알 수 없는 문제가 발생했습니다: " + errorResponse.message); // Unknown server communication error
			} catch (e) {
				alert("서버 통신 중 알 수 없는 문제가 발생했습니다."); // Unknown server communication error
			}
		}
	});
}


// --- Code executed when DOM is ready ---
$(document).ready(function() {
	// Log loginedUserId when DOM is ready (for debugging)
	console.log("board.js - loginedUserId (on DOM ready):", loginedUserId);

	// Comment edit button click event
	$('.edit-comment-btn').on('click', function() {
		var $commentItem = $(this).closest('.comment-item');
		var $commentContent = $commentItem.find('.comment-content');
		var $textarea = $commentItem.find('.comment-edit-textarea');
		var $saveBtn = $commentItem.find('.save-comment-btn');
		var $editBtn = $(this);

		$commentContent.hide();
		$textarea.show();
		$saveBtn.show();
		$editBtn.hide();
	});

	// Comment save (finish editing) button click event (AJAX)
	$('.save-comment-btn').on('click', function(e) {
		e.preventDefault(); // Prevent default form submission

		var $form = $(this).closest('form');
		var boardNo = $form.find('input[name="board_no"]').val();
		var newContent = $form.find('textarea[name="new_content"]').val();
		var recipeId = $form.find('input[name="recipeId"]').val();
		var ckgNm = $form.find('input[name="ckg_nm"]').val();
		var userid = $form.find('input[name="userid"]').val();

		if (newContent.trim() === "") {
			alert("댓글 내용을 입력해주세요."); // Please enter comment content
			return;
		}

		$.ajax({
			url: contextPath + "/board/updateComment",
			type: "POST",
			data: {
				board_no: boardNo,
				new_content: newContent,
				recipeId: recipeId,
				ckg_nm: ckgNm,
				userid: userid
			},
			success: function(response) {
				if (response.success) { // Assuming the server response has a 'success' field
					var $commentItem = $form.closest('.comment-item');
					$commentItem.find('.comment-content').text(newContent).show(); // Update content
					$form.find('.comment-edit-textarea').hide();
					$form.find('.save-comment-btn').hide();
					$form.find('.edit-comment-btn').show();
					alert("댓글이 수정되었습니다."); // Comment updated
				} else {
					alert("댓글 수정 실패: " + (response.message || "알 수 없는 오류")); // Comment update failed
				}
			},
			error: function(xhr, status, error) {
				console.error("댓글 수정 오류:", status, error, xhr.responseText); // Comment update error
				alert("댓글 수정에 실패했습니다. 다시 시도해주세요."); // Failed to update comment. Please try again.
			}
		});
	});

	// Attach click event to all .reaction-button classes
	$(".reaction-button").on("click", handleReactionClick);

	// Comment submission button login check
	$('.comment-submit-btn').on('click', function() {
		return checkLogin();
	});


	// 북마크 버튼 클릭 이벤트(AJAX 처리)
	$('.bookmark-btn').on('click', function(e) {
		// 로그인 체크 (기존 로직 유지)
		if (!checkLogin()) {
			e.preventDefault(); // 로그인 안 되어 있으면 새로고침도 막음
			return;
		}

		e.preventDefault(); // 버튼의 기본 제출 동작(페이지 새로고침) 방지

		const $clickedButton = $(this);
		const $form = $clickedButton.closest('form');
		const recipeId = $form.find('input[name="recipeId"]').val();
		const $bookmarkIcon = $clickedButton.find('#bookmarkIcon'); // span 태그를 정확히 선택해야 합니다.

		console.log("DEBUG_JS: 북마크 버튼 클릭됨. recipeId:", recipeId); // 디버그 로그 추가
		console.log("DEBUG_JS: $bookmarkIcon 현재 클래스:", $bookmarkIcon.attr('class')); // 디버그 로그 추가

		$.ajax({
			url: contextPath + "/list/toggleBookmark", // 북마크 토글 URL
			type: "POST",
			data: {
				recipeId: recipeId
			},
			success: function(response) {
				if (response.success) {
					// 서버 응답에 따라 북마크 아이콘 변경 (CSS 클래스 토글)
					// response.bookmarked: true/false (서버에서 현재 북마크 상태를 알려준다고 가정)
					if (response.bookmarked) {
						$bookmarkIcon.removeClass('unbookmarked-star').addClass('bookmarked-star');
						alert("북마크에 추가되었습니다.");
					} else {
						$bookmarkIcon.removeClass('bookmarked-star').addClass('unbookmarked-star');
						alert("북마크에서 제거되었습니다.");
					}
					console.log("DEBUG_JS: 북마크 처리 성공! 새로운 상태:", response.bookmarked ? "BOOKMARKED" : "UNBOOKMARKED"); // 디버그 로그 추가
					console.log("DEBUG_JS: $bookmarkIcon 변경 후 클래스:", $bookmarkIcon.attr('class')); // 디버그 로그 추가
				} else {
					alert("북마크 처리 실패: " + (response.message || "알 수 없는 오류"));
					console.error("DEBUG_JS: 북마크 처리 실패 메시지:", response.message);
				}
			},
			error: function(xhr, status, error) {
				console.error("북마크 처리 중 오류 발생:", status, error, xhr.responseText);
				alert("북마크 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
			}
		});
	});

});