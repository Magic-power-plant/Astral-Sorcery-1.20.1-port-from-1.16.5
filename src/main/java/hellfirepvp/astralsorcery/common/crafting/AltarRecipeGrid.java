/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.common.util.collection.MapStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.crafting.helper.WrappedIngredient;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.FluidIngredient;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarRecipeGrid
 * Created by HellFirePvP
 * Date: 12.08.2019 / 19:53
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe/altar; this tree has it at the top level
//of common/crafting. Not moved because the package layout is a tree-wide decision.
public class AltarRecipeGrid {

    //TODO re-add dynamic size shifting, respecting typed slot filters

    public static final int GRID_SIZE = 5;
    public static final int MAX_INVENTORY_SIZE = GRID_SIZE * GRID_SIZE;
    //1.16.5 builds EMPTY with width/height 0 (1.16.5 common/crafting/recipe/altar/AltarRecipeGrid.java:50).
    //Restored: with an empty gridParts map both variants behave identically in containsInputs (the
    //per-cell loop either does not run at all or tests Ingredient.EMPTY against every slot, and both then
    //fall through to isGridEmpty), but getWidth/getHeight now report the reference values.
    public static final AltarRecipeGrid EMPTY = new AltarRecipeGrid(new HashMap<>(), 0, 0);
    private static final Pattern SKIP_CHARS = Pattern.compile("^\\s|_|#$");

    private final Map<Integer, Ingredient> gridParts;
    private final int width, height;

    public AltarRecipeGrid(Map<Integer, Ingredient> gridParts) {
        this(gridParts, GRID_SIZE, GRID_SIZE);
    }

    private AltarRecipeGrid(Map<Integer, Ingredient> gridParts, int width, int height) {
        this.gridParts = gridParts;
        this.width = width;
        this.height = height;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean containsInputs(IItemHandler itemHandler, boolean testMirrored) {
        for (int xx = 0; xx <= (GRID_SIZE - this.width); xx++) {
            for (int zz = 0; zz <= (GRID_SIZE - this.height); zz++) {
                if (matches(itemHandler, xx, zz, false)) {
                    return true;
                }
                if (testMirrored && matches(itemHandler, xx, zz, true)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Ingredient getIngredient(int index) {
        return gridParts.getOrDefault(index, Ingredient.EMPTY);
    }

    public Set<Integer> getInputSlots() {
        return gridParts.keySet();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private boolean matches(IItemHandler itemHandler, int xOffset, int zOffset, boolean mirrored) {
        Set<Integer> matchedItems = new HashSet<>();
        int totalOffset = zOffset * GRID_SIZE + xOffset;
        for (int x = 0; x < this.width; x++) {
            for (int z = 0; z < this.height; z++) {
                int index = x + z * GRID_SIZE;
                if (mirrored) {
                    index = (this.width - x - 1) + z * GRID_SIZE;
                }
                Ingredient expected = getIngredient(index);
                int slot = index + totalOffset;
                ItemStack contained = itemHandler.getStackInSlot(slot);
                if (!expected.test(contained)) {
                    return false;
                }
                matchedItems.add(slot);
            }
        }
        return isGridEmpty(itemHandler, matchedItems);
    }

    private boolean isGridEmpty(IItemHandler inventory, Set<Integer> skipSlots) {
        for (int x = 0; x < GRID_SIZE; x++) {
            for (int z = 0; z < GRID_SIZE; z++) {
                int slot = x + z * GRID_SIZE;
                if (!skipSlots.contains(slot) && !inventory.getStackInSlot(slot).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void validate(AltarType type) {
        //AltarRecipeGrid centralized = this.centralizeGrid();
        if (gridParts.isEmpty()) {
            //1.16.5 threw IllegalArgumentException("Altar recipe grid cannot be empty!") here and left the
            //JsonSyntaxException wording to SimpleAltarRecipeSerializer; this tree's deserialize(..) relies
            //on validate for both checks, so the JSON-typed exception is kept - both are RuntimeExceptions
            //and the serializer reports them the same way.
            throw new JsonSyntaxException("Altar recipe grid cannot be empty");
        }
        for (Integer slot : gridParts.keySet()) {
            if (!type.hasSlot(slot)) {
                throw new JsonSyntaxException("Slot " + slot + " has an ingredient but cannot be used in altar type " + type.name());
            }

            //1.16.5: Ingredient#hasNoMatchingItems() (1.16.5
            //common/crafting/recipe/altar/AltarRecipeGrid.java:147-150) - renamed to Ingredient#isEmpty() in
            //1.20.1. FluidIngredient overrides it (common/crafting/helper/ingredient/FluidIngredient:72) so
            //fluid inputs are not falsely rejected, exactly as in 1.16.5.
            Ingredient input = gridParts.get(slot);
            if (input.isEmpty()) {
                throw new JsonSyntaxException("Input at " + slot + " has no matching items!");
            }
        }
    }

    /*
    //1.16.5 carries these two helpers commented out as well (1.16.5
    //common/crafting/recipe/altar/AltarRecipeGrid.java:154-207). Kept verbatim except that 1.16.5's
    //MapStream utility does not exist in this tree, so re-enabling minimizeGrid would need the two mapKey
    //calls rewritten over a plain HashMap first.
    private AltarRecipeGrid centralizeGrid() {
        int shiftX = (GRID_SIZE - this.getWidth())  / 2;
        int shiftZ = (GRID_SIZE - this.getHeight()) / 2;

        Map<Integer, Ingredient> copy = new HashMap<>();
        for (Integer slotIndex : this.gridParts.keySet()) {
            int index = slotIndex + shiftX + (shiftZ * GRID_SIZE);
            copy.put(index, this.gridParts.get(slotIndex));
        }
        return new AltarRecipeGrid(copy, GRID_SIZE, GRID_SIZE);
    }

    private AltarRecipeGrid minimizeGrid() {
        Map<Integer, Ingredient> mappedIngredients = new HashMap<>(this.gridParts);

        int firstColumn = GRID_SIZE - 1,
                firstRow = GRID_SIZE - 1,
                lastColumn = 0,
                lastRow = 0;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int column = 0; column < GRID_SIZE; column++) {
                int slotIndex = row * GRID_SIZE + column;
                if (mappedIngredients.containsKey(slotIndex)) {
                    if (row < firstRow) {
                        firstRow = row;
                    }
                    if (row > lastRow) {
                        lastRow = row;
                    }
                    if (column < firstColumn) {
                        firstColumn = column;
                    }
                    if (column > lastColumn) {
                        lastColumn = column;
                    }
                }
            }
        }
        if (firstColumn > 0) {
            int xShift = firstColumn;
            mappedIngredients = MapStream.of(mappedIngredients)
                    .mapKey(slot -> slot - xShift)
                    .toMap();
        }
        if (firstRow > 0) {
            int yShift = firstRow * GRID_SIZE;
            mappedIngredients = MapStream.of(mappedIngredients)
                    .mapKey(slot -> slot - yShift)
                    .toMap();
        }
        return new AltarRecipeGrid(mappedIngredients, width, height);
    }
    */

    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.width);
        buffer.writeVarInt(this.height);
        buffer.writeVarInt(gridParts.size());
        gridParts.forEach((slot, ingredient) -> {
            buffer.writeVarInt(slot);
            new WrappedIngredient(ingredient).write(buffer);
        });
    }

    public static AltarRecipeGrid read(FriendlyByteBuf buffer) {
        int width = buffer.readVarInt();
        int height = buffer.readVarInt();
        int size = buffer.readVarInt();
        Map<Integer, Ingredient> ingredients = new HashMap<>();
        for (int i = 0; i < size; i++) {
            int slot = buffer.readVarInt();
            ingredients.put(slot, WrappedIngredient.read(buffer).getIngredient());
        }
        return new AltarRecipeGrid(ingredients, width, height);
    }

    public static AltarRecipeGrid deserialize(AltarType type, JsonObject json) {
        JsonArray pattern = GsonHelper.getAsJsonArray(json, "pattern");
        JsonObject keys = GsonHelper.getAsJsonObject(json, "key");
        Map<Integer, Character> patternMap = new HashMap<>();
        Set<Character> usedChars = new HashSet<>();
        for (int i = 0; i < MAX_INVENTORY_SIZE; i++) {
            patternMap.put(i, '_');
        }

        for (int row = 0; row < Math.min(pattern.size(), GRID_SIZE); row++) {
            String line = GsonHelper.convertToString(pattern.get(row), "pattern[" + row + "]");
            if (line.length() > GRID_SIZE) {
                throw new JsonSyntaxException("Invalid altar pattern: too many columns");
            }
            char[] chars = line.toCharArray();
            for (int column = 0; column < chars.length; column++) {
                char c = chars[column];
                if (SKIP_CHARS.matcher(String.valueOf(c)).matches()) {
                    continue;
                }
                usedChars.add(c);
                patternMap.put(row * GRID_SIZE + column, c);
            }
        }

        Map<Integer, Ingredient> mappedIngredients = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : keys.entrySet()) {
            String key = entry.getKey();
            if (key.length() != 1) {
                throw new JsonSyntaxException("Altar recipe keys must be a single character");
            }
            char c = key.charAt(0);
            if (SKIP_CHARS.matcher(String.valueOf(c)).matches()) {
                continue;
            }
            if (!usedChars.contains(c)) {
                throw new JsonSyntaxException("Altar recipe key '" + key + "' is not used in the pattern");
            }

            Ingredient ingredient = deserializeIngredient(entry.getValue());
            for (int index = 0; index < MAX_INVENTORY_SIZE; index++) {
                if (patternMap.get(index) == c) {
                    mappedIngredients.put(index, ingredient);
                }
            }
            usedChars.remove(c);
        }

        if (!usedChars.isEmpty()) {
            throw new JsonSyntaxException("Pattern uses keys without definitions: " + usedChars);
        }
        AltarRecipeGrid grid = new AltarRecipeGrid(mappedIngredients);
        grid.validate(type);
        return grid;
    }

    private static Ingredient deserializeIngredient(JsonElement element) {
        if (!element.isJsonObject()) {
            return Ingredient.fromJson(element);
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has("type")) {
            return Ingredient.fromJson(element);
        }
        ResourceLocation type = ResourceLocation.tryParse(GsonHelper.getAsString(object, "type"));
        if (type == null) {
            return Ingredient.EMPTY;
        }
        if (type.toString().equals("astralsorcery:crystal")) {
            return Ingredient.of(ItemsAS.ROCK_CRYSTAL.get(), ItemsAS.CELESTIAL_CRYSTAL.get());
        }
        if (type.toString().equals("astralsorcery:fluid")) {
            return FluidIngredient.fromJson(object);
        }
        return Ingredient.fromJson(element);
    }

    public void serialize(JsonObject object) {
        JsonArray pattern = new JsonArray();
        JsonObject keys = new JsonObject();

        Map<JsonElement, String> revMap = new HashMap<>();
        Map<String, JsonElement> ingredientMap = new HashMap<>();
        Map<Integer, String> patternMap = new HashMap<>();
        char c = 'A';

        for (Map.Entry<Integer, Ingredient> entry : this.gridParts.entrySet()) {
            Integer slotIndex = entry.getKey();
            Ingredient value = entry.getValue();
            JsonElement jsonIngredient = value.toJson();
            if (!revMap.containsKey(jsonIngredient)) {
                String strKey = String.valueOf(c);
                revMap.put(jsonIngredient, strKey);
                patternMap.put(slotIndex, strKey);
                ingredientMap.put(strKey, jsonIngredient);
                c++;
            } else {
                patternMap.put(slotIndex, revMap.get(jsonIngredient));
            }
        }

        for (int xx = 0; xx < GRID_SIZE; xx++) {
            StringBuilder line = new StringBuilder();
            for (int zz = 0; zz < GRID_SIZE; zz++) {
                int slotIndex = xx * GRID_SIZE + zz;
                line.append(patternMap.getOrDefault(slotIndex, "_"));
            }
            pattern.add(line.toString());
        }
        object.add("pattern", pattern);

        ingredientMap.forEach((key, ingredient) -> keys.add(key, ingredient));
        object.add("key", keys);
    }

    public static class Builder {

        private final LinkedList<String> pattern = new LinkedList<>();
        private final Map<Character, Ingredient> inputMapping = new HashMap<>();

        public Builder patternLine(String line) {
            if (line.length() > GRID_SIZE) {
                throw new IllegalArgumentException("Altar recipe pattern line must not be more than 5 characters long! Passed line '" + line + "'");
            }
            if (this.pattern.size() >= GRID_SIZE) {
                throw new IllegalArgumentException("Altar recipe pattern must not have more than 5 lines total!");
            }
            this.pattern.add(line);
            return this;
        }

        //1.16.5: key(Character, ITag.INamedTag<Item>) + Ingredient.fromTag - item tags are TagKey<Item> in
        //1.20.1 and the factory is Ingredient.of(TagKey).
        public Builder key(Character key, TagKey<Item> tagIn) {
            return this.key(key, Ingredient.of(tagIn));
        }

        //1.16.5: key(Character, IItemProvider) + Ingredient.fromItems - IItemProvider is ItemLike and the
        //factory Ingredient.of(ItemLike...) in 1.20.1.
        public Builder key(Character key, ItemLike itemIn) {
            return this.key(key, Ingredient.of(itemIn));
        }

        //1.16.5: FluidAttributes.BUCKET_VOLUME -> FluidType.BUCKET_VOLUME (Forge dropped FluidAttributes in
        //1.19).
        public Builder key(Character key, Fluid fluid) {
            return this.key(key, new FluidIngredient(new FluidStack(fluid, FluidType.BUCKET_VOLUME)));
        }

        public Builder key(Character key, Ingredient input) {
            if (this.inputMapping.containsKey(key)) {
                throw new IllegalArgumentException("Character '" + key + "' is already defined!");
            }
            if (key.equals(' ') || key.equals('_')) {
                throw new IllegalArgumentException("Character ' ' (whitespace) or '_' (underscore) is reserved and cannot be defined!");
            }
            this.inputMapping.put(key, input);
            return this;
        }

        public AltarRecipeGrid build() {
            int mostWidth = this.pattern.stream()
                    .map(String::length)
                    .max(Integer::compareTo)
                    .orElseThrow(() -> new IllegalArgumentException("No pattern is defined for altar recipe!"));
            int mostHeight = (int) this.pattern.stream()
                    .filter(s -> !s.isEmpty())
                    .count();
            if (mostHeight == 0 || mostWidth == 0) {
                throw new IllegalArgumentException("Altar recipe grid pattern is empty!");
            }
            int shiftZ = (GRID_SIZE - mostHeight) / 2;
            for (int i = 0; i < shiftZ; i++) {
                this.pattern.addFirst(StringUtils.repeat('_', GRID_SIZE));
            }
            for (int i = 0; i < (GRID_SIZE - mostHeight - shiftZ); i++) {
                this.pattern.add(StringUtils.repeat('_', GRID_SIZE));
            }

            List<String> patternLines = new LinkedList<>();
            int shiftX = (GRID_SIZE - mostWidth) / 2;
            for (String line : this.pattern) {
                String newLine = StringUtils.repeat("_", shiftX) + line + StringUtils.repeat("_", GRID_SIZE - mostWidth - shiftX);
                patternLines.add(newLine);
            }

            HashSet<Character> foundCharacters = new HashSet<>();
            for (String line : patternLines) {
                for (char ch : line.toCharArray()) {
                    if (!SKIP_CHARS.matcher(String.valueOf(ch)).matches()) {
                        foundCharacters.add(ch);
                    }
                }
            }
            if (!this.inputMapping.keySet().containsAll(foundCharacters)) {
                String missingCharacters = foundCharacters.stream()
                        .filter(key -> !this.inputMapping.containsKey(key))
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.joining(", "));
                throw new IllegalArgumentException("No matching input found for characters " + missingCharacters);
            }

            Map<Integer, Ingredient> ingredientMap = new HashMap<>();
            for (int lineIndex = 0; lineIndex < patternLines.size(); lineIndex++) {
                char[] charArray = patternLines.get(lineIndex).toCharArray();
                for (int cIndex = 0; cIndex < charArray.length; cIndex++) {
                    Character character = charArray[cIndex];
                    if (!SKIP_CHARS.matcher(String.valueOf(character)).matches()) {
                        int slotIndex = lineIndex * GRID_SIZE + cIndex;
                        ingredientMap.put(slotIndex, this.inputMapping.get(character));
                    }
                }
            }
            return new AltarRecipeGrid(ingredientMap);
        }
    }
}
