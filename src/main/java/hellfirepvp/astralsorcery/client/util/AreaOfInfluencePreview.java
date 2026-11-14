/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXCube;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.ITickHandler;
import hellfirepvp.astralsorcery.common.blockentity.base.AreaOfInfluenceBlockEntity;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.EnumSet;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AreaOfInfluencePreview
 * Created by HellFirePvP
 * Date: 25.04.2020 / 15:53
 */
public class AreaOfInfluencePreview implements ITickHandler {

    public static final AreaOfInfluencePreview INSTANCE = new AreaOfInfluencePreview();

    private static final int MAX_LIFE = 40;
    private static final float alphaTick = 1F / MAX_LIFE;
    private static final float sizeCube1 = 1.25F, sizeCube2 = 1.35F;

    //1.16.5: RegistryKey<World> -> 1.20.1: ResourceKey<Level>
    private ResourceKey<Level> blockEntityDimension = null;
    private BlockPos blockEntityPosition = null;
    private FXCube effect1 = null, effect2 = null;

    private AreaOfInfluencePreview() {}

    public void showOrRemoveIdentical(AreaOfInfluenceBlockEntity aoeBlockEntity) {
        if (this.blockEntityDimension == aoeBlockEntity.getDimension() && aoeBlockEntity.getEffectOriginPosition().equals(this.blockEntityPosition)) {
            this.clearClient();
            return;
        }
        this.show(aoeBlockEntity);
    }

    public void show(AreaOfInfluenceBlockEntity aoeBlockEntity) {
        //1.16.5: TileEntity -> 1.20.1: BlockEntity
        if (!(aoeBlockEntity instanceof BlockEntity)) {
            return;
        }
        this.blockEntityDimension = aoeBlockEntity.getDimension();
        this.blockEntityPosition = aoeBlockEntity.getEffectOriginPosition();
    }

    public void clearClient() {
        this.blockEntityDimension = null;
        this.blockEntityPosition = null;
    }

    @Override
    public void tick(TickEvent.Type type, Object... context) {
        if (blockEntityDimension == null || blockEntityPosition == null) {
            this.removeEffects();
            return;
        }
        //1.16.5: Minecraft#world -> 1.20.1: Minecraft#level
        Level clientWorld = Minecraft.getInstance().level;
        if (clientWorld == null) {
            this.clearClient();
            this.removeEffects();
            return;
        }
        //1.16.5: World#getDimensionKey() -> 1.20.1: Level#dimension()
        ResourceKey<Level> clientDimType = clientWorld.dimension();
        if (!clientDimType.equals(this.blockEntityDimension)) {
            this.clearClient();
            this.removeEffects();
            return;
        }
        AreaOfInfluenceBlockEntity aoeBlockEntity = MiscUtils.getBlockEntityAt(clientWorld, this.blockEntityPosition, AreaOfInfluenceBlockEntity.class, true);
        if (aoeBlockEntity != null && aoeBlockEntity.providesEffect() && shouldContinueEffect(aoeBlockEntity)) {
            this.effect1 = uptickEffect(this.effect1, sizeCube1, aoeBlockEntity);
            this.effect2 = uptickEffect(this.effect2, sizeCube2, aoeBlockEntity);
        } else {
            this.effect1 = downtickEffect(this.effect1, sizeCube1, aoeBlockEntity);
            this.effect2 = downtickEffect(this.effect2, sizeCube2, aoeBlockEntity);
            if (this.effect1 == null && this.effect2 == null) {
                clearClient();
            }
        }
    }

    private boolean shouldContinueEffect(AreaOfInfluenceBlockEntity aoeBlockEntity) {
        if (Minecraft.getInstance().player == null) {
            return false;
        }
        float effectRadius = aoeBlockEntity.getRadius();
        if (effectRadius <= 0F) {
            return false;
        }
        Vector3 offset = aoeBlockEntity.getEffectPosition();
        double distance = offset.distance(Minecraft.getInstance().player);
        if (distance > effectRadius * 3) {
            return false;
        }
        return distance <= 48;
    }

    private FXCube downtickEffect(@Nullable FXCube cube, float sizeMultiplier, @Nullable AreaOfInfluenceBlockEntity aoeBlockEntity) {
        if (cube != null && !cube.isRemoved()) {
            if (aoeBlockEntity != null) {
                updateEffect(cube, sizeMultiplier, aoeBlockEntity);
            }
            //1.16.5: MathHelper#clamp -> 1.20.1: Mth#clamp
            cube.setAlphaMultiplier(Mth.clamp(cube.getAlphaMultiplier() - alphaTick, 0F, 0.75F));
            if (!this.canRefresh(cube)) {
                cube = null;
            }
        }
        return cube;
    }

    private FXCube uptickEffect(@Nullable FXCube cube, float sizeMultiplier, AreaOfInfluenceBlockEntity aoeBlockEntity) {
        if (cube != null) {
            if (cube.isRemoved()) {
                EffectHelper.refresh(cube, EffectTemplatesAS.CUBE_AREA_OF_EFFECT);
            }
            cube.setAlphaMultiplier(Mth.clamp(cube.getAlphaMultiplier() + alphaTick, 0F, 0.75F));
            updateEffect(cube, sizeMultiplier, aoeBlockEntity);
        } else {
            cube = createCube(sizeMultiplier, aoeBlockEntity);
        }
        return cube;
    }

    private void updateEffect(FXCube cube, float sizeMultiplier, AreaOfInfluenceBlockEntity aoeBlockEntity) {
        Color c = aoeBlockEntity.getEffectColor();
        if (c != null) {
            cube.color(VFXColorFunction.constant(c));
        } else {
            cube.color(VFXColorFunction.WHITE);
        }
        cube.setScaleMultiplier(aoeBlockEntity.getRadius() * sizeMultiplier);
        cube.setPosition(aoeBlockEntity.getEffectPosition());
    }

    private FXCube createCube(float sizeMultiplier, AreaOfInfluenceBlockEntity aoeBlockEntity) {
        FXCube cube = EffectHelper.of(EffectTemplatesAS.CUBE_AREA_OF_EFFECT)
                .spawn(aoeBlockEntity.getEffectPosition())
                .tumble()
                .setTumbleIntensityMultiplier(0.06F)
                .setAlphaMultiplier(2 * alphaTick)
                .alpha((fx, alpha, pTicks) -> alpha)
                .color(VFXColorFunction.WHITE)
                .refresh(fx -> canRefresh((FXCube) fx));
        updateEffect(cube, sizeMultiplier, aoeBlockEntity);
        return cube;
    }

    private void removeEffects() {
        if (this.effect1 != null) {
            this.effect1.setAlphaMultiplier(0F);
            this.effect1 = null;
        }
        if (this.effect2 != null) {
            this.effect2.setAlphaMultiplier(0F);
            this.effect2 = null;
        }
    }

    private boolean canRefresh(FXCube cube) {
        return cube.getAlpha(1F) > 0F;
    }

    @Override
    public EnumSet<TickEvent.Type> getHandledTypes() {
        return EnumSet.of(TickEvent.Type.CLIENT);
    }

    @Override
    public boolean canFire(TickEvent.Phase phase) {
        return phase == TickEvent.Phase.END;
    }

    @Override
    public String getName() {
        return "Area of Effect Preview";
    }
}
