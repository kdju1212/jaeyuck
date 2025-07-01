document.addEventListener('DOMContentLoaded', function() {
    const cookItems = document.querySelectorAll('.cook-item.clickable');

    cookItems.forEach(item => {
        item.addEventListener('click', function() {
            // data-* 속성에서 값 읽어오기 (th:data-recipe-id로 변경했으니 dataset.recipeId로 접근)
            const recipeId = this.dataset.recipeId;

            // 만약 details 페이지로 title도 함께 넘겨야 한다면 data-recipe-title을 사용
            // const recipeTitle = this.dataset.recipeTitle; // HTML에 data-recipe-title이 있다면 사용 가능

            // 레시피 상세 페이지로 이동
            // contextPath는 Thymeleaf에서 주입된 변수라고 가정
            window.location.href = `${contextPath}list/details?recipeId=${recipeId}`; // title 파라미터가 필요 없으면 제거
        });
    });
});