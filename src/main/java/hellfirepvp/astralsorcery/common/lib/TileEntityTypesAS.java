package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.tile.TileWell;
import hellfirepvp.astralsorcery.common.tile.TileFountain;
import hellfirepvp.astralsorcery.common.tile.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.TileInfuser;
import hellfirepvp.astralsorcery.common.tile.TileChalice;
import hellfirepvp.astralsorcery.common.tile.TileSpectralRelay;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import hellfirepvp.astralsorcery.common.tile.TileCelestialCrystals;
import hellfirepvp.astralsorcery.common.tile.TileCelestialGateway;
import hellfirepvp.astralsorcery.common.tile.TileGemCrystals;
import hellfirepvp.astralsorcery.common.tile.TileIlluminator;
import hellfirepvp.astralsorcery.common.tile.TileLens;
import hellfirepvp.astralsorcery.common.tile.TileObservatory;
import hellfirepvp.astralsorcery.common.tile.TilePrism;
import hellfirepvp.astralsorcery.common.tile.TileRefractionTable;
import hellfirepvp.astralsorcery.common.tile.TileRitualLink;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.tile.TileTelescope;
import hellfirepvp.astralsorcery.common.tile.TileTranslucentBlock;
import hellfirepvp.astralsorcery.common.tile.TileTreeBeacon;
import hellfirepvp.astralsorcery.common.tile.TileTreeBeaconComponent;
import hellfirepvp.astralsorcery.common.tile.TileVanishing;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TileEntityTypesAS {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = 
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<BlockEntityType<?>> ALTAR = TILE_ENTITIES.register("altar",
        () -> BlockEntityType.Builder.of(TileAltar::new,
            BlocksAS.ALTAR_DISCOVERY.get(),
            BlocksAS.ALTAR_ATTUNEMENT.get(),
            BlocksAS.ALTAR_CONSTELLATION.get(),
            BlocksAS.ALTAR_RADIANCE.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<?>> WELL = TILE_ENTITIES.register("well",
        () -> BlockEntityType.Builder.of(TileWell::new, BlocksAS.WELL.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<?>> FOUNTAIN = TILE_ENTITIES.register("fountain",
        () -> BlockEntityType.Builder.of(TileFountain::new, BlocksAS.FOUNTAIN.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<?>> COLLECTOR_CRYSTAL = TILE_ENTITIES.register("collector_crystal",
        () -> BlockEntityType.Builder.of(TileCollectorCrystal::new,
            BlocksAS.ROCK_COLLECTOR_CRYSTAL.get(),
            BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<?>> INFUSER = TILE_ENTITIES.register("infuser",
        () -> BlockEntityType.Builder.of(TileInfuser::new, BlocksAS.INFUSER.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileChalice>> CHALICE = TILE_ENTITIES.register("chalice",
        () -> BlockEntityType.Builder.of(TileChalice::new, BlocksAS.CHALICE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TileSpectralRelay>> SPECTRAL_RELAY = TILE_ENTITIES.register("spectral_relay",
        () -> BlockEntityType.Builder.of(TileSpectralRelay::new, BlocksAS.SPECTRAL_RELAY.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> ATTUNEMENT_ALTAR = TILE_ENTITIES.register("attunement_altar",
        () -> BlockEntityType.Builder.of(TileAttunementAltar::new, BlocksAS.ATTUNEMENT_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> CELESTIAL_CRYSTAL_CLUSTER = TILE_ENTITIES.register("celestial_crystal_cluster",
        () -> BlockEntityType.Builder.of(TileCelestialCrystals::new, BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> CELESTIAL_GATEWAY = TILE_ENTITIES.register("celestial_gateway",
        () -> BlockEntityType.Builder.of(TileCelestialGateway::new, BlocksAS.CELESTIAL_GATEWAY.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> GEM_CRYSTAL_CLUSTER = TILE_ENTITIES.register("gem_crystal_cluster",
        () -> BlockEntityType.Builder.of(TileGemCrystals::new, BlocksAS.GEM_CRYSTAL_CLUSTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> ILLUMINATOR = TILE_ENTITIES.register("illuminator",
        () -> BlockEntityType.Builder.of(TileIlluminator::new, BlocksAS.ILLUMINATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> LENS = TILE_ENTITIES.register("lens",
        () -> BlockEntityType.Builder.of(TileLens::new, BlocksAS.LENS.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> OBSERVATORY = TILE_ENTITIES.register("observatory",
        () -> BlockEntityType.Builder.of(TileObservatory::new, BlocksAS.OBSERVATORY.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> PRISM = TILE_ENTITIES.register("prism",
        () -> BlockEntityType.Builder.of(TilePrism::new, BlocksAS.PRISM.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> REFRACTION_TABLE = TILE_ENTITIES.register("refraction_table",
        () -> BlockEntityType.Builder.of(TileRefractionTable::new, BlocksAS.REFRACTION_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> RITUAL_LINK = TILE_ENTITIES.register("ritual_link",
        () -> BlockEntityType.Builder.of(TileRitualLink::new, BlocksAS.RITUAL_LINK.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> RITUAL_PEDESTAL = TILE_ENTITIES.register("ritual_pedestal",
        () -> BlockEntityType.Builder.of(TileRitualPedestal::new, BlocksAS.RITUAL_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> TELESCOPE = TILE_ENTITIES.register("telescope",
        () -> BlockEntityType.Builder.of(TileTelescope::new, BlocksAS.TELESCOPE.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> TRANSLUCENT_BLOCK = TILE_ENTITIES.register("translucent_block",
        () -> BlockEntityType.Builder.of(TileTranslucentBlock::new, BlocksAS.TRANSLUCENT_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> TREE_BEACON = TILE_ENTITIES.register("tree_beacon",
        () -> BlockEntityType.Builder.of(TileTreeBeacon::new, BlocksAS.TREE_BEACON.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> TREE_BEACON_COMPONENT = TILE_ENTITIES.register("tree_beacon_component",
        () -> BlockEntityType.Builder.of(TileTreeBeaconComponent::new, BlocksAS.TREE_BEACON_COMPONENT.get()).build(null));

    public static final RegistryObject<BlockEntityType<?>> VANISHING = TILE_ENTITIES.register("vanishing",
        () -> BlockEntityType.Builder.of(TileVanishing::new, BlocksAS.VANISHING.get()).build(null));
}