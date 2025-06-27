document.addEventListener("DOMContentLoaded", () => {
	let stepIndex = 1;

	// 재료 추가 버튼 클릭 시
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

	// 요리 단계 추가 버튼 클릭 시
	document.getElementById("btnAddStep").addEventListener("click", () => {
		stepIndex++;
		const stepContainer = document.getElementById("stepContainer");
		const step = document.createElement("div");
		step.className = "step-item";
		step.innerHTML = `
			<label>Step ${stepIndex}</label>
			<textarea name="step_text[]" placeholder="예) 양파를 썰어주세요."></textarea>
			<input type="file" name="step_photo_${stepIndex}" accept=".jpg,.jpeg,.png,.gif">
		`;
		stepContainer.appendChild(step);
	});
});

// 재료 행 삭제 함수
function removeRow(button) {
	const row = button.parentElement;
	row.remove();
}

// 저장 또는 저장+공개 버튼 처리
function doSubmit(type) {
	const form = document.querySelector("form");

	// 기존 hidden input 제거
	const oldInput = document.getElementById("submitType");
	if (oldInput) oldInput.remove();

	// 저장 타입 (임시 저장 or 저장 후 공개) 전달용 hidden input 추가
	const input = document.createElement("input");
	input.type = "hidden";
	input.name = "submitType";
	input.id = "submitType";
	input.value = type; // "save" 또는 "save_public"
	form.appendChild(input);

	// 폼 제출
	form.submit();
}
