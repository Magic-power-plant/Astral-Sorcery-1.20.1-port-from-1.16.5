/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration;

import hellfirepvp.astralsorcery.common.constellation.*;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.tile.altar.AltarType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IntegrationCraftTweaker
 * Created by Jaredlll08
 * Date: 03.17.2021 / 15:36
 */
//BLOCKED_PORT[AS-INT-03]: 1.16.5 imported com.blamejared.crafttweaker.api.CraftTweakerAPI and
// com.blamejared.crafttweaker.impl.commands.CTCommandCollectionEvent
// (1.16.5 IntegrationCraftTweaker.java:11-12). No CraftTweaker artifact is on the compile classpath of
// this port (build.gradle declares no com.blamejared.crafttweaker dependency), so neither type can be
// named and the single listener this class installs cannot be written.
// On top of the missing artifact the event itself is gone: CraftTweaker for 1.20.1 deleted
// CTCommandCollectionEvent together with the whole "registerDump" command-collection mechanism (dumps
// are now contributed by annotating a static method with @CraftTweakerCommand / via
// com.blamejared.crafttweaker.api.CraftTweakerRegistries command registration), and
// CraftTweakerAPI.logDump was replaced by CraftTweakerAPI.LOGGER / the per-script logger. So even with
// the dependency restored this file needs a rewrite against the new command API rather than a
// mechanical translation - that rewrite is deliberately not invented here.
// 1.20.1 translations that DO apply to the rest of the reference body, so that unblocking is
// mechanical: TextFormatting -> ChatFormatting, new StringTextComponent(s) -> Component.literal(s),
// CommandSource#sendFeedback(ITextComponent, boolean) ->
// CommandSourceStack#sendSuccess(Supplier<Component>, boolean) (the component became a supplier in
// 1.20.1, i.e. "commandContext.getSource().sendSuccess(() -> message, true)").
//TODO_PORT[AS-PKG-01]: 1.16.5 took AltarType from common.block.tile.altar; this tree has it under
// common.tile.altar.
public class IntegrationCraftTweaker {

    public static void attachListeners(IEventBus eventBus) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 body (1.16.5 IntegrationCraftTweaker.java:29):
        //   eventBus.addListener(IntegrationCraftTweaker::onCommandCollection);
        // IEventBus#addListener only accepts a Consumer of an actual Event subclass, so the reference
        // registration cannot be expressed while CTCommandCollectionEvent is unavailable - the
        // degraded Object-typed onCommandCollection below is not an Event and would not compile here.
    }

    /**
     * 1.16.5 signature: public static void onCommandCollection(CTCommandCollectionEvent event)
     * The parameter is degraded to Object following the convention this tree uses for third-party
     * types that are not on the classpath (see
     * common/crafting/recipe/interaction/jei/JEIInteractionResultHandler.java).
     */
    public static void onCommandCollection(Object event) {
        //BLOCKED_PORT[AS-INT-03]: 1.16.5 body (1.16.5 IntegrationCraftTweaker.java:34-55):
        //   event.registerDump("astralConstellations", "Lists the different Astral Sorcery Constellations", commandContext -> {
        //
        //       CraftTweakerAPI.logDump("List of all known Astral Sorcery Constellations: ");
        //       RegistriesAS.REGISTRY_CONSTELLATIONS.getKeys().forEach(resourceLocation -> {
        //           IConstellation constellation = RegistriesAS.REGISTRY_CONSTELLATIONS.getValue(resourceLocation);
        //           CraftTweakerAPI.logDump("%s\tis weak: %s, is major: %s", resourceLocation.toString(), constellation instanceof IWeakConstellation, constellation instanceof IMajorConstellation);
        //       });
        //
        //       final StringTextComponent message = new StringTextComponent(TextFormatting.GREEN + "Constellations written to the log" + TextFormatting.RESET);
        //       commandContext.getSource().sendFeedback(message, true);
        //       return 0;
        //   });
        //   event.registerDump("astralAltarTypes", "Lists the different Astral Sorcery Altar Types", commandContext -> {
        //
        //       CraftTweakerAPI.logDump("List of all known Astral Sorcery Altar Types: ");
        //       for(AltarType value : AltarType.values()) {
        //           CraftTweakerAPI.logDump(value.name());
        //       }
        //       final StringTextComponent message = new StringTextComponent(TextFormatting.GREEN + "Altar Types written to the log" + TextFormatting.RESET);
        //       commandContext.getSource().sendFeedback(message, true);
        //       return 0;
        //   });
        // Everything the dumps read is ported and available in this tree:
        // RegistriesAS.REGISTRY_CONSTELLATIONS is an IForgeRegistry<IConstellation> (so getKeys() and
        // getValue(ResourceLocation) are unchanged), IWeakConstellation/IMajorConstellation exist under
        // common.constellation, and AltarType.values() exists under common.tile.altar. The 1.20.1
        // spellings of the two feedback lines are:
        //   final Component message = Component.literal(ChatFormatting.GREEN + "Constellations written to the log" + ChatFormatting.RESET);
        //   commandContext.getSource().sendSuccess(() -> message, true);
        // Only the CraftTweaker halves - event.registerDump and CraftTweakerAPI.logDump - are missing.
    }
}
