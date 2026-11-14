package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.WellBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.FountainBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.CollectorCrystalBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.InfuserBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.ChaliceBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.SpectralRelayBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.AttunementAltarBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.CelestialCrystalsBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.CelestialGatewayBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.GemCrystalsBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.IlluminatorBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.LensBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.ObservatoryBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.PrismBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.RefractionTableBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.RitualLinkBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.RitualPedestalBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.TelescopeBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.TranslucentBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.TreeBeaconBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.TreeBeaconComponentBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.VanishingBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityTypesAS {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AstralSorcery.MODID);

    public static final RegistryObject<BlockEntityType<AltarBlockEntity>> ALTAR = BLOCK_ENTITY_TYPES.register("altar",
        () -> BlockEntityType.Builder.of(AltarBlockEntity::new,
            BlocksAS.ALTAR_DISCOVERY.get(),
            BlocksAS.ALTAR_ATTUNEMENT.get(),
            BlocksAS.ALTAR_CONSTELLATION.get(),
            BlocksAS.ALTAR_RADIANCE.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<WellBlockEntity>> WELL = BLOCK_ENTITY_TYPES.register("well",
        () -> BlockEntityType.Builder.of(WellBlockEntity::new, BlocksAS.WELL.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<FountainBlockEntity>> FOUNTAIN = BLOCK_ENTITY_TYPES.register("fountain",
        () -> BlockEntityType.Builder.of(FountainBlockEntity::new, BlocksAS.FOUNTAIN.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<CollectorCrystalBlockEntity>> COLLECTOR_CRYSTAL = BLOCK_ENTITY_TYPES.register("collector_crystal",
        () -> BlockEntityType.Builder.of(CollectorCrystalBlockEntity::new,
            BlocksAS.ROCK_COLLECTOR_CRYSTAL.get(),
            BlocksAS.CELESTIAL_COLLECTOR_CRYSTAL.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<InfuserBlockEntity>> INFUSER = BLOCK_ENTITY_TYPES.register("infuser",
        () -> BlockEntityType.Builder.of(InfuserBlockEntity::new, BlocksAS.INFUSER.get()).build(null));

    public static final RegistryObject<BlockEntityType<ChaliceBlockEntity>> CHALICE = BLOCK_ENTITY_TYPES.register("chalice",
        () -> BlockEntityType.Builder.of(ChaliceBlockEntity::new, BlocksAS.CHALICE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SpectralRelayBlockEntity>> SPECTRAL_RELAY = BLOCK_ENTITY_TYPES.register("spectral_relay",
        () -> BlockEntityType.Builder.of(SpectralRelayBlockEntity::new, BlocksAS.SPECTRAL_RELAY.get()).build(null));

    public static final RegistryObject<BlockEntityType<AttunementAltarBlockEntity>> ATTUNEMENT_ALTAR = BLOCK_ENTITY_TYPES.register("attunement_altar",
        () -> BlockEntityType.Builder.of(AttunementAltarBlockEntity::new, BlocksAS.ATTUNEMENT_ALTAR.get()).build(null));

    public static final RegistryObject<BlockEntityType<CelestialCrystalsBlockEntity>> CELESTIAL_CRYSTAL_CLUSTER = BLOCK_ENTITY_TYPES.register("celestial_crystal_cluster",
        () -> BlockEntityType.Builder.of(CelestialCrystalsBlockEntity::new, BlocksAS.CELESTIAL_CRYSTAL_CLUSTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<CelestialGatewayBlockEntity>> CELESTIAL_GATEWAY = BLOCK_ENTITY_TYPES.register("celestial_gateway",
        () -> BlockEntityType.Builder.of(CelestialGatewayBlockEntity::new, BlocksAS.CELESTIAL_GATEWAY.get()).build(null));

    public static final RegistryObject<BlockEntityType<GemCrystalsBlockEntity>> GEM_CRYSTAL_CLUSTER = BLOCK_ENTITY_TYPES.register("gem_crystal_cluster",
        () -> BlockEntityType.Builder.of(GemCrystalsBlockEntity::new, BlocksAS.GEM_CRYSTAL_CLUSTER.get()).build(null));

    public static final RegistryObject<BlockEntityType<IlluminatorBlockEntity>> ILLUMINATOR = BLOCK_ENTITY_TYPES.register("illuminator",
        () -> BlockEntityType.Builder.of(IlluminatorBlockEntity::new, BlocksAS.ILLUMINATOR.get()).build(null));

    public static final RegistryObject<BlockEntityType<LensBlockEntity>> LENS = BLOCK_ENTITY_TYPES.register("lens",
        () -> BlockEntityType.Builder.of(LensBlockEntity::new, BlocksAS.LENS.get()).build(null));

    public static final RegistryObject<BlockEntityType<ObservatoryBlockEntity>> OBSERVATORY = BLOCK_ENTITY_TYPES.register("observatory",
        () -> BlockEntityType.Builder.of(ObservatoryBlockEntity::new, BlocksAS.OBSERVATORY.get()).build(null));

    public static final RegistryObject<BlockEntityType<PrismBlockEntity>> PRISM = BLOCK_ENTITY_TYPES.register("prism",
        () -> BlockEntityType.Builder.of(PrismBlockEntity::new, BlocksAS.PRISM.get()).build(null));

    public static final RegistryObject<BlockEntityType<RefractionTableBlockEntity>> REFRACTION_TABLE = BLOCK_ENTITY_TYPES.register("refraction_table",
        () -> BlockEntityType.Builder.of(RefractionTableBlockEntity::new, BlocksAS.REFRACTION_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<RitualLinkBlockEntity>> RITUAL_LINK = BLOCK_ENTITY_TYPES.register("ritual_link",
        () -> BlockEntityType.Builder.of(RitualLinkBlockEntity::new, BlocksAS.RITUAL_LINK.get()).build(null));

    public static final RegistryObject<BlockEntityType<RitualPedestalBlockEntity>> RITUAL_PEDESTAL = BLOCK_ENTITY_TYPES.register("ritual_pedestal",
        () -> BlockEntityType.Builder.of(RitualPedestalBlockEntity::new, BlocksAS.RITUAL_PEDESTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<TelescopeBlockEntity>> TELESCOPE = BLOCK_ENTITY_TYPES.register("telescope",
        () -> BlockEntityType.Builder.of(TelescopeBlockEntity::new, BlocksAS.TELESCOPE.get()).build(null));

    public static final RegistryObject<BlockEntityType<TranslucentBlockEntity>> TRANSLUCENT_BLOCK = BLOCK_ENTITY_TYPES.register("translucent_block",
        () -> BlockEntityType.Builder.of(TranslucentBlockEntity::new, BlocksAS.TRANSLUCENT_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<TreeBeaconBlockEntity>> TREE_BEACON = BLOCK_ENTITY_TYPES.register("tree_beacon",
        () -> BlockEntityType.Builder.of(TreeBeaconBlockEntity::new, BlocksAS.TREE_BEACON.get()).build(null));

    public static final RegistryObject<BlockEntityType<TreeBeaconComponentBlockEntity>> TREE_BEACON_COMPONENT = BLOCK_ENTITY_TYPES.register("tree_beacon_component",
        () -> BlockEntityType.Builder.of(TreeBeaconComponentBlockEntity::new, BlocksAS.TREE_BEACON_COMPONENT.get()).build(null));

    public static final RegistryObject<BlockEntityType<VanishingBlockEntity>> VANISHING = BLOCK_ENTITY_TYPES.register("vanishing",
        () -> BlockEntityType.Builder.of(VanishingBlockEntity::new, BlocksAS.VANISHING.get()).build(null));
}