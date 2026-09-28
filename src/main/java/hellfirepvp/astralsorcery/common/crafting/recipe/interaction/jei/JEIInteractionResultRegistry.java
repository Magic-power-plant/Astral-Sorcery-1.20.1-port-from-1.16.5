/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction.jei;

//1.16.5 imported hellfirepvp.astralsorcery.common.crafting.recipe.interaction.InteractionResultRegistry; this
//tree keeps the registry as a top-level class in common/crafting (tracked as TODO_PORT[AS-PKG-01]).
import hellfirepvp.astralsorcery.common.crafting.InteractionResultRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIInteractionResultRegistry
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:47
 */
public class JEIInteractionResultRegistry {

    private static final Map<ResourceLocation, JEIInteractionResultHandler> interactionRegistry = new HashMap<>();

    public static void register(ResourceLocation key, JEIInteractionResultHandler interactionResultHandler) {
        interactionRegistry.put(key, interactionResultHandler);
    }

    public static Optional<JEIInteractionResultHandler> get(ResourceLocation key) {
        return Optional.ofNullable(interactionRegistry.get(key));
    }

    static {
        register(InteractionResultRegistry.ID_DROP_ITEM, new JEIHandlerDropItem());
        register(InteractionResultRegistry.ID_SPAWN_ENTITY, new JEIHandlerSpawnEntity());
    }
}
