package com.office.cook.list;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeVo {
	private int recipeId;
	private String title;
	private String kind;
	private String tip;
	private String completeImgUrl;
	private Timestamp createdAt;
	private String userid; // <-- 이 줄을 추가하세요! 레시피 작성자의 ID

	private List<RecipeStepVo> steps;
	private List<RecipeIngredientVo> ingredients;

	private int likeCount;
	private int dislikeCount;
}