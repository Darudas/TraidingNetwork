package com.robocraft999.amazingtrading.resourcepoints.mapper.recipe;

import com.robocraft999.amazingtrading.api.mapper.RecipeTypeMapper;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;

@RecipeTypeMapper(priority = 100)
public class VanillaRecipeTypeMapper extends BaseRecipeTypeMapper<CraftingRecipe> {
    @Override
    public String getName() {
        return "CraftingRecipeMapper";
    }

    @Override
    public String getDescription() {
        return "default vanilla crafting recipes";
    }

    @Override
    public boolean canHandle(RecipeType<?> recipeType) {
        return recipeType == RecipeType.CRAFTING || recipeType == RecipeType.SMELTING || recipeType == RecipeType.BLASTING || recipeType == RecipeType.SMOKING
                || recipeType == RecipeType.CAMPFIRE_COOKING || recipeType == RecipeType.STONECUTTING;
    }
}
