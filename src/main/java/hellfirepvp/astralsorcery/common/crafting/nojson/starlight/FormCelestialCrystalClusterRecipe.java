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
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.crafting.helper.ingredient.CrystalIngredient;
import hellfirepvp.astralsorcery.api.crystal.CrystalAttributeItem;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalGenerator;
import hellfirepvp.astralsorcery.common.data.config.entry.CraftingConfig;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.item.ItemStardust;
import hellfirepvp.astralsorcery.common.item.crystal.ItemCrystalBase;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.blockentity.crystal.CelestialCrystalsBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
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
 * Class: FormCelestialCrystalClusterRecipe
 * Created by HellFirePvP
 * Date: 01.10.2019 / 21:32
 */
public class FormCelestialCrystalClusterRecipe extends LiquidStarlightRecipe {

    public FormCelestialCrystalClusterRecipe() {
        super(AstralSorcery.key("form_celestial_crystal_cluster"));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Ingredient> getInputForRender() {
        return Arrays.asList(Ingredient.of(new ItemStack(ItemsAS.STARDUST.get())),
                new CrystalIngredient(false, false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Ingredient> getOutputForRender() {
        return Collections.singletonList(Ingredient.of(new ItemStack(BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get())));
    }

    @Override
    public boolean doesStartRecipe(ItemStack item) {
        if (!CraftingConfig.CONFIG.liquidStarlightFormCelestialCrystalCluster.get()) {
            return false;
        }
        //VERIFY_PORT[AS-LSR-01]: ItemsAS.STARDUST is currently registered as a plain 'new Item(...)' instead of
        // 'new ItemStardust(...)', so this instanceof can never match at runtime. Reported as an external change.
        return item.getItem() instanceof ItemStardust;
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
        if (!world.isClientSide() && getAndIncrementCraftingTick(trigger) > 50 + r.nextInt(20)) {
            ItemStack crystalFound;
            if (consumeItemEntityInBlock(world, at, ItemsAS.STARDUST.get()) != null &&
                    (crystalFound = consumeItemEntityInBlock(world, at, 1, stack -> stack.getItem() instanceof ItemCrystalBase)) != null) {

                // 1.16.5 World#setBlockState(BlockPos, BlockState) is Level#setBlockAndUpdate(BlockPos, BlockState)
                if (world.setBlockAndUpdate(at, BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get().defaultBlockState())) {
                    CelestialCrystalsBlockEntity cluster = MiscUtils.getBlockEntityAt(world, at, CelestialCrystalsBlockEntity.class, true);
                    if (cluster != null) {
                        CrystalAttributes attr = ((CrystalAttributeItem) crystalFound.getItem()).getAttributes(crystalFound);
                        ItemStack targetCrystal = new ItemStack(ItemsAS.CELESTIAL_CRYSTAL.get());
                        ((CrystalAttributeItem) crystalFound.getItem()).setAttributes(targetCrystal, attr);
                        cluster.setAttributes(CrystalGenerator.upgradeProperties(targetCrystal));
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void doClientEffectTick(ItemEntity trigger, Level world, BlockPos at) {
        for (int i = 0; i < 3; i++) {
            Vector3 pos = Vector3.atEntityCorner(trigger);
            MiscUtils.applyRandomOffset(pos, rand, 0.15F);

            Vector3 motion = Vector3.RotAxis.Y_AXIS.clone();
            motion.rotate(Math.toRadians(10 + rand.nextInt(20)), Vector3.RotAxis.X_AXIS)
                .rotate(rand.nextFloat() * Math.PI * 2, Vector3.RotAxis.Y_AXIS)
                .normalize().multiply(0.07F + rand.nextFloat() * 0.04F);

            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setMotion(motion)
                    .setScaleMultiplier(0.05F + rand.nextFloat() * 0.2F)
                    .setMaxAge(30 + rand.nextInt(20));
        }
        for (int i = 0; i < 4; i++) {
            Vector3 target = Vector3.atEntityCorner(trigger);
            Vector3 pos = target.clone().add(Vector3.random().normalize().multiply(3 + rand.nextFloat()));

            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .alpha(VFXAlphaFunction.PYRAMID.andThen(VFXAlphaFunction.proximity(target::clone, 2)))
                    .motion(VFXMotionController.target(target::clone, 0.1F))
                    .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                    .setMaxAge(20 + rand.nextInt(20));
        }
    }
}
