package hellfirepvp.astralsorcery.common.fluid;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

/**
 * 1.20.1 counterpart of the 1.16.5 {@code FluidLiquidStarlight} fluid attributes.
 * <p>
 * In 1.16.5 the fluid classes carried the {@code FluidAttributes} (textures, rarity, luminosity, density, viscosity,
 * temperature). Forge 1.20.1 moved those onto a {@link FluidType}; the flowing/source fluid instances themselves are
 * plain {@code ForgeFlowingFluid.Source}/{@code Flowing} created in {@code FluidsAS}.
 */
public class FluidLiquidStarlight extends FluidType {

    public static final ResourceLocation TEXTURE_STILL = AstralSorcery.key("fluid/liquid_starlight_still");
    public static final ResourceLocation TEXTURE_FLOWING = AstralSorcery.key("fluid/liquid_starlight_flowing");

    public FluidLiquidStarlight() {
        super(FluidType.Properties.create()
                .descriptionId("fluid.astralsorcery.liquid_starlight")
                .rarity(Rarity.EPIC)
                .lightLevel(15)
                .density(1001)
                .viscosity(300)
                .temperature(40)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY));
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return TEXTURE_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return TEXTURE_FLOWING;
            }
        });
    }
}
