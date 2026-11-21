/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.effect.source;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.effect.vfx.FXLightning;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.TexturesAS;
import hellfirepvp.astralsorcery.client.resource.AbstractRenderableTexture;
import hellfirepvp.astralsorcery.api.effect.Effects;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.awt.*;
import java.util.List;
import java.util.Random;
import hellfirepvp.astralsorcery.common.util.MiscUtils;

public final class CelestialStrikeEffect {
    @OnlyIn(Dist.CLIENT)
    public static void playEffect(PktPlayEffect effect) {
        Random r = new Random();
        Vector3 vec = ByteBufUtils.readVector(effect.getExtraData());
        Vector3 effectPos = vec.clone();

        Effects.of(EffectTemplatesAS.LIGHTBEAM)
                .spawn(effectPos.clone().addY(-4))
                .setup(effectPos.clone().addY(16), 9, 6)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.WHITE)
                .setAlphaMultiplier(1F)
                .setMaxAge(25);

        effectPos.add(r.nextFloat() - r.nextFloat(), 0, r.nextFloat() - r.nextFloat());
        Effects.of(EffectTemplatesAS.LIGHTBEAM)
                .spawn(effectPos.clone().addY(-4))
                .setup(effectPos.clone().addY(16).addY(r.nextFloat() * 2F), 9, 6)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_LIGHT))
                .setAlphaMultiplier(1F)
                .setMaxAge(24 + r.nextInt(6));

        effectPos.add(r.nextFloat() - r.nextFloat(), 0, r.nextFloat() - r.nextFloat());
        Effects.of(EffectTemplatesAS.LIGHTBEAM)
                .spawn(effectPos.clone().addY(-4))
                .setup(effectPos.clone().addY(16).addY(r.nextFloat() * 2F), 9, 6)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_DARK))
                .setAlphaMultiplier(1F)
                .setMaxAge(24 + r.nextInt(6));

        AbstractRenderableTexture tex = MiscUtils.eitherOf(r,
                TexturesAS.TEX_SMOKE_1, TexturesAS.TEX_SMOKE_2, TexturesAS.TEX_SMOKE_3, TexturesAS.TEX_SMOKE_4);
        Effects.of(EffectTemplatesAS.TEXTURE_SPRITE)
                .spawn(vec.clone().addY(0.1F))
                .setAxis(Vector3.RotAxis.Y_AXIS.clone().negate())
                .setSprite(tex)
                .setNoRotation(r.nextFloat() * 360F)
                .setAlphaMultiplier(0.4F)
                .alpha(VFXAlphaFunction.FADE_OUT)
                .setScaleMultiplier(17F)
                .setMaxAge(30 + r.nextInt(10));

        for (int i = 0; i < 43; i++) {
            Vector3 randTo = new Vector3((r.nextDouble() * 9) - (r.nextDouble() * 9), r.nextDouble() * 5, (r.nextDouble() * 9) - (r.nextDouble() * 9));
            randTo.add(vec.clone());
            FXLightning lightning = Effects.of(EffectTemplatesAS.LIGHTNING)
                    .spawn(vec.clone())
                    .makeDefault(randTo);
            lightning.color(MiscUtils.eitherOf(r,
                    VFXColorFunction.constant(Color.WHITE),
                    VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_LIGHT),
                    VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_DARK)));
        }

        for (int i = 0; i < 40; i++) {
            FXFacingParticle p = Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(vec.clone().add((r.nextFloat() - r.nextFloat()) * 4, r.nextFloat() * 9, (r.nextFloat() - r.nextFloat()) * 4))
                    .setGravityStrength(-0.005F)
                    .setScaleMultiplier(0.85F)
                    .setMaxAge(14 + r.nextInt(6));
            p.color(MiscUtils.eitherOf(r,
                    VFXColorFunction.constant(Color.WHITE),
                    VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_LIGHT),
                    VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_DARK)));
        }

        List<Vector3> circle = MiscUtils.getCirclePositions(vec, Vector3.RotAxis.Y_AXIS, 7.5F + r.nextFloat(), 200 + r.nextInt(40));
        for (Vector3 at : circle) {
            Vector3 dir = at.clone().subtract(vec).normalize().multiply(0.3 + 0.4 * r.nextFloat());
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .setAlphaMultiplier(0.4F)
                    .setMotion(dir)
                    .color(VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_LIGHT))
                    .setScaleMultiplier(1.2F)
                    .setMaxAge(14 + r.nextInt(6));
        }
        circle = MiscUtils.getCirclePositions(vec, Vector3.RotAxis.Y_AXIS, 7.5F + r.nextFloat(), 100 + r.nextInt(40));
        for (Vector3 at : circle) {
            Vector3 dir = at.clone().subtract(vec).normalize().multiply(0.2 + 0.1 * r.nextFloat());
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at)
                    .setAlphaMultiplier(0.4F)
                    .setMotion(dir)
                    .color(VFXColorFunction.constant(ColorsAS.EFFECT_BLUE_DARK))
                    .setScaleMultiplier(1.5F)
                    .setMaxAge(14 + r.nextInt(6));
        }
    }
}
