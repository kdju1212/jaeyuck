package com.office.cook.list;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepVo {
	private int stepId;
	private int recipeId;
	private int stepOrder;
	private String description;
	private String stepImgUrl;
}
