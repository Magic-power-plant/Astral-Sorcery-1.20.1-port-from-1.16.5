/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IntegrationKubeJS
 * Created by HellFirePvP
 * Date: 27.09.2025 / 00:00
 */
//Entry point of the KubeJS integration. It is reached exactly one way: KubeJSPlugins#load reads the file
//"kubejs.plugins.txt" from the root of every mod jar and reflectively instantiates the class names in it,
//then KubeJSPlugins#forEachPlugin fires registerRecipeSchemas on each. src/main/resources/kubejs.plugins.txt
//names this class.
//
//That discovery file - NOT META-INF/services - is the mechanism in KubeJS 2001.x; verified by disassembling
//kubejs-forge-2001.6.5-build.26.jar, which ships its own kubejs.plugins.txt at the jar root and whose
//KubeJSPlugins#load loads the string constant "kubejs.plugins.txt". The base class is
//dev.latvian.mods.kubejs.KubeJSPlugin (there is no dev.latvian.mods.kubejs.plugin package in this line) and
//the hook is registerRecipeSchemas(RegisterRecipeSchemasEvent).
//
//Because nothing in Astral Sorcery references this class, an installation without KubeJS never loads it and
//never touches a dev.latvian.mods.** type - which is why kubejs-forge can stay a compileOnly dependency and
//no Mods enum guard is needed for it.
//
//This is server-side only: recipe schemas are consumed by KubeJS' server script context when the datapack is
//(re)loaded. Nothing here reaches net.minecraft.client.**.
public class IntegrationKubeJS extends KubeJSPlugin {

    //These are RECIPE SERIALIZER ids, not recipe TYPE ids. RecipeJS writes the id a schema is registered
    //under into the recipe json's "type" field, and RecipeSchemaType#getSerializer then resolves that same id
    //against the recipe-serializer registry - so each entry below has to be a key of
    //common/lib/RecipeSerializersAS or the schema throws the moment a script uses it.
    //
    //Two of the five therefore do NOT match the recipe-type id in common/lib/RecipeTypesAS:
    //  recipe type astralsorcery:well      -> serializer astralsorcery:lightwell (and astralsorcery:well)
    //  recipe type astralsorcery:infusion  -> serializer astralsorcery:infuser
    private static final String ALTAR = "altar";
    private static final String LIGHTWELL = "lightwell";
    private static final String WELL = "well";
    private static final String INFUSER = "infuser";
    private static final String LIQUID_INTERACTION = "liquid_interaction";
    private static final String BLOCK_TRANSMUTATION = "block_transmutation";

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.register(id(ALTAR), AltarRecipeSchema.SCHEMA);

        //RecipeSerializersAS binds one WellLiquefaction.Serializer to both ids, so both are offered. Two
        //separate RecipeSchema instances rather than one shared one: a RecipeSchema carries its own
        //constructor table and uuid, and registering the same instance twice would give two RecipeSchemaTypes
        //that claim the same identity.
        event.register(id(LIGHTWELL), WellRecipeSchema.create());
        event.register(id(WELL), WellRecipeSchema.create());

        event.register(id(INFUSER), InfusionRecipeSchema.SCHEMA);
        event.register(id(LIQUID_INTERACTION), LiquidInteractionRecipeSchema.SCHEMA);
        event.register(id(BLOCK_TRANSMUTATION), BlockTransmutationRecipeSchema.SCHEMA);

        //Deliberately logged: this is the only observable proof that the kubejs.plugins.txt discovery worked.
        //Without it a missing/renamed discovery file looks exactly like "my script did nothing".
        AstralSorcery.LOG.info("Registered Astral Sorcery recipe schemas with KubeJS: {}, {}, {}, {}, {}, {}",
                ALTAR, LIGHTWELL, WELL, INFUSER, LIQUID_INTERACTION, BLOCK_TRANSMUTATION);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(AstralSorcery.MODID, path);
    }
}
