package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.block.BlockChalice;
import hellfirepvp.astralsorcery.common.block.BlockFountain;
import hellfirepvp.astralsorcery.common.block.foliage.BlockGlowFlower;
import hellfirepvp.astralsorcery.common.block.ore.BlockAquamarineSandOre;
import hellfirepvp.astralsorcery.common.block.ore.BlockRockCrystalOre;
import hellfirepvp.astralsorcery.common.block.ore.BlockStarmetal;
import hellfirepvp.astralsorcery.common.block.ore.BlockStarmetalOre;
import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.block.tile.BlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockCelestialCrystalCluster;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockGemCrystalCluster;
import hellfirepvp.astralsorcery.common.block.tile.BlockCelestialGateway;
import hellfirepvp.astralsorcery.common.block.tile.BlockFlareLight;
import hellfirepvp.astralsorcery.common.block.tile.BlockIlluminator;
import hellfirepvp.astralsorcery.common.block.tile.BlockLens;
import hellfirepvp.astralsorcery.common.block.tile.BlockPrism;
import hellfirepvp.astralsorcery.common.block.tile.BlockRefractionTable;
import hellfirepvp.astralsorcery.common.block.tile.BlockRitualLink;
import hellfirepvp.astralsorcery.common.block.tile.BlockRitualPedestal;
import hellfirepvp.astralsorcery.common.block.tile.BlockStructural;
import hellfirepvp.astralsorcery.common.block.tile.BlockObservatory;
import hellfirepvp.astralsorcery.common.block.tile.BlockTelescope;
import hellfirepvp.astralsorcery.common.block.tile.BlockTranslucentBlock;
import hellfirepvp.astralsorcery.common.block.tile.BlockTreeBeacon;
import hellfirepvp.astralsorcery.common.block.tile.BlockTreeBeaconComponent;
import hellfirepvp.astralsorcery.common.block.tile.BlockVanishing;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockLens;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockPrism;
import hellfirepvp.astralsorcery.common.block.tile.crystal.BlockCelestialCollectorCrystal;
import hellfirepvp.astralsorcery.common.block.tile.crystal.BlockRockCollectorCrystal;
import hellfirepvp.astralsorcery.common.tile.TileCollectorCrystal;
import hellfirepvp.astralsorcery.common.block.BlockInfuser;
//The three pillar/column blocks carry an EnumProperty "pillartype" (top/middle/bottom) that their shipped
//blockstate json indexes on. Registering them as bare Block instances left them with no properties at all, so
//no variant in marble_pillar.json / black_marble_pillar.json / infused_wood_column.json could ever match.
import hellfirepvp.astralsorcery.common.block.marble.BlockMarblePillar;
import hellfirepvp.astralsorcery.common.block.blackmarble.BlockBlackMarblePillar;
import hellfirepvp.astralsorcery.common.block.infusedwood.BlockInfusedWoodColumn;
import hellfirepvp.astralsorcery.common.block.tile.fountain.BlockFountainPrimeLiquid;
import hellfirepvp.astralsorcery.common.block.tile.fountain.BlockFountainPrimeOre;
import hellfirepvp.astralsorcery.common.block.tile.fountain.BlockFountainPrimeVortex;
import hellfirepvp.astralsorcery.common.block.tile.altar.BlockAltarDiscovery;
import hellfirepvp.astralsorcery.common.block.tile.altar.BlockAltarAttunement;
import hellfirepvp.astralsorcery.common.block.tile.altar.BlockAltarConstellation;
import hellfirepvp.astralsorcery.common.block.tile.altar.BlockAltarRadiance;
import hellfirepvp.astralsorcery.common.block.BlockSpectralRelay;
import hellfirepvp.astralsorcery.common.block.BlockWell;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockCelestialCollectorCrystal;
import hellfirepvp.astralsorcery.common.item.block.ItemBlockRockCollectorCrystal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlocksAS {
    // TODO_STUB[AS-BLK-01]: Many newly registered decoration/machine blocks below are resource-load placeholders.
    // Port their 1.16.5 block subclasses, blockstate properties, tile entities, and interaction logic before marking them complete.
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AstralSorcery.MODID);

    //1.16.5 RegistryBlocks.java:98-101 registers the four altar subclasses, not `new BlockAltar(type)`. Each one
    //exists here and supplies that altar's sculpted VoxelShape; without them every altar used the default full
    //cube for selection and collision, so the four tiers were indistinguishable to the cursor and you could not
    //stand inside the carved-out parts.
    public static final RegistryObject<Block> ALTAR_DISCOVERY = registerBlockWithItem("altar_discovery", BlockAltarDiscovery::new);
    public static final RegistryObject<Block> ALTAR_ATTUNEMENT = registerBlockWithItem("altar_attunement", BlockAltarAttunement::new);
    public static final RegistryObject<Block> ALTAR_CONSTELLATION = registerBlockWithItem("altar_constellation", BlockAltarConstellation::new);
    public static final RegistryObject<Block> ALTAR_RADIANCE = registerBlockWithItem("altar_radiance", BlockAltarRadiance::new);
    public static final RegistryObject<Block> ALTAR = ALTAR_DISCOVERY;

    //Neither "marble" nor "black_marble" is a real Astral Sorcery block: 1.16.5 has no BlockMarble or
    //BlockBlackMarble class at all, only the Arch/Bricks/Chiseled/Engraved/Pillar/Raw/Runed variants, and 1.21.1
    //has no such id either. The plain "Marble"/"Sooty Marble" names duplicated marble_raw and black_marble_raw in
    //the creative tab, and marble's model pointed at astralsorcery:block/marble.png, which does not ship.
    //Both stair blocks take their base state from the BRICKS variant, as 1.16.5 does
    //(RegistryBlocks.java: makeStairs(MARBLE_BRICKS...) / makeStairs(BLACK_MARBLE_BRICKS...)).
    public static final RegistryObject<Block> MARBLE_RAW = registerBlockWithItem("marble_raw", BlocksAS::marbleBlock);
    public static final RegistryObject<Block> MARBLE_ARCH = registerBlockWithItem("marble_arch", BlocksAS::marbleBlock);
    public static final RegistryObject<Block> MARBLE_BRICKS = registerBlockWithItem("marble_bricks", BlocksAS::marbleBlock);
    public static final RegistryObject<Block> MARBLE_CHISELED = registerBlockWithItem("marble_chiseled", BlocksAS::marbleBlock);
    public static final RegistryObject<Block> MARBLE_ENGRAVED = registerBlockWithItem("marble_engraved", BlocksAS::marbleBlock);
    public static final RegistryObject<Block> MARBLE_PILLAR = registerBlockWithItem("marble_pillar", BlockMarblePillar::new);
    public static final RegistryObject<Block> MARBLE_RUNED = registerBlockWithItem("marble_runed", BlocksAS::marbleBlock);
    public static final RegistryObject<Block> MARBLE_SLAB = registerBlockWithItem("marble_slab", () -> new SlabBlock(marbleProperties()));
    public static final RegistryObject<Block> MARBLE_STAIRS = registerBlockWithItem("marble_stairs", () -> new StairBlock(MARBLE_BRICKS.get().defaultBlockState(), marbleProperties()));

    public static final RegistryObject<Block> BLACK_MARBLE_RAW = registerBlockWithItem("black_marble_raw", BlocksAS::blackMarbleBlock);
    public static final RegistryObject<Block> BLACK_MARBLE_ARCH = registerBlockWithItem("black_marble_arch", BlocksAS::blackMarbleBlock);
    public static final RegistryObject<Block> BLACK_MARBLE_BRICKS = registerBlockWithItem("black_marble_bricks", BlocksAS::blackMarbleBlock);
    public static final RegistryObject<Block> BLACK_MARBLE_CHISELED = registerBlockWithItem("black_marble_chiseled", BlocksAS::blackMarbleBlock);
    public static final RegistryObject<Block> BLACK_MARBLE_ENGRAVED = registerBlockWithItem("black_marble_engraved", BlocksAS::blackMarbleBlock);
    public static final RegistryObject<Block> BLACK_MARBLE_PILLAR = registerBlockWithItem("black_marble_pillar", BlockBlackMarblePillar::new);
    public static final RegistryObject<Block> BLACK_MARBLE_RUNED = registerBlockWithItem("black_marble_runed", BlocksAS::blackMarbleBlock);
    public static final RegistryObject<Block> BLACK_MARBLE_SLAB = registerBlockWithItem("black_marble_slab", () -> new SlabBlock(blackMarbleProperties()));
    public static final RegistryObject<Block> BLACK_MARBLE_STAIRS = registerBlockWithItem("black_marble_stairs", () -> new StairBlock(BLACK_MARBLE_BRICKS.get().defaultBlockState(), blackMarbleProperties()));

    public static final RegistryObject<Block> INFUSED_WOOD = registerBlockWithItem("infused_wood", BlocksAS::infusedWoodBlock);
    public static final RegistryObject<Block> INFUSED_WOOD_ARCH = registerBlockWithItem("infused_wood_arch", BlocksAS::infusedWoodBlock);
    public static final RegistryObject<Block> INFUSED_WOOD_COLUMN = registerBlockWithItem("infused_wood_column", BlockInfusedWoodColumn::new);
    public static final RegistryObject<Block> INFUSED_WOOD_ENGRAVED = registerBlockWithItem("infused_wood_engraved", BlocksAS::infusedWoodBlock);
    public static final RegistryObject<Block> INFUSED_WOOD_ENRICHED = registerBlockWithItem("infused_wood_enriched", BlocksAS::infusedWoodBlock);
    public static final RegistryObject<Block> INFUSED_WOOD_INFUSED = registerBlockWithItem("infused_wood_infused", BlocksAS::infusedWoodBlock);
    public static final RegistryObject<Block> INFUSED_WOOD_PLANKS = registerBlockWithItem("infused_wood_planks", BlocksAS::infusedWoodBlock);
    public static final RegistryObject<Block> INFUSED_WOOD_SLAB = registerBlockWithItem("infused_wood_slab", () -> new SlabBlock(infusedWoodProperties()));
    public static final RegistryObject<Block> INFUSED_WOOD_STAIRS = registerBlockWithItem("infused_wood_stairs", () -> new StairBlock(INFUSED_WOOD.get().defaultBlockState(), infusedWoodProperties()));

    public static final RegistryObject<Block> STARMETAL_ORE = registerBlockWithItem("starmetal_ore", BlockStarmetalOre::new);
    public static final RegistryObject<Block> AQUAMARINE_SAND_ORE = registerBlockWithItem("aquamarine_sand_ore", BlockAquamarineSandOre::new);
    public static final RegistryObject<Block> ROCK_CRYSTAL_ORE = registerBlockWithItem("rock_crystal_ore", BlockRockCrystalOre::new);
    //"starmetal" is the CANONICAL id, not "starmetal_block": 1.16.5 registers STARMETAL = new BlockStarmetal()
    //through registerBlock(T), which derives the id with NameUtil.fromClass(block, "Block") - simple name
    //"BlockStarmetal", prefix "Block" stripped, UPPER_CAMEL -> LOWER_UNDERSCORE = "starmetal". An earlier port
    //stage registered the real class under the invented "starmetal_block" and put a bare placeholder Block on
    //"starmetal". Since recipes/shaped/starmetal.json, recipes/shapeless/starmetal_ingot.json and
    //minecraft:beacon_base_blocks all reference "astralsorcery:starmetal", every one of them was operating on the
    //placeholder: strength(5.0, 10.0) instead of 1.5/6.0, stone sounds instead of SoundType.METAL, and - because
    //only "starmetal_block" was in minecraft:mineable/pickaxe - no pickaxe tag at all, so DiggerItem
    //#getDestroySpeed returned 1.0F and a diamond pickaxe needed ~150 ticks rather than ~6.
    public static final RegistryObject<Block> STARMETAL = registerBlockWithItem("starmetal", BlockStarmetal::new);

    public static final RegistryObject<Block> INFUSER = registerBlockWithItem("infuser", BlockInfuser::new);
    public static final RegistryObject<Block> WELL = registerBlockWithItem("well", BlockWell::new);
    public static final RegistryObject<Block> FOUNTAIN = registerBlockWithItem("fountain", BlockFountain::new);
    //An earlier port stage also registered a bare "collector_crystal" here. It exists in neither reference tree:
    //1.16.5 declares BlockCollectorCrystal abstract and registers only the two subclasses below, and 1.21.1 has no
    //such id either. It was registered with the two-arg registerBlockWithItem, so its item was a plain BlockItem;
    //BlockCollectorCrystal#setPlacedBy therefore skipped updateData, TileCollectorCrystal#isPlayerMade() stayed
    //false, getDestroyProgress fell through to super, and with strength(-1F, 3600000.0F) vanilla returns 0.0F
    //destroy progress - a bedrock-grade, blast-proof, missing-texture block that permanently occupied its position
    //and collected nothing, craftable in survival from eight rock crystals via a shipped JEI-visible recipe.
    public static final RegistryObject<Block> ROCK_COLLECTOR_CRYSTAL = registerBlockWithItem("rock_collector_crystal", BlockRockCollectorCrystal::new,
            b -> new ItemBlockRockCollectorCrystal(b, new Item.Properties()));
    public static final RegistryObject<Block> CELESTIAL_COLLECTOR_CRYSTAL = registerBlockWithItem("celestial_collector_crystal", BlockCelestialCollectorCrystal::new,
            b -> new ItemBlockCelestialCollectorCrystal(b, new Item.Properties()));

    public static final RegistryObject<Block> ATTUNEMENT_ALTAR = registerBlockWithItem("attunement_altar", hellfirepvp.astralsorcery.common.block.tile.BlockAttunementAltar::new);
    public static final RegistryObject<Block> CELESTIAL_GATEWAY = registerBlockWithItem("celestial_gateway", BlockCelestialGateway::new);
    public static final RegistryObject<Block> CHALICE = registerBlockWithItem("chalice", BlockChalice::new);
    //TileFountain#... does `primeState.getBlock() instanceof BlockFountainPrime` and then calls provideEffect()
    //(TileFountain.java:200-201). Registered as a bare Block that instanceof was always false, so a fountain
    //never picked up an effect from the prime beneath it. The real classes also carry canSurvive/updateShape, which
    //are what make a prime break when its fountain is removed.
    public static final RegistryObject<Block> FOUNTAIN_PRIME_LIQUID = registerBlockWithItem("fountain_prime_liquid", BlockFountainPrimeLiquid::new);
    public static final RegistryObject<Block> FOUNTAIN_PRIME_ORE = registerBlockWithItem("fountain_prime_ore", BlockFountainPrimeOre::new);
    public static final RegistryObject<Block> FOUNTAIN_PRIME_VORTEX = registerBlockWithItem("fountain_prime_vortex", BlockFountainPrimeVortex::new);
    public static final RegistryObject<Block> ILLUMINATOR = registerBlockWithItem("illuminator", BlockIlluminator::new);
    public static final RegistryObject<Block> LENS = registerBlockWithItem("lens", BlockLens::new, b -> new ItemBlockLens(b, new Item.Properties()));
    public static final RegistryObject<Block> OBSERVATORY = registerBlockWithItem("observatory", BlockObservatory::new);
    public static final RegistryObject<Block> PRISM = registerBlockWithItem("prism", BlockPrism::new, b -> new ItemBlockPrism(b, new Item.Properties()));
    public static final RegistryObject<Block> REFRACTION_TABLE = registerBlockWithItem("refraction_table", BlockRefractionTable::new);
    public static final RegistryObject<Block> RITUAL_LINK = registerBlockWithItem("ritual_link", BlockRitualLink::new);
    public static final RegistryObject<Block> RITUAL_PEDESTAL = registerBlockWithItem("ritual_pedestal", BlockRitualPedestal::new);
    public static final RegistryObject<Block> SPECTRAL_RELAY = registerBlockWithItem("spectral_relay", BlockSpectralRelay::new);
    public static final RegistryObject<Block> TELESCOPE = registerBlockWithItem("telescope", BlockTelescope::new);
    public static final RegistryObject<Block> TREE_BEACON = registerBlockWithItem("tree_beacon", BlockTreeBeacon::new);

    public static final RegistryObject<Block> CELESTIAL_CRYSTAL_CLUSTER = registerBlockWithItem("celestial_crystal_cluster", BlockCelestialCrystalCluster::new, b -> new ItemBlockCelestialCrystalCluster(b, new Item.Properties()));
    public static final RegistryObject<Block> GEM_CRYSTAL_CLUSTER = registerBlockWithItem("gem_crystal_cluster", BlockGemCrystalCluster::new, b -> new ItemBlockGemCrystalCluster(b, new Item.Properties()));
    public static final RegistryObject<Block> GLOW_FLOWER = registerBlockWithItem("glow_flower", BlockGlowFlower::new);

    public static final RegistryObject<Block> FLARE_LIGHT = registerBlockOnly("flare_light", BlockFlareLight::new);
    public static final RegistryObject<Block> STRUCTURAL = registerBlockOnly("structural", BlockStructural::new);
    public static final RegistryObject<Block> TRANSLUCENT_BLOCK = registerBlockOnly("translucent_block", BlockTranslucentBlock::new);
    public static final RegistryObject<Block> TREE_BEACON_COMPONENT = registerBlockOnly("tree_beacon_component", BlockTreeBeaconComponent::new);
    public static final RegistryObject<Block> VANISHING = registerBlockOnly("vanishing", BlockVanishing::new);

    private static Block marbleBlock() {
        return new Block(marbleProperties());
    }

    private static BlockBehaviour.Properties marbleProperties() {
        return BlockBehaviour.Properties.of().strength(2.0f, 6.0f);
    }

    private static Block blackMarbleBlock() {
        return new Block(blackMarbleProperties());
    }

    private static BlockBehaviour.Properties blackMarbleProperties() {
        return BlockBehaviour.Properties.of().strength(2.0f, 6.0f);
    }

    private static Block infusedWoodBlock() {
        return new Block(infusedWoodProperties());
    }

    private static BlockBehaviour.Properties infusedWoodProperties() {
        return BlockBehaviour.Properties.of().strength(2.0f, 5.0f);
    }

    //machineBlock() is gone: the three fountain primes were its only callers and now construct their real classes.
    //fragileBlock() below already had no caller before this change and is left alone rather than folded into it.
    private static Block fragileBlock() {
        return new Block(BlockBehaviour.Properties.of().strength(0.3f, 0.3f).lightLevel(state -> 8));
    }

    private static RegistryObject<Block> registerBlockOnly(String name, Supplier<Block> blockSupplier) {
        return BLOCKS.register(name, blockSupplier);
    }

    private static RegistryObject<Block> registerBlockWithItem(String name, Supplier<Block> blockSupplier) {
        return registerBlockWithItem(name, blockSupplier, b -> new BlockItem(b, new Item.Properties()));
    }

    private static RegistryObject<Block> registerBlockWithItem(String name, Supplier<Block> blockSupplier, java.util.function.Function<Block, Item> itemFactory) {
        RegistryObject<Block> block = BLOCKS.register(name, blockSupplier);
        ItemsAS.ITEMS.register(name, () -> itemFactory.apply(block.get()));
        return block;
    }
}
