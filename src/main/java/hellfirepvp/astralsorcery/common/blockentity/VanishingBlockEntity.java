package hellfirepvp.astralsorcery.common.blockentity;

import hellfirepvp.astralsorcery.common.item.armor.ItemMantle;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class VanishingBlockEntity extends TickableBlockEntity {

    private static final AABB SEARCH_BOX = new AABB(-4, 0, -4, 4, 3, 4);

    public VanishingBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.VANISHING.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getLevel().isClientSide() && this.getTicksExisted() % 5 == 0) {
            boolean removeBlock = true;

            List<Player> players = level.getEntitiesOfClass(Player.class, SEARCH_BOX.move(worldPosition));
            for (Player player : players) {
                if (ItemMantle.getEffect(player, ConstellationsAS.aevitas) != null) {
                    double yDiff = player.getY() - this.worldPosition.getY();

                    if (player.onGround() && yDiff >= 0.95 && yDiff <= 1.15) {
                        if (player.isShiftKeyDown()) {
                            break;
                        }

                        removeBlock = false;
                    } else if (player.isShiftKeyDown() && yDiff >= 0.95 && yDiff <= 2.15) {
                        removeBlock = false;
                    }
                }
            }

            if (removeBlock) {
                this.getLevel().removeBlock(worldPosition, false);
            }
        }

        if (this.getLevel().isClientSide()) {
            this.tickClient();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        for (int i = 0; i < 3; i++) {
            if (rand.nextFloat() < 0.07F) {
                Vector3 at = new Vector3(worldPosition).add(0.5F, 0.5F, 0.5F).add(Vector3.random());
                hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle p = hellfirepvp.astralsorcery.common.effect.Effects.of(hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                        .alpha(hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction.PYRAMID)
                        .setMaxAge(40 + rand.nextInt(10));
                if (rand.nextBoolean()) {
                    p.color(hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction.WHITE);
                } else {
                    p.color(hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction.constant(ColorsAS.RITUAL_CONSTELLATION_AEVITAS));
                }
            }
        }
    }
}
