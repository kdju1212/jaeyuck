document.addEventListener("DOMContentLoaded", () => {
	let stepIndex = 1;

	// 재료 추가 버튼 이벤트
	document.getElementById("btnAddMaterialRow").addEventListener("click", () => {
		const materialList = document.querySelector(".material-list");
		const row = document.createElement("div");
		row.className = "material-row";
		row.innerHTML = `
            <input type="text" name="material_name[]" placeholder="재료명">
            <input type="text" name="material_amount[]" placeholder="수량 (예: 100g)">
            <button type="button" onclick="removeRow(this)">-</button>
        `;
		materialList.appendChild(row);
	});

	// 요리 순서 추가 버튼 이벤트
	document.getElementById("btnAddStep").addEventListener("click", () => {
		stepIndex++;
		const stepContainer = document.getElementById("stepContainer");
		const step = document.createElement("div");
		step.className = "step-item";
		step.innerHTML = `
            <label>Step ${stepIndex}</label>
            <textarea name="step_text[]" placeholder="예) 양파를 썰어주세요."></textarea>
			<input type="file" name="step_photo_${stepIndex}[]" multiple accept=".jpg,.jpeg,.png,.gif">
        `;
		stepContainer.appendChild(step);
	});
});

// 행 삭제 함수 (재료 리스트 등)
function removeRow(button) {
	const row = button.parentElement;
	row.remove();
}

// 저장 및 저장 후 공개 버튼 처리
function doSubmit(type) {
	if (type === 'save') {
		alert("저장 완료!");
	} else if (type === 'save_public') {
		alert("저장 후 공개 완료!");
	}
	// 실제로는 form.submit() 또는 AJAX 요청 처리 구현 필요
}
