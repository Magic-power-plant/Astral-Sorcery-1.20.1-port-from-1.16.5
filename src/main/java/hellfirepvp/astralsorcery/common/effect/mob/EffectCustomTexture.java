/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.effect.mob;

import hellfirepvp.astralsorcery.client.effect.MobEffectIconRenderer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;
import net.minecraftforge.eventbus.api.IEventBus;

import java.awt.Color;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Base for registered gameplay effects. MobEffect owns category and color; event callbacks apply
 * gameplay behavior. Icon rendering is a client capability, installed only by Forge's client hook.
 */
public abstract class EffectCustomTexture extends MobEffect {

    protected static final Random rand = new Random();

    protected EffectCustomTexture(MobEffectCategory type, Color color) {
        super(type, color.getRGB());
    }

    public void attachEventListeners(IEventBus bus) {}

    /** Name beneath the GUI effect resource directory; contains no client resource object. */
    public abstract String getIconName();

    /** Forge invokes this only on a physical client, outside data generation. */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void initializeClient(Consumer<IClientMobEffectExtensions> consumer) {
        // MobEffect calls this during its constructor: do not read subclass fields here.
        consumer.accept(new MobEffectIconRenderer(this));
    }
}
