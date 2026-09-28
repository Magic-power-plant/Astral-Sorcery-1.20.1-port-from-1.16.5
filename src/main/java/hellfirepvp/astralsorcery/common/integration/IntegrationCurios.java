/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.integration;

import hellfirepvp.astralsorcery.common.base.Mods;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.InterModComms;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IntegrationCurios
 * Created by HellFirePvP
 * Date: 12.08.2019 / 06:50
 */
//BLOCKED_PORT[AS-INT-01]: 1.16.5 imported top.theillusivec4.curios.api.CuriosApi,
// top.theillusivec4.curios.api.SlotTypeMessage and top.theillusivec4.curios.api.SlotTypePreset
// (1.16.5 IntegrationCurios.java:16-18). No Curios artifact is on the compile classpath of this port -
// build.gradle explicitly dropped the 1.16.5 Curios dependencies and says so in a comment referring to
// TODO_PORT[AS-INT-01] - so none of the three types can be named and both method bodies are blocked.
// Curios also changed both APIs for 1.20.1, so this is not a pure rename once the dependency is back:
//  - SlotTypeMessage/IMC slot registration was removed in Curios 5.x. A mod now declares its slots
//    with a datapack file, data/astralsorcery/curios/slots/necklace.json (the "necklace" slot is still
//    one of the presets Curios ships), and initIMC() disappears entirely. See externalChangesNeeded -
//    the resource has to be added by whoever owns src/main/resources.
//  - CuriosApi.getCuriosHelper().findEquippedCurio(Predicate<ItemStack>, LivingEntity) became
//    CuriosApi.getCuriosHelper().findFirstCurio(LivingEntity, Predicate<ItemStack>) and returns
//    Optional<top.theillusivec4.curios.api.type.capability.ICurio.SlotResult> instead of
//    Optional<ImmutableTriple<String, Integer, ItemStack>>; the stack is SlotResult#stack() rather than
//    ImmutableTriple#right.
// The return type is degraded to Optional<Object> (the convention this tree uses for third-party types
// that are not on the classpath, see
// common/crafting/recipe/interaction/jei/JEIInteractionResultHandler.java) because the caller,
// common/enchantment/amulet/AmuletEnchantmentHelper#getWornAmulet, already consumes Optional<Object>.
public class IntegrationCurios {

    public static void initIMC() {
        //BLOCKED_PORT[AS-INT-01]: 1.16.5 body (1.16.5 IntegrationCurios.java:33-34):
        //   InterModComms.sendTo(Mods.CURIOS.getModId(), SlotTypeMessage.REGISTER_TYPE,
        //           () -> SlotTypePreset.NECKLACE.getMessageBuilder().build());
        // Mods.CURIOS.getModId() and InterModComms.sendTo(String, String, Supplier) both exist in this
        // tree/1.20.1 unchanged; the message key and the payload builder are the Curios types that are
        // missing, and in Curios 5.x the whole IMC channel was replaced by the slot datapack described
        // above, so nothing is invented in its place here.
    }

    /**
     * 1.16.5 signature:
     * Optional<ImmutableTriple<String, Integer, ItemStack>> getCurio(PlayerEntity player, Predicate<ItemStack> match)
     */
    public static Optional<Object> getCurio(Player player, Predicate<ItemStack> match) {
        //BLOCKED_PORT[AS-INT-01]: 1.16.5 body (1.16.5 IntegrationCurios.java:38):
        //   return CuriosApi.getCuriosHelper().findEquippedCurio(match, player);
        // 1.20.1 spelling once the dependency is back:
        //   return CuriosApi.getCuriosHelper().findFirstCurio(player, match).map(result -> result);
        // (or, with the return type widened back to Optional<SlotResult>, without the map).
        return Optional.empty();
    }

}
