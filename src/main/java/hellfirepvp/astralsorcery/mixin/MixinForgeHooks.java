/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.mixin;

import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.perk.node.key.KeyMagnetDrops;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.loot.LootUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MixinForgeHooks
 * Created by HellFirePvP
 * Date: 01.01.2022 / 10:06
 *
 * 1.16.5 target:
 *   modifyLoot(Lnet/minecraft/util/ResourceLocation;Ljava/util/List;Lnet/minecraft/loot/LootContext;)Ljava/util/List;
 * 1.20.1 target (Forge 47.4.20, verified with javap - ForgeHooks now carries two overloads:
 *   modifyLoot(List, LootContext) which merely delegates, and the 3-arg one below which is the
 *   real implementation and the counterpart of the 1.16.5 method; it has a single areturn, so
 *   {@literal @}At("RETURN") still resolves to exactly one injection point):
 *   modifyLoot(Lnet/minecraft/resources/ResourceLocation;Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;
 *
 * ForgeHooks is a Forge class, so remap = false (as in 1.16.5).
 */
@Mixin(ForgeHooks.class)
public class MixinForgeHooks {

    @Inject(
            method = "modifyLoot(Lnet/minecraft/resources/ResourceLocation;Lit/unimi/dsi/fastutil/objects/ObjectArrayList;Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void runLootTeleportation(ResourceLocation lootTableId, ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ObjectArrayList<ItemStack> loot = cir.getReturnValue();

        if (!LootUtil.doesContextFulfillSet(context, LootContextParamSets.BLOCK)) {
            return;
        }
        //1.16.5 used LootContext#get, whose 1.20.1 counterpart that keeps the "may be absent"
        //contract (THIS_ENTITY and TOOL are optional members of the BLOCK parameter set, and the
        //1.16.5 body null-checks them) is getParamOrNull. LootContext#getParam would throw for
        //every non-player block break instead.
        Entity e = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(e instanceof Player)) {
            return;
        }
        Player player = (Player) e;
        PlayerProgress prog = ResearchHelper.getProgress(player, LogicalSide.SERVER);
        if (!prog.isValid() || !prog.getPerkData().hasPerkEffect(perk -> perk instanceof KeyMagnetDrops)) {
            return;
        }

        //Means we're in the 2nd run of loot manipulation, re-run by top.theillusivec4.curios.common.objects.FortuneBonusMultiplier
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool != null && tool.hasTag() && tool.getTag().contains("HasCuriosFortuneBonus")) {
            loot.removeIf(result -> ItemUtils.dropItemToPlayer(player, result).isEmpty());
            //TODO_PORT[AS-INT-01]: 1.16.5 fell through to the curios fortune-bonus check below
            //instead of returning here. That check always returned when curios had applied a
            //fortune bonus - which is exactly the situation this branch detects - so the loot was
            //consumed exactly once. Without the Curios API on the classpath the bonus below is
            //hard-coded to 0, and falling through would run dropItemToPlayer a second time on the
            //leftovers, duplicating drops. Returning here reproduces the 1.16.5 outcome.
            return;
        }
        //TODO_PORT[AS-INT-01]: 1.16.5 continued with
        //    int curiosFortuneBonus = CuriosApi.getCuriosHelper().getCuriosHandler(player)
        //            .map(ICuriosItemHandler::getFortuneBonus)
        //            .orElse(0);
        //    if (curiosFortuneBonus > 0) {
        //        return; //Do not modify loot, loot modification gets re-run by curios later
        //    }
        //Curios is not a dependency of this port (see build.gradle / mods.toml), so the bonus is
        //always 0 and the guard is a no-op. Restore both this block and the branch above once a
        //1.20.1 Curios integration exists.
        loot.removeIf(result -> ItemUtils.dropItemToPlayer(player, result).isEmpty());
    }
}
