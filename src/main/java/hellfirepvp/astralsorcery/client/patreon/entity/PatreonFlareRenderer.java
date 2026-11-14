/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.entity;
import hellfirepvp.astralsorcery.client.patreon.effect.ClientPatreonEffect;
import hellfirepvp.astralsorcery.common.patreon.entity.PatreonPartialEntity;
import hellfirepvp.astralsorcery.client.data.config.entry.RenderingConfig;
import hellfirepvp.astralsorcery.client.effect.EntityVisualFX;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXPositionController;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingSprite;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffect;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PatreonFlareRenderer
 * Created by HellFirePvP
 * Date: 30.08.2019 / 18:15
 */
public class PatreonFlareRenderer extends PatreonEntityRenderer {

    protected FXFacingSprite clientSprite = null;

    public PatreonFlareRenderer(PatreonPartialEntity entity, ClientPatreonEffect effect) {
        super(entity, effect);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickClient() {
        super.tickClient();

        if (this.clientSprite != null) {
            FXFacingSprite sprite = clientSprite;
            if (sprite.isRemoved() && RenderingConfig.CONFIG.patreonEffects.get()) {
                this.clientSprite = null;
            }
        }

        if (this.clientSprite == null) {
            SpriteQuery sprite = getSpriteQuery();
            if (sprite != null) {
                this.clientSprite = Effects.of(EffectTemplatesAS.FACING_SPRITE)
                        .spawn(getPos())
                        .setSprite(sprite.resolveSprite())
                        .setScaleMultiplier(0.35F)
                        .position(new VFXPositionController<EntityVisualFX>() {
                            @Nonnull
                            @Override
                            public Vector3 updatePosition(@Nonnull EntityVisualFX fx, @Nonnull Vector3 position, @Nonnull Vector3 motionToBeMoved) {
                                return PatreonFlareRenderer.this.entity.getPos().clone();
                            }
                        })
                        .refresh(fx -> !PatreonFlareRenderer.this.isRemoved() && RenderingConfig.CONFIG.patreonEffects.get());
            }
        }
    }

    @Nullable
    protected SpriteQuery getSpriteQuery() {
        FlareColor color = getFlareColor();
        if (color != null) {
            return new SpriteQuery(hellfirepvp.astralsorcery.client.resource.AssetLoader.TextureLocation.EFFECT,
                    1, 48, "patreonflares", color.name().toLowerCase(java.util.Locale.ROOT));
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void tickEffects(Level world) {
        super.tickEffects(world);

        if (!RenderingConfig.CONFIG.patreonEffects.get() || rand.nextBoolean()) {
            return;
        }
        Color c = this.getColor();
        if (c == null) {
            return;
        }

        int age = 30 + rand.nextInt(15);
        float scale = 0.1F + rand.nextFloat() * 0.1F;
        Vector3 at = new Vector3(this.entity.getPos());
        at.add(rand.nextFloat() * 0.08 * (rand.nextBoolean() ? 1 : -1),
                rand.nextFloat() * 0.08 * (rand.nextBoolean() ? 1 : -1),
                rand.nextFloat() * 0.08 * (rand.nextBoolean() ? 1 : -1));

        Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(at)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .setScaleMultiplier(scale)
                .color(VFXColorFunction.constant(c))
                .setMaxAge(age);

        if (rand.nextBoolean()) {
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(scale * 0.3F)
                    .setMaxAge(age - 10);
        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    protected Color getColor() {
        FlareColor col = this.getFlareColor();
        if (col == null) {
            return null;
        }
        return rand.nextInt(3) == 0 ? col.color2 : col.color1;
    }

    @Nullable
    private FlareColor getFlareColor() {
        PatreonEffect effect = this.getEffect();
        if (effect != null) {
            return effect.getFlareColor();
        }
        return null;
    }
    @Override
    public void close() {
        super.close();
        if (clientSprite != null) {
            clientSprite.requestRemoval();
            clientSprite = null;
        }
    }
}
