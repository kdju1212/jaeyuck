document.addEventListener('DOMContentLoaded', function() {
	// DOM 요소들을 변수에 할당 (자주 사용될 요소들)
	const modifyForm = document.getElementById('modifyForm');
	const addIngredientBtn = document.getElementById('add-ingredient-btn');
	const ingredientsContainer = document.getElementById('ingredients-container');
	const addStepBtn = document.getElementById('add-step-btn');
	const stepsContainer = document.getElementById('steps-container');

	const deletedIngredientIdsInput = document.getElementById('deletedIngredientIds');
	const deletedStepIdsInput = document.getElementById('deletedStepIds');

	// 완성 사진 관련 요소들
	const completePhotoUploadInput = document.getElementById('complete_photo_upload');
	const completeImageDisplaySection = document.querySelector('.complete-image-display-section');
	const initialUploadSection = document.querySelector('.initial-upload-section');
	const deleteImageFlagInput = document.getElementById('deleteImageFlag');

	// --- 재료 추가/삭제 로직 ---

	// '재료 추가' 버튼 클릭 이벤트
	addIngredientBtn.addEventListener('click', function() {
		const newRow = document.createElement('div');
		newRow.classList.add('material-row');
		newRow.innerHTML = `
            <input type="text" name="newIngredientNames" placeholder="재료명" required>    <input type="text" name="newIngredientAmounts" placeholder="수량 (예: 100g)" required> <button type="button" class="remove-button" onclick="removeRow(this)">-</button>
        `;
		ingredientsContainer.appendChild(newRow);
	});

	// 재료 행 삭제 함수 (기존 재료 및 새로 추가된 재료 모두 처리)
	window.removeRow = function(button) {
		const row = button.closest('.material-row');
		const ingredientIdInput = row.querySelector('input[name="existingIngredientIds"]');

		if (ingredientIdInput && ingredientIdInput.value) {
			const currentDeletedIds = deletedIngredientIdsInput.value;
			deletedIngredientIdsInput.value = currentDeletedIds ? `${currentDeletedIds},${ingredientIdInput.value}` : ingredientIdInput.value;
		}
		row.remove();
	};

	// --- 요리 순서 추가/삭제 로직 ---

	// '요리 순서 추가' 버튼 클릭 이벤트
	addStepBtn.addEventListener('click', function() {
		const currentStepTotalCount = stepsContainer.children.length; // 현재 DOM에 있는 스텝 개수
		const newStepItem = document.createElement('div');
		newStepItem.classList.add('step-item');
		newStepItem.innerHTML = `
            <label>Step ${currentStepTotalCount + 1}</label>
            <textarea name="newStepDescriptions" placeholder="예) 양파를 썰어주세요." required></textarea>
            
            <div class="step-image-container">
                <div class="new-step-image-upload-section" style="display: block;">
                    <label>단계 이미지 업로드 (선택)</label>
                    <input type="file" name="step_photo" accept=".jpg,.jpeg,.png,.gif">
                </div>
            </div>
            <button type="button" class="remove-button btn-remove-step">-</button>
        `;
		stepsContainer.appendChild(newStepItem);
		updateStepLabels(); // 단계 번호 업데이트
	});

	// 요리 순서 행 삭제 함수 (기존 단계 및 새로 추가된 단계 모두 처리)
	stepsContainer.addEventListener('click', function(event) {
		if (event.target.classList.contains('btn-remove-step')) {
			const stepItem = event.target.closest('.step-item');
			const stepIdInput = stepItem.querySelector('input[name="existingStepIds"]'); // 기존 단계 ID 확인

			if (stepIdInput && stepIdInput.value) { // 기존 단계인 경우에만 deletedStepIds에 추가
				const currentDeletedIds = deletedStepIdsInput.value;
				deletedStepIdsInput.value = currentDeletedIds ? `${currentDeletedIds},${stepIdInput.value}` : stepIdInput.value;
			}
			stepItem.remove();
			updateStepLabels(); // 단계 번호 업데이트
		}
	});

	// 요리 순서 레이블 (Step 1, Step 2 등) 업데이트 함수
	function updateStepLabels() {
		const stepItems = stepsContainer.querySelectorAll('.step-item');

		stepItems.forEach((item, index) => {
			item.querySelector('label').textContent = `Step ${index + 1}`;
		});
	}

	// --- 완성 사진 이미지 처리 로직 ---

	// '이미지 삭제' 버튼 클릭 이벤트 (완성 사진)
	const deleteCompleteImageBtn = document.querySelector('.btn-delete-complete-image');
	if (deleteCompleteImageBtn) {
		deleteCompleteImageBtn.addEventListener('click', function() {
			if (confirm('현재 요리 이미지를 삭제하시겠습니까?')) {
				deleteImageFlagInput.value = 'Y';
				if (completeImageDisplaySection) completeImageDisplaySection.style.display = 'none';
				if (initialUploadSection) initialUploadSection.style.display = 'block';

				if (completePhotoUploadInput) {
					completePhotoUploadInput.required = false; // 이미지 삭제 후 새로운 이미지 첨부가 필수는 아니도록
					completePhotoUploadInput.value = ''; // 파일 input 초기화
				}

				alert('이미지가 삭제되었습니다. 저장 시 반영됩니다.');
			}
		});
	}

	// --- 단계별 이미지 처리 로직 ---

	// '단계 이미지 삭제' 버튼 클릭 이벤트 (이벤트 위임)
	stepsContainer.addEventListener('click', function(event) {
		if (event.target.classList.contains('btn-delete-step-image')) {
			const button = event.target;
			const stepItem = button.closest('.step-item');
			const currentImageDisplaySection = stepItem.querySelector('.current-step-image-display');
			const newImageUploadSection = stepItem.querySelector('.new-step-image-upload-section');
			const deleteFlagInput = stepItem.querySelector('input[name="existingStepDeleteImageFlags"]');
			const currentImgUrlInput = stepItem.querySelector('input[name="existingStepCurrentImgUrls"]');

			const newFileInput = newImageUploadSection ? newImageUploadSection.querySelector('input[type="file"]') : null;


			if (confirm('현재 단계 이미지를 삭제하시겠습니까?')) {
				if (deleteFlagInput) {
					deleteFlagInput.value = 'Y';
				}
				if (currentImgUrlInput) {
					currentImgUrlInput.value = ''; // 현재 이미지 URL도 지웁니다.
				}

				// UI 업데이트: 기존 이미지 섹션 숨기고, 새 이미지 업로드 섹션 표시
				if (currentImageDisplaySection) currentImageDisplaySection.style.display = 'none';
				if (newImageUploadSection) newImageUploadSection.style.display = 'block';

				if (newFileInput) {
					newFileInput.value = ''; // 파일 input 초기화
				}

				alert('단계 이미지가 삭제되었습니다. 저장 시 반영됩니다.');
			}
		}
	});

	// 초기 로드 시 각 요리 순서 단계의 이미지 섹션 상태 설정
	stepsContainer.querySelectorAll('.step-item').forEach(item => {
		const currentImageDisplaySection = item.querySelector('.current-step-image-display');
		const newImageUploadSection = item.querySelector('.new-step-image-upload-section');
		const currentImageUrlInput = item.querySelector('input[name="existingStepCurrentImgUrls"]');

		if (currentImageUrlInput && currentImageUrlInput.value) { // 기존 이미지가 있는 경우
			if (currentImageDisplaySection) currentImageDisplaySection.style.display = 'block';
			if (newImageUploadSection) newImageUploadSection.style.display = 'none'; // 새 업로드 섹션은 숨김
		} else { // 기존 이미지가 없는 경우 (새로 추가된 스텝이거나, 이미지가 원래 없었던 경우)
			if (currentImageDisplaySection) currentImageDisplaySection.style.display = 'none'; // 기존 이미지 섹션은 숨김
			if (newImageUploadSection) newImageUploadSection.style.display = 'block'; // 새 업로드 섹션은 보임
		}
	});

	// --- 폼 제출 로직 (doSubmit 함수) ---
	window.doSubmit = function(action) {
		if (!modifyForm.checkValidity()) {
			modifyForm.reportValidity();
			return false;
		}

		// 최소 재료/단계 확인 (필수라면)
		if (ingredientsContainer.children.length === 0) {
			alert('재료를 최소 하나 이상 입력해주세요.');
			return false;
		}
		if (stepsContainer.children.length === 0) {
			alert('요리 순서를 최소 하나 이상 입력해주세요.');
			return false;
		}

		modifyForm.submit();
	};

	// 초기 로드 시 단계 레이블 업데이트
	updateStepLabels();
});