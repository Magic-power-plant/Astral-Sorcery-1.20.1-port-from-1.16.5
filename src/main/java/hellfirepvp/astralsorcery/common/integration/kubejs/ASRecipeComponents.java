/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.RegistryComponent;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.util.MapJS;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ASRecipeComponents
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//The KubeJS RecipeComponents that the five Astral Sorcery schemas are built from. Everything here is only
//ever reached from a schema's static initialiser, which in turn is only reached from IntegrationKubeJS -
//i.e. only when KubeJS itself loaded the plugin. Nothing on a normal code path touches this class.
//
//Why so many of these are hand-written rather than KubeJS built-ins: KubeJS' FluidComponents write
//{"fluid": "<id>", "amount": n} (dev.latvian.mods.kubejs.fluid.FluidStackJS#toJson), but every fluid field
//Astral Sorcery reads is a bare id string with the amount in a SEPARATE int field - WellLiquefaction's
//"output", LiquidInfusion's "fluidInput", LiquidInteraction's "reactant1"/"reactant2". Feeding a KubeJS
//fluid component into those would produce json the serializers reject outright, so the fluid fields use a
//RegistryComponent, which writes exactly the bare id string.
public final class ASRecipeComponents {

    private ASRecipeComponents() {}

    /**
     * A bare fluid id string, e.g. "astralsorcery:liquid_starlight". Reads a Fluid, a fluid id string or a
     * json string; writes {@code new JsonPrimitive(id)} - see RegistryComponent#write.
     */
    public static final RecipeComponent<Fluid> FLUID_ID = new RegistryComponent<>(RegistryInfo.FLUID);

    /**
     * A block id written as the {"block": "<id>"} object BlockStateHelper#deserializeObject wants. A script
     * may hand over a Block, an id string, a json string, or that object form directly.
     */
    public static final RecipeComponent<JsonObject> BLOCK_STATE_OBJECT = new JsonObjectComponent("astralsorcery:block_state") {
        @Override
        protected boolean isOwnShape(JsonObject object) {
            return object.has("block");
        }

        @Override
        protected JsonObject normalize(JsonObject object) {
            //Passed through rather than rebuilt from the id: one shipped recipe's output is
            //{"block": "minecraft:cake", "properties": [{"name": "bites", "value": "0"}]}
            //(data/astralsorcery/recipes/block_transmutation/pumpkin_cake.json), and rebuilding would drop
            //"properties". BlockStateHelper#deserializeObject ignores it today, but silently deleting a field
            //the shipped data carries is not this component's call to make.
            return object;
        }

        @Override
        protected JsonObject readFallback(RecipeJS recipe, Object from) {
            //RegistryComponent resolves a Block/id string/json string, and its own write() turns the Block
            //back into the bare id primitive - so KubeJS' id handling is reused in both directions.
            return ASRecipeJson.blockStateObject(BLOCK_ID.write(recipe, BLOCK_ID.read(recipe, from)));
        }
    };

    /**
     * One block-transmutation input option, as a BlockMatchInformation object. A script may write
     * {@code "<block id>"} or {@code "#<block tag>"} instead and it is wrapped into that object; the tag form
     * is a string because it has no Block to resolve to at all, and BlockMatchInformation's tag branch defers
     * its own lookup anyway (see the AS-ALT-07 note on common/crafting/BlockTransmutation.BlockMatchInformation).
     */
    public static final RecipeComponent<JsonObject> BLOCK_MATCH = new JsonObjectComponent("astralsorcery:block_match") {
        @Override
        protected boolean isOwnShape(JsonObject object) {
            return object.has("block") || object.has("tag");
        }

        @Override
        protected JsonObject normalize(JsonObject object) {
            //Passed through for the same reason as BLOCK_STATE_OBJECT: nine of the ten shipped
            //block_transmutation recipes carry a "display" stack on their input entry, which is what the
            //journal and JEI show for that input, and rebuilding the object from the block id alone would
            //throw it away.
            return object;
        }

        @Override
        protected JsonObject readFallback(RecipeJS recipe, Object from) {
            return ASRecipeJson.blockMatch(new JsonPrimitive(String.valueOf(
                    from instanceof CharSequence ? from : BLOCK_ID.write(recipe, BLOCK_ID.read(recipe, from)).getAsString())));
        }
    };

    //Not a schema component itself - only the id-resolution half of the two block components above, which
    //need to turn a Block or an id string into an id and back.
    private static final RegistryComponent<Block> BLOCK_ID = new RegistryComponent<>(RegistryInfo.BLOCK);

    /**
     * The altar tier. Always writes the int ordinal into "altar_type", which is the primary key
     * AltarRecipeSerializer#readAltarType reads and the form all 117 shipped altar recipes use, but accepts
     * the enum name from script as well so a pack author can write altarType: 'CONSTELLATION'.
     * <p>
     * An out-of-range ordinal is rejected here. AltarRecipeSerializer#readAltarType silently falls back to
     * DISCOVERY for one, which would turn a typo into a recipe that quietly lands on the wrong altar.
     */
    public static final RecipeComponent<Integer> ALTAR_TYPE = new AltarTypeComponent();

    private static final class AltarTypeComponent implements RecipeComponent<Integer> {

        @Override
        public String componentType() {
            return "astralsorcery:altar_type";
        }

        @Override
        public Class<?> componentClass() {
            return Integer.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, Integer value) {
            return new JsonPrimitive(value);
        }

        @Override
        public Integer read(RecipeJS recipe, Object from) {
            if (from instanceof AltarType type) {
                return type.ordinal();
            }
            if (from instanceof Number number) {
                return checkOrdinal(number.intValue());
            }
            if (from instanceof JsonPrimitive primitive) {
                return primitive.isNumber() ? checkOrdinal(primitive.getAsInt()) : byName(primitive.getAsString());
            }
            if (from instanceof CharSequence sequence) {
                return byName(sequence.toString());
            }
            throw new JsonSyntaxException("Cannot read an altar type from " + from + "; expected " + validNames());
        }

        private static int checkOrdinal(int ordinal) {
            AltarType[] values = AltarType.values();
            if (ordinal < 0 || ordinal >= values.length) {
                throw new JsonSyntaxException("Altar type ordinal " + ordinal + " is out of range; expected 0.."
                        + (values.length - 1) + " or " + validNames());
            }
            return ordinal;
        }

        private static int byName(String name) {
            String upper = name.toUpperCase(Locale.ROOT);
            for (AltarType type : AltarType.values()) {
                if (type.name().equals(upper)) {
                    return type.ordinal();
                }
            }
            //A bare number arriving as a string - json and rhino both do this - is still an ordinal.
            try {
                return checkOrdinal(Integer.parseInt(name.trim()));
            } catch (NumberFormatException ignored) {
                throw new JsonSyntaxException("Unknown altar type '" + name + "'; expected " + validNames());
            }
        }

        private static String validNames() {
            return Stream.of(AltarType.values()).map(Enum::name).collect(Collectors.joining(", "));
        }
    }

    /**
     * A json object handed through to the serializer untouched. Used for the altar's "options" block, whose
     * shape is decided by the recipe class named in "recipe_class" rather than by the altar format itself -
     * AltarRecipe#deserializeAdditionalJson is overridden per subclass - so there is nothing stable for a
     * schema to describe.
     */
    public static final RecipeComponent<JsonObject> RAW_JSON_OBJECT = new JsonObjectComponent("astralsorcery:raw_json") {
        @Override
        protected JsonObject normalize(JsonObject object) {
            return object;
        }
    };

    /**
     * The liquid-interaction result, i.e. {"id": "<result id>", "data": {..}}. A script may hand over that
     * object directly, or - for the overwhelmingly common drop_item case - just an item, which is wrapped
     * into the drop_item form.
     */
    public static final RecipeComponent<JsonObject> INTERACTION_RESULT = new JsonObjectComponent("astralsorcery:interaction_result") {
        @Override
        protected boolean isOwnShape(JsonObject object) {
            //Only an object that actually names a result type is a result; everything else - a bare item id,
            //Item.of(..), {item:..,count:..} - is the item to drop.
            return object.has("id");
        }

        @Override
        protected JsonObject normalize(JsonObject object) {
            return ASRecipeJson.validateInteractionResult(object);
        }

        @Override
        protected JsonObject readFallback(RecipeJS recipe, Object from) {
            //OutputItem.of / JsonHelper#serializeItemStack rather than RecipeJS#readOutputItem /
            //#writeOutputItem, which would be the natural pair: RecipeJS implements
            //dev.latvian.mods.rhino.util.CustomJavaToJsWrapper, and rhino is NOT on this build's compile
            //classpath (kubejs-forge is pulled in with transitive = false), so javac cannot resolve any
            //method call on a RecipeJS. OutputItem has no rhino type in its hierarchy, and
            //JsonHelper#serializeItemStack emits the same {item,count,nbt} object RecipeJS#writeOutputItem
            //does - which is also exactly what ResultDropItem#read reads back through
            //JsonHelper#getItemStack.
            ItemStack stack = OutputItem.of(from).item;
            if (stack.isEmpty()) {
                //ResultDropItem#read would reject this too, but only at datapack load and without naming
                //the script value that produced it.
                throw new JsonSyntaxException("A liquid interaction result has to be either an item to drop or "
                        + "{id: '<result id>', data: {..}}, got: " + from);
            }
            return ASRecipeJson.dropItemResult(JsonHelper.serializeItemStack(stack));
        }
    };

    /**
     * Shared plumbing for the two json-object components: turns whatever rhino handed us into a JsonObject
     * and then lets the subclass validate it.
     */
    private abstract static class JsonObjectComponent implements RecipeComponent<JsonObject> {

        private final String type;

        private JsonObjectComponent(String type) {
            this.type = type;
        }

        protected abstract JsonObject normalize(JsonObject object);

        /**
         * Whether an object-shaped value is this component's own json, as opposed to something the subclass
         * wants to reinterpret in {@link #readFallback}.
         */
        protected boolean isOwnShape(JsonObject object) {
            return true;
        }

        protected JsonObject readFallback(RecipeJS recipe, Object from) {
            throw new JsonSyntaxException("Expected " + this.type + " to be an object, got: " + from);
        }

        @Override
        public String componentType() {
            return this.type;
        }

        @Override
        public Class<?> componentClass() {
            return JsonObject.class;
        }

        @Override
        public JsonElement write(RecipeJS recipe, JsonObject value) {
            return value;
        }

        @Override
        public JsonObject read(RecipeJS recipe, Object from) {
            if (from instanceof JsonObject object) {
                return this.isOwnShape(object) ? this.normalize(object) : this.readFallback(recipe, from);
            }
            //MapJS#of returns null for anything that is not map-like, which is the signal to fall back.
            Map<?, ?> map = MapJS.of(from);
            if (map != null) {
                JsonObject json = MapJS.json(map);
                if (json != null && this.isOwnShape(json)) {
                    return this.normalize(json);
                }
            }
            return this.readFallback(recipe, from);
        }
    }
}
