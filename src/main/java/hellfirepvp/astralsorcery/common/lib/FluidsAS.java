package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.fluid.BlockLiquidStarlight;
import hellfirepvp.astralsorcery.common.fluid.FluidLiquidStarlight;
import hellfirepvp.astralsorcery.common.fluid.ItemLiquidStarlightBucket;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FluidsAS {

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, AstralSorcery.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, AstralSorcery.MODID);

    public static final RegistryObject<FluidType> LIQUID_STARLIGHT_TYPE = FLUID_TYPES.register("liquid_starlight", FluidLiquidStarlight::new);

    public static final RegistryObject<FlowingFluid> LIQUID_STARLIGHT_SOURCE = FLUIDS.register("liquid_starlight", () -> new ForgeFlowingFluid.Source(liquidStarlightProperties()));
    public static final RegistryObject<FlowingFluid> LIQUID_STARLIGHT_FLOWING = FLUIDS.register("flowing_liquid_starlight", () -> new ForgeFlowingFluid.Flowing(liquidStarlightProperties()));

    public static final RegistryObject<BlockLiquidStarlight> LIQUID_STARLIGHT_BLOCK = BlocksAS.BLOCKS.register("liquid_starlight", () -> new BlockLiquidStarlight(LIQUID_STARLIGHT_SOURCE));

    public static final RegistryObject<Item> LIQUID_STARLIGHT_BUCKET = ItemsAS.ITEMS.register("liquid_starlight_bucket", () -> new ItemLiquidStarlightBucket(LIQUID_STARLIGHT_SOURCE));

    private static ForgeFlowingFluid.Properties liquidStarlightProperties() {
        return new ForgeFlowingFluid.Properties(LIQUID_STARLIGHT_TYPE, LIQUID_STARLIGHT_SOURCE, LIQUID_STARLIGHT_FLOWING)
                .block(LIQUID_STARLIGHT_BLOCK)
                .bucket(LIQUID_STARLIGHT_BUCKET)
                .slopeFindDistance(2)
                .levelDecreasePerBlock(2)
                .tickRate(5);
    }
}
