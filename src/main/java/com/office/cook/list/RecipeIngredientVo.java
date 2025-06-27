package com.office.cook.list;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientVo {
    private int ingredientId;
    private int recipeId;
    private String name;
    private String amount;
}
