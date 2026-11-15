/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.starlight;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredient;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.common.effect.visual.Effects;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.item.dust.ItemIlluminationPowder;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: FormGemCrystalClusterRecipe
 * Created by HellFirePvP
 * Date: 16.11.2019 / 13:12
 */
public class FormGemCrystalClusterRecipe extends LiquidStarlightRecipe {

    public FormGemCrystalClusterRecipe() {
        super(AstralSorcery.key("form_gem_crystal_cluster"));
    }

    @Override
    public List<Ingredient> getInputForRender() {
        return Arrays.asList(Ingredient.of(new ItemStack(ItemsAS.ILLUMINATION_POWDER.get())),
                new CrystalIngredient(false, false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(Ingredient.of(new ItemStack(BlocksAS.GEM_CRYSTAL_CLUSTER.get())));
    }

    @Override
    public boolean doesStartRecipe(ItemStack item) {
        if (!CraftingConfig.CONFIG.liquidStarlightFormGemCrystalCluster.get()) {
            return false;
        }
        //VERIFY_PORT[AS-LSR-02]: ItemsAS.ILLUMINATION_POWDER is currently registered as a plain 'new Item(...)'
        // instead of 'new ItemIlluminationPowder(...)', so this instanceof can never match at runtime.
        // Reported as an external change.
        return item.getItem() instanceof ItemIlluminationPowder;
    }

    @Override
    public boolean matches(ItemEntity trigger, Level world, BlockPos at) {
        // 1.16.5 BlockState#isTopSolid(IWorldReader, BlockPos, Entity, Direction) no longer exists in 1.20.1;
        // Forge dropped the entity-sensitive hook (see IForgeBlockState) so the vanilla
        // BlockState#isFaceSturdy(BlockGetter, BlockPos, Direction) is the only remaining equivalent.
        if (!world.getBlockState(at.below()).isFaceSturdy(world, at.below(), Direction.UP)) {
            return false;
        }
        List<Entity> otherEntities = getEntitiesInBlock(world, at);
        otherEntities.remove(trigger);
        Optional<Entity> crystalEntity = otherEntities.stream()
                .filter(e -> e instanceof ItemEntity)
                .filter(e -> ((ItemEntity) e).getItem().getItem() instanceof ItemCrystalBase)
                .findFirst();
        return crystalEntity.isPresent() && otherEntities.size() == 1;
    }

    @Override
    public void doServerCraftTick(ItemEntity trigger, Level world, BlockPos at) {
        // 1.16.5 MathHelper#getPositionRandom(Vec3i) is Mth#getSeed(Vec3i) in official mappings
        Random r = new Random(Mth.getSeed(at));
        if (getAndIncrementCraftingTick(trigger) > 50 + r.nextInt(20)) {
            if (consumeItemEntityInBlock(world, at, ItemsAS.ILLUMINATION_POWDER.get()) != null &&
                    consumeItemEntityInBlock(world, at, 1, stack -> stack.getItem() instanceof ItemCrystalBase) != null) {

                // 1.16.5 World#setBlockState(BlockPos, BlockState) is Level#setBlockAndUpdate(BlockPos, BlockState)
                world.setBlockAndUpdate(at, BlocksAS.GEM_CRYSTAL_CLUSTER.get().defaultBlockState());
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void doClientEffectTick(ItemEntity trigger, Level world, BlockPos at) {
        for (int i = 0; i < 4; i++) {
            Vector3 target = Vector3.atEntityCenter(trigger);
            Vector3 pos = target.clone().add(Vector3.random().normalize().multiply(3 + rand.nextFloat()));

            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .color(VFXColorFunction.constant(ColorsAS.ILLUMINATION_POWDER_2))
                    .alpha(VFXAlphaFunction.PYRAMID.andThen(VFXAlphaFunction.proximity(target::clone, 2F)))
                    .motion(VFXMotionController.target(target::clone, 0.09F))
                    .setScaleMultiplier(0.25F + rand.nextFloat() * 0.2F)
                    .setMaxAge(20 + rand.nextInt(20));
        }
    }
}
