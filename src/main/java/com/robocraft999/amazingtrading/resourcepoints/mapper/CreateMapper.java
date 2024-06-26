package com.robocraft999.amazingtrading.resourcepoints.mapper;

import com.robocraft999.amazingtrading.AmazingTrading;
import com.robocraft999.amazingtrading.api.mapper.RecipeTypeMapper;
import com.robocraft999.amazingtrading.resourcepoints.IngredientMap;
import com.robocraft999.amazingtrading.resourcepoints.mapper.collector.IMappingCollector;
import com.robocraft999.amazingtrading.resourcepoints.mapper.recipe.BaseRecipeTypeMapper;
import com.robocraft999.amazingtrading.resourcepoints.nss.NormalizedSimpleStack;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.fan.processing.HauntingRecipe;
import com.simibubi.create.content.kinetics.fan.processing.SplashingRecipe;
import com.simibubi.create.content.kinetics.millstone.MillingRecipe;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreateMapper {

    protected abstract static class CreateProcessingRecipeMapper<R extends ProcessingRecipe<?>> extends BaseRecipeTypeMapper<R> {

        @Override
        public String getDescription() {
            return "Create ProcessingRecipeMapper";
        }

        @Override
        public boolean handleRecipeTyped(IMappingCollector<NormalizedSimpleStack, Long> mapper, R recipe, RegistryAccess registryAccess, INSSFakeGroupManager fakeGroupManager) {
            //Outputs
            List<Object> outputs = new ArrayList<>();
            List<ItemStack> results = recipe.getRollableResults().stream().filter(pO -> pO.getChance() >= 1.0f).map(ProcessingOutput::getStack).toList();
            outputs.addAll(results);
            outputs.addAll(recipe.getFluidResults());

            if (outputs.isEmpty()) {
                AmazingTrading.LOGGER.debug("Recipe ({}) contains no outputs: {}", recipe.getId(), outputs);
                return false;
            }
            NSSOutput bundledOutput = mapOutputs(mapper, recipe, fakeGroupManager, outputs.toArray());

            //Inputs
            NSSInput bundledInput = getInput(recipe, fakeGroupManager);
            if (bundledInput == null || !bundledInput.successful()){
                return addConversionsAndReturn(mapper, bundledInput != null ? bundledInput.fakeGroupMap() : null, true);
            }

            mapper.addConversion(bundledOutput.amount(), bundledOutput.nss(), bundledInput.ingredientMap().getMap());
            return addConversionsAndReturn(mapper, bundledInput.fakeGroupMap(), true);
        }

        @Override
        protected NSSInput getInput(R recipe, INSSFakeGroupManager fakeGroupManager){
            NonNullList<Ingredient> ingredients = recipe.getIngredients();

            List<FluidIngredient> rawFluidIngredients = recipe.getFluidIngredients().stream().filter(s -> !s.test(new FluidStack(Fluids.WATER, 1))).toList();
            NonNullList<FluidIngredient> fluidIngredients = NonNullList.of(FluidIngredient.EMPTY, rawFluidIngredients.toArray(new FluidIngredient[]{}));

            if (ingredients.isEmpty() && fluidIngredients.isEmpty()) {
                AmazingTrading.LOGGER.debug("Recipe ({}) contains no inputs: (Ingredients: {})", recipe.getId(), ingredients);
                return null;
            }

            // A 'Map' of NormalizedSimpleStack and List<IngredientMap>
            List<Tuple<NormalizedSimpleStack, List<IngredientMap<NormalizedSimpleStack>>>> fakeGroupMap = new ArrayList<>();
            IngredientMap<NormalizedSimpleStack> ingredientMap = new IngredientMap<>();

            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                if (recipe instanceof ItemApplicationRecipe iaRecipe && iaRecipe.shouldKeepHeldItem() && i == 1)
                    continue; // Skip ItemApplicationRecipe's held item if it is not consumed.
                if (!convertIngredient(-1, ingredient, ingredientMap, fakeGroupMap, fakeGroupManager, recipe.getId())) {
                    return new NSSInput(ingredientMap, fakeGroupMap, false);
                }
            }

            bundleInputWithFluids(ingredientMap, fakeGroupMap, fakeGroupManager, fluidIngredients);

            return new NSSInput(ingredientMap, fakeGroupMap, true);
        }

        @Override
        protected Collection<Ingredient> getIngredients(R recipe) {
            NonNullList<Ingredient> ingredients = (NonNullList<Ingredient>) super.getIngredients(recipe);
            AmazingTrading.LOGGER.debug("Recipe ({}) contains following inputs: (Ingredients: {})", recipe.getId(), ingredients.stream().map(Ingredient::getItems).toList());
            if (ingredients.isEmpty()) {
                AmazingTrading.LOGGER.debug("Recipe ({}) contains no inputs: (Ingredients: {})", recipe.getId(), ingredients);
                return null;
            }
            return ingredients;
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateBasinMapper extends CreateProcessingRecipeMapper<BasinRecipe> {

        @Override
        public String getName() {
            return "BasinMapper";//1229
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.BASIN.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create", priority = -1)
    public static class CreateCompactingMapper extends CreateProcessingRecipeMapper<CompactingRecipe> {
        @Override
        public String getName() {
            return "CompactingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.COMPACTING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateCrushingMapper extends CreateProcessingRecipeMapper<CrushingRecipe>{

        @Override
        public String getName() {
            return "CrushingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.CRUSHING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateCuttingMapper extends CreateProcessingRecipeMapper<CuttingRecipe>{

        @Override
        public String getName() {
            return "CuttingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.CUTTING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateFillingMapper extends CreateProcessingRecipeMapper<FillingRecipe>{
        @Override
        public String getName() {
            return "FillingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.FILLING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateHauntingMapper extends CreateProcessingRecipeMapper<HauntingRecipe>{

        @Override
        public String getName() {
            return "HauntingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.HAUNTING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateMillingMapper extends CreateProcessingRecipeMapper<MillingRecipe>{

        @Override
        public String getName() {
            return "MillingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.MILLING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create", priority = 10)
    public static class CreateMixingMapper extends CreateProcessingRecipeMapper<MixingRecipe>{

        @Override
        public String getName() {
            return "MixingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.MIXING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreatePressingMapper extends CreateProcessingRecipeMapper<PressingRecipe>{

        @Override
        public String getName() {
            return "PressingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.PRESSING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateSplashingMapper extends CreateProcessingRecipeMapper<SplashingRecipe>{

        @Override
        public String getName() {
            return "SplashingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.SPLASHING.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateDeployerApplicationMapper extends CreateProcessingRecipeMapper<DeployerApplicationRecipe>{

        @Override
        public String getName() {
            return "DeployerApplicationMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.DEPLOYING.getType();
        }
    }

    /*@RecipeTypeMapper(requiredMods = "create")
    public static class CreateMechanicalCrafterMapper extends BaseRecipeTypeMapper<MechanicalCraftingRecipe>{

        @Override
        public String getName() {
            return "MechanicalCraftingMapper";
        }

        @Override
        public String getDescription() {
            return "Mechanical Crafting Recipe Mapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.MECHANICAL_CRAFTING.getType();
        }
    }*/

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateItemApplicationMapper extends CreateProcessingRecipeMapper<ItemApplicationRecipe>{

        @Override
        public String getName() {
            return "ItemApplicationMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.ITEM_APPLICATION.getType();
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateSequencedAssemblyMapper extends BaseRecipeTypeMapper<SequencedAssemblyRecipe>{

        @Override
        public String getName() {
            return "ItemApplicationMapper";
        }

        @Override
        public String getDescription() {
            return "Sequenced Assembly Recipe Mapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.SEQUENCED_ASSEMBLY.getType();
        }

        @Override
        protected Collection<Ingredient> getIngredients(SequencedAssemblyRecipe recipe) {
            var sequence_ingredients = recipe.getSequence().stream().map(s->s.getRecipe().getIngredients().size() > 1 ? s.getRecipe().getIngredients().get(1) : Ingredient.EMPTY).toList();
            var ingredients = new ArrayList<>(sequence_ingredients);
            ingredients.add(recipe.getIngredient());
            ingredients.removeIf(Ingredient::isEmpty);
            return ingredients;
        }

        @Override
        protected NSSInput getInput(SequencedAssemblyRecipe recipe, INSSFakeGroupManager fakeGroupManager) {
            var ingredients = getIngredients(recipe);
            List<FluidIngredient> fluidIngredients = recipe.getSequence().stream().flatMap(s -> s.getRecipe().getFluidIngredients().stream()).filter(s -> !s.test(new FluidStack(Fluids.WATER, 1))).toList();

            if (ingredients.isEmpty() && fluidIngredients.isEmpty()) {
                AmazingTrading.LOGGER.debug("Recipe ({}) contains no inputs: (Ingredients: {})", recipe.getId(), ingredients);
                return null;
            }

            // A 'Map' of NormalizedSimpleStack and List<IngredientMap>
            List<Tuple<NormalizedSimpleStack, List<IngredientMap<NormalizedSimpleStack>>>> fakeGroupMap = new ArrayList<>();
            IngredientMap<NormalizedSimpleStack> ingredientMap = new IngredientMap<>();

            for (Ingredient ingredient : ingredients) {
                if (!convertIngredient(-1, ingredient, ingredientMap, fakeGroupMap, fakeGroupManager, recipe.getId())) {
                    return new NSSInput(ingredientMap, fakeGroupMap, false);
                }
            }

            bundleInputWithFluids(ingredientMap, fakeGroupMap, fakeGroupManager, fluidIngredients);

            return new NSSInput(ingredientMap, fakeGroupMap, true);
        }
    }

    @RecipeTypeMapper(requiredMods = "create")
    public static class CreateSandPaperPolishingMapper extends CreateProcessingRecipeMapper<SandPaperPolishingRecipe>{

        @Override
        public String getName() {
            return "SandPaperPolishingMapper";
        }

        @Override
        public boolean canHandle(RecipeType<?> recipeType) {
            return recipeType == AllRecipeTypes.SANDPAPER_POLISHING.getType();
        }
    }
}
