/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.blockentity.altar;

import hellfirepvp.astralsorcery.client.util.sound.ClientSoundHelper;

import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.crafting.ActiveAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.AltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.item.base.IConstellationFocus;
import hellfirepvp.astralsorcery.common.item.wand.WandInteractable;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.blockentity.starlight.SpectralRelayBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.base.network.ReceiverBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.network.StarlightReceiverAltar;
import hellfirepvp.astralsorcery.common.item.util.ItemStackHandlerAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.block.util.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarBlockEntity
 * Created by HellFirePvP
 * Date: 12.08.2019 / 19:53
 */
public class AltarBlockEntity extends ReceiverBlockEntity<StarlightReceiverAltar> implements WandInteractable {

    private float posDistribution = -1;

    private AltarType altarType = AltarType.DISCOVERY;

    //TODO_PORT[AS-ALT-11]: 1.16.5 holds a non-final 'TileInventoryFiltered inventory' built as
    //new TileInventoryFiltered(this, () -> 25) and re-assigned from NBT by inventory.deserialize(..)
    //(1.16.5 TileAltar.java:78/93/483). This tree carries ItemStackHandlerAS instead, and the out-of-lane
    //common/crafting/recipe/altar/ActiveSimpleAltarRecipe.java:153 declares 'ItemStackHandlerAS inv =
    //altar.getInventory();', which pins the return type of getInventory() here. Widening that call site is
    //reported as an external change; until then the filtered-inventory semantics (slot filter delegating to
    //AltarType#hasSlot) are only enforced by ActiveAltarRecipe#consumeInputs via TileAltar#hasSlot below.
    private final ItemStackHandlerAS inventory = new ItemStackHandlerAS(25);

    private final Map<AltarCollectionCategory, Float> tickStarlightCollectionMap = new HashMap<>();

    //TODO_PORT[AS-ALT-12]: 1.16.5 types this as ActiveSimpleAltarRecipe/SimpleAltarRecipe
    //(1.16.5 TileAltar.java:82). Both class pairs exist in this tree - the legacy
    //common.crafting.ActiveAltarRecipe/AltarRecipe used here, and common.crafting.recipe.altar.
    //ActiveSimpleAltarRecipe/common.crafting.recipe.SimpleAltarRecipe. Switching the field would change the
    //return type of getActiveRecipe(), which ~12 files outside this slice are typed against (RenderAltar,
    //ResearchManager#informCraftedAltar, AltarCraftingProgress, the three builtin *UpgradeRecipe classes and
    //six of the AltarRecipeEffect implementations). Collapsing the two hierarchies is tracked globally as
    //VERIFY_PORT[AS-ALT-01] and reported as an external change.
    private ActiveAltarRecipe activeRecipe = null;
    private ItemStack focusItem = ItemStack.EMPTY;
    private Set<ResourceLocation> knownRecipes = new HashSet<>();
    private final DeferredStarlightStorage starlightStorage = new DeferredStarlightStorage(2);
    private int starlightNextTick = 0;

    //Kept as Object exactly as 1.16.5 does (TileAltar.java:88-89): the fields are only ever assigned on the
    //client, so typing them as the client-only sound classes would load those classes on a dedicated server.
    private Object clientCraftSound = null;
    private Object clientWaitSound = null;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.ALTAR.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level.isClientSide()) {
            this.doesSeeSky();
            this.hasMultiblock();

            this.gatherStarlight();
            this.doCraftingCycle();
        } else {
            if (this.getActiveRecipe() != null) {
                this.doCraftEffects();
                this.doCraftSound();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doCraftEffects() {
        //1.16.5: activeRecipe.getRecipeToCraft() -> this tree's ActiveAltarRecipe#getRecipe() is the same accessor.
        //AltarRecipeEffect#onTick is (still) declared against ActiveSimpleAltarRecipe.CraftingState, so the state
        //is bridged by name the same way client/render/tile/RenderAltar.java:80 does it. Both enums carry the
        //identical constants; see VERIFY_PORT[AS-ALT-01].
        ActiveSimpleAltarRecipe.CraftingState state = ActiveSimpleAltarRecipe.CraftingState.valueOf(this.activeRecipe.getState().name());
        this.activeRecipe.getRecipe().getCraftingEffects()
                .forEach(effect -> effect.onTick(this, state));
    }

    @OnlyIn(Dist.CLIENT)
    private void doCraftSound() {
        //1.16.5: SoundCategory.BLOCKS -> 1.20.1: SoundSource.BLOCKS
        if (ClientSoundHelper.getSoundVolume(SoundSource.BLOCKS) > 0) {
            ActiveAltarRecipe activeRecipe = this.getActiveRecipe();
            AltarType type = this.getAltarType();

            if (clientCraftSound == null || ((PositionedLoopSound) clientCraftSound).hasStoppedPlaying()) {
                CategorizedSoundEvent sound = SoundsAS.ALTAR_CRAFT_LOOP_T1;
                switch (type) {
                    case ATTUNEMENT:
                        sound = SoundsAS.ALTAR_CRAFT_LOOP_T2;
                        break;
                    case CONSTELLATION:
                        sound = SoundsAS.ALTAR_CRAFT_LOOP_T3;
                        break;
                    case RADIANCE:
                        sound = SoundsAS.ALTAR_CRAFT_LOOP_T4;
                        break;
                }

                clientCraftSound = ClientSoundHelper.playSoundLoopFadeInClient(sound,
                        new Vector3(this).add(0.5, 0.5, 0.5),
                        0.6F,
                        1F,
                        false,
                        (s) -> isRemoved() ||
                                ClientSoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0 ||
                                this.getActiveRecipe() == null)
                        .setFadeInTicks(40)
                        .setFadeOutTicks(20);
            }

            if (activeRecipe.getState() == ActiveAltarRecipe.CraftingState.WAITING && type.isThisGEThan(AltarType.RADIANCE)) {

                if (clientWaitSound == null || ((PositionedLoopSound) clientWaitSound).hasStoppedPlaying()) {
                    clientWaitSound = ClientSoundHelper.playSoundLoopFadeInClient(SoundsAS.ALTAR_CRAFT_LOOP_T4_WAITING, new Vector3(this).add(0.5, 0.5, 0.5), 0.7F, 1F, false,
                            (s) -> isRemoved() ||
                                    ClientSoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0 ||
                                    this.getActiveRecipe() == null ||
                                    this.getActiveRecipe().getState() != ActiveAltarRecipe.CraftingState.WAITING)
                            .setFadeInTicks(30)
                            .setFadeOutTicks(10);
                }

                ((PositionedLoopSound) clientCraftSound).setVolumeMultiplier(0.75F);
            } else {
                ((PositionedLoopSound) clientCraftSound).setVolumeMultiplier(1F);
            }
        } else {
            clientWaitSound = null;
            clientCraftSound = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void finishCraftingEffects(PktPlayEffect pkt) {
        ResourceLocation recipeName = ByteBufUtils.readResourceLocation(pkt.getExtraData());
        BlockPos at = ByteBufUtils.readPos(pkt.getExtraData());
        boolean isChaining = pkt.getExtraData().readBoolean();

        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        AltarBlockEntity thisAltar = MiscUtils.getBlockEntityAt(world, at, AltarBlockEntity.class, false);
        if (thisAltar != null) {
            //1.16.5: world.getRecipeManager().getRecipes(RecipeTypesAS.TYPE_ALTAR.getType()).get(recipeName).
            //1.20.1's RecipeManager exposes byKey(ResourceLocation) directly; the instanceof below keeps the
            //same "only altar recipes have crafting effects" guard the 1.16.5 lookup-by-type provided.
            Recipe<?> recipe = world.getRecipeManager().byKey(recipeName).orElse(null);
            if (recipe instanceof AltarRecipe altarRecipe) {
                altarRecipe.getCraftingEffects().forEach(effect -> {
                    effect.onCraftingFinish(thisAltar, isChaining);
                });
            }

            if (!isChaining) {
                ClientSoundHelper.playSoundClientWorld(SoundsAS.ALTAR_CRAFT_FINISH, at, 0.6F, 1F);
            }
        }
    }

    private void doCraftingCycle() {
        if (this.activeRecipe == null) {
            return;
        }

        if (!this.hasMultiblock() || !this.activeRecipe.matches(this, false, false)) {
            this.abortCrafting();
            return;
        }
        if (this.activeRecipe.isFinished()) {
            this.finishRecipe();
            return;
        }

        this.activeRecipe.setState(this.activeRecipe.tick(this));
    }

    private void finishRecipe() {
        ActiveAltarRecipe finishedRecipe = this.activeRecipe;

        //1.16.5 wraps the output creation and input consumption in ForgeHooks.setCraftingPlayer so that
        //container-item/remainder logic sees the crafter. tryGetCraftingPlayerServer takes the altar in this
        //tree (it resolves the player off the altar's level) where 1.16.5 kept a world reference on the recipe.
        ForgeHooks.setCraftingPlayer(finishedRecipe.tryGetCraftingPlayerServer(this));
        //1.16.5: finishedRecipe.createItemOutputs(this, this::dropItemOnTop) - this tree's
        //ActiveAltarRecipe#createOutputs(TileAltar) already routes every output through altar.dropItemOnTop.
        finishedRecipe.createOutputs(this);
        finishedRecipe.consumeInputs(this);
        ForgeHooks.setCraftingPlayer(null);

        boolean isChaining;
        ResourceLocation recipeName = finishedRecipe.getRecipe().getId();

        if (!(isChaining = finishedRecipe.matches(this, false, true))) {
            this.abortCrafting();

            EntityFlare.spawnAmbientFlare(level, getBlockPos().offset(-3 + rand.nextInt(7), 1 + rand.nextInt(3), -3 + rand.nextInt(7)));
            EntityFlare.spawnAmbientFlare(level, getBlockPos().offset(-3 + rand.nextInt(7), 1 + rand.nextInt(3), -3 + rand.nextInt(7)));
        }
        PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.ALTAR_RECIPE_FINISH)
                .addData(buf -> {
                    ByteBufUtils.writeResourceLocation(buf, recipeName);
                    ByteBufUtils.writePos(buf, this.getBlockPos());
                    buf.writeBoolean(isChaining);
                });
        //1.16.5: PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(world, pos, 32)), where
        //pointFromPos took a Vector3i and used its lower corner verbatim. This tree's pointFromPos takes a Vec3,
        //so the lower corner is spelled out rather than centering the position.
        PacketChannel.sendToAllAround(pkt, this.level, Vec3.atLowerCornerOf(this.getBlockPos()), 32);

        this.knownRecipes.add(recipeName);
        markForUpdate();
    }

    private void abortCrafting() {
        this.activeRecipe = null;
        markForUpdate();
    }

    //1.16.5: protected SimpleAltarRecipe findRecipe(PlayerEntity) delegating to
    //RecipeTypesAS.TYPE_ALTAR.findRecipe(new SimpleAltarRecipeContext(crafter, LogicalSide.SERVER, this)
    //.setIgnoreStarlightRequirement(false)). See TODO_PORT[AS-ALT-12]: this tree resolves altar recipes through
    //the legacy AltarRecipe static, which applies the same "server side, starlight required" lookup. Back to
    //protected, as in 1.16.5: it had been widened to public only for an invented inline crafting path in
    //common/block/BlockAltar#use, which is gone - crafting goes through onInteract below, driven by ItemWand.
    @Nullable
    protected AltarRecipe findRecipe(@Nullable Player crafter) {
        if (level == null) {
            return null;
        }
        return AltarRecipe.findRecipe(level, this, crafter);
    }

    public boolean startCrafting(AltarRecipe recipe, Player crafter) {
        if (this.getActiveRecipe() != null) {
            return false;
        }

        int divisor = Math.max(0, this.getAltarType().ordinal() - recipe.getAltarType().ordinal());
        divisor = (int) Math.round(Math.pow(2, divisor));
        this.activeRecipe = new ActiveAltarRecipe(recipe, crafter.getUUID(), divisor);
        markForUpdate();

        //1.16.5: SoundHelper.playSoundAround(SoundsAS.ALTAR_CRAFT_START, SoundCategory.BLOCKS, ...) - the
        //CategorizedSoundEvent overload with an explicit SoundSource is the direct 1.20.1 counterpart.
        SoundHelper.playSoundAround(SoundsAS.ALTAR_CRAFT_START, SoundSource.BLOCKS,
                this.level, new Vector3(this).add(0.5, 0.5, 0.5), 0.6F, 1F);
        return true;
    }

    @Override
    public boolean onInteract(Level world, BlockPos pos, Player player, Direction side, boolean sneak) {
        if (!world.isClientSide() && this.hasMultiblock()) {
            if (this.getActiveRecipe() != null) {
                if (this.getActiveRecipe().matches(this, false, false)) {
                    return true;
                }
                abortCrafting();
            }
            AltarRecipe recipe = this.findRecipe(player);
            if (recipe != null) {
                this.startCrafting(recipe, player);
            }
            return true;
        }
        return false;
    }

    private void gatherStarlight() {
        this.tickStarlightCollectionMap.clear();

        WorldContext ctx = SkyHandler.getContext(level);
        if (ctx == null) {
            if (this.starlightNextTick > 0) {
                this.starlightNextTick = 0;
                this.markForUpdate();
            }
            return;
        }
        this.starlightNextTick *= 0.9F;

        if (this.doesSeeSky()) {
            int altarTier = this.getAltarType().ordinal() + 1;

            float heightAmount = Mth.clamp((float) Math.pow(getBlockPos().getY() / 7F, 1.5F) / 65F, 0F, 1F);
            heightAmount *= DayTimeHelper.getCurrentDaytimeDistribution(level);
            this.collectStarlight(heightAmount * altarTier * 60F, AltarCollectionCategory.HEIGHT);

            if (posDistribution == -1) {
                //1.16.5 tested 'world instanceof ISeedReader' - the seeded server-side view SkyCollectionHelper
                //needs. 1.20.1 dropped ISeedReader; ServerLevel is the only implementation the altar ever sees.
                if (level instanceof ServerLevel) {
                    posDistribution = SkyCollectionHelper.getSkyNoiseDistribution((ServerLevel) level, getBlockPos());
                } else {
                    posDistribution = 0.3F;
                }
            }
            float fieldAmount = Mth.sqrt(posDistribution);
            fieldAmount *= DayTimeHelper.getCurrentDaytimeDistribution(level);
            this.collectStarlight(fieldAmount * altarTier * 65F, AltarCollectionCategory.FOSIC_FIELD);
        }

        this.starlightStorage.setStoredStarlight(this.starlightNextTick);
    }

    public void collectStarlight(float percent, AltarCollectionCategory category) {
        int collectable = Mth.floor(Math.min(percent, getRemainingCollectionCapacity(category)));
        this.starlightNextTick = Mth.clamp(this.starlightNextTick + collectable, 0, this.getAltarType().getStarlightCapacity());
        this.tickStarlightCollectionMap.computeIfPresent(category, (cat, remaining) -> Math.max(remaining - collectable, 0));
        this.markForUpdate();
        this.preventNetworkSync();
    }

    public float getRemainingCollectionCapacity(AltarCollectionCategory category) {
        return this.tickStarlightCollectionMap.computeIfAbsent(category, this::getCollectionCap);
    }

    public float getCollectionCap(AltarCollectionCategory category) {
        return this.getAltarType().getStarlightCapacity() / 8.5F / this.getAltarType().getMinimumSources();
    }

    @Nonnull
    public Set<BlockPos> nearbyRelays() {
        Set<BlockPos> eligableRelayOffsets = new HashSet<>();
        for (int xx = -3; xx <= 3; xx++) {
            for (int zz = -3; zz <= 3; zz++) {
                if (xx == 0 && zz == 0) {
                    continue; //Not that it matters though
                }

                BlockPos offset = new BlockPos(xx, 0, zz);
                SpectralRelayBlockEntity tar = MiscUtils.getBlockEntityAt(level, getBlockPos().offset(offset), SpectralRelayBlockEntity.class, true);
                if (tar != null) {
                    eligableRelayOffsets.add(getBlockPos().offset(offset));
                }
            }
        }
        return eligableRelayOffsets;
    }

    @Override
    public void onBreak() {
        super.onBreak();

        if (!level.isClientSide() && !getFocusItem().isEmpty()) {
            ItemUtils.dropItemNaturally(level,
                    getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5,
                    this.focusItem);

            this.focusItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void onFirstTick() {
        super.onFirstTick();

        this.updateNearbyRelayLinkStates();
    }

    private void updateNearbyRelayLinkStates() {
        Set<BlockPos> relayPositions = BlockDiscoverer.searchForBlockEntitiesAround(level, getBlockPos(), 16, blockEntity -> blockEntity instanceof SpectralRelayBlockEntity);

        for (BlockPos relayPos : relayPositions) {
            SpectralRelayBlockEntity tsr = MiscUtils.getBlockEntityAt(level, relayPos, SpectralRelayBlockEntity.class, true);
            if (tsr != null) {
                tsr.updateAltarLinkState();
            }
        }
    }

    public int getStoredStarlight() {
        return this.starlightStorage.getStoredStarlight();
    }

    public float getAmbientStarlightPercent() {
        return ((float) getStoredStarlight()) / ((float) getAltarType().getStarlightCapacity());
    }

    public AltarType getAltarType() {
        return altarType;
    }

    @Nullable
    public ActiveAltarRecipe getActiveRecipe() {
        return activeRecipe;
    }

    @Nonnull
    public ItemStack getFocusItem() {
        return focusItem;
    }

    //VERIFY_PORT[AS-ALT-13]: 1.16.5 has no TileAltar#consumeStarlight - the altar's starlight is a purely
    //transient per-tick pool (gatherStarlight rebuilds it every tick and the deferred storage publishes it two
    //ticks later); 1.16.5 recipes only ever *test* getStoredStarlight(). This tree's out-of-lane
    //common/crafting/ActiveAltarRecipe.java:178 calls altar.consumeStarlight(starlightPerTick), so the method is
    //kept and drains the same accumulator gatherStarlight fills. Remove it once AS-ALT-12 collapses the two
    //active-recipe hierarchies onto the 1.16.5 semantics.
    public void consumeStarlight(int amount) {
        this.starlightNextTick = Math.max(0, this.starlightNextTick - amount);
    }

    //1.16.5 expresses this through TileInventoryFiltered's slot predicate rather than a method on the tile
    //(see TODO_PORT[AS-ALT-11]); common/crafting/ActiveAltarRecipe.java:234 (outside this slice) calls it.
    public boolean hasSlot(int slotId) {
        return altarType.hasSlot(slotId);
    }

    public void setFocusItem(@Nonnull ItemStack focusItem) {
        this.focusItem = focusItem;
        this.markForUpdate();
    }

    @Nullable
    public IConstellation getFocusedConstellation() {
        ItemStack focus = getFocusItem();
        if (focus.getItem() instanceof IConstellationFocus) {
            return ((IConstellationFocus) focus.getItem()).getFocusConstellation(focus);
        }
        return null;
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return this.altarType.getRequiredStructure();
    }

    @Nonnull
    public ItemStackHandlerAS getInventory() {
        return inventory;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        //1.16.5: AxisAlignedBB#expand(0, 5, 0) grows the box in the positive direction only; 1.20.1's
        //AABB#inflate(x, y, z) grows symmetrically. Both are only used to keep the altar's tall crafting VFX
        //inside the render box, and 1.16.5 follows it with grow(3, 0, 3) for radiance altars.
        AABB box = super.getRenderBoundingBox().inflate(0, 5, 0);
        if (this.getAltarType().isThisGEThan(AltarType.RADIANCE)) {
            box = box.inflate(3, 0, 3);
        }
        return box;
    }

    @SuppressWarnings("unchecked")
    public <T extends AltarBlockEntity> T updateType(AltarType newType, boolean initialPlacement) {
        if (!initialPlacement) {
            this.abortCrafting();
        }

        this.altarType = newType;

        CompoundTag thisTag = new CompoundTag();
        this.writeCustomNBT(thisTag);
        this.readCustomNBT(thisTag);
        if (!initialPlacement) {
            this.markForUpdate();

            this.hasMultiblock();
        }
        return (T) this;
    }

    @Override
    protected void readNetNBT(CompoundTag compound) {
        super.readNetNBT(compound);

        this.starlightStorage.readNBT(compound);
    }

    @Override
    protected void writeNetNBT(CompoundTag compound) {
        super.writeNetNBT(compound);

        this.starlightStorage.writeNBT(compound);
    }

    @Override
    protected void readCustomNBT(CompoundTag compound) {
        super.readCustomNBT(compound);

        this.altarType = AltarType.values()[compound.getInt("altarType")];
        this.inventory.deserializeNBT(compound.getCompound("inventory"));
        this.focusItem = NBTHelper.getStack(compound, "focusItem");
        //1.16.5: Constants.NBT.TAG_STRING -> 1.20.1: Tag.TAG_STRING; new ResourceLocation(String) is deprecated
        //in Forge 47.x in favour of the backported ResourceLocation.parse(String), which is the same parse.
        this.knownRecipes = NBTHelper.readSet(compound, "knownRecipes", Tag.TAG_STRING,
                nbt -> ResourceLocation.parse(nbt.getAsString()));

        if (compound.contains("activeRecipe", Tag.TAG_COMPOUND)) {
            //1.16.5: ActiveSimpleAltarRecipe.deserialize(tag, this.activeRecipe) - the previous instance was
            //passed in so contained crafting effects could be recovered, and no world/RecipeManager was needed.
            //This tree's ActiveAltarRecipe#deserialize resolves the recipe by id through a RecipeManager
            //(ActiveAltarRecipe.java:308) and performs the effect recovery internally (ActiveAltarRecipe.java:57).
            //VERIFY_PORT[AS-ALT-14]: BlockEntity#load runs before setLevel on the disk-load path, so this.level is
            //null there; the server's RecipeManager is used as the fallback so a saved in-progress craft survives
            //a chunk reload instead of NPE-ing. Drop the fallback once AS-ALT-12 restores the 1.16.5 two-argument
            //deserialize that needs no manager at all.
            RecipeManager recipeManager = null;
            if (this.level != null) {
                recipeManager = this.level.getRecipeManager();
            } else {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server != null) {
                    recipeManager = server.getRecipeManager();
                }
            }
            this.activeRecipe = recipeManager == null ? null :
                    ActiveAltarRecipe.deserialize(compound.getCompound("activeRecipe"), recipeManager);
        } else {
            this.activeRecipe = null;
        }
    }

    @Override
    protected void writeCustomNBT(CompoundTag compound) {
        super.writeCustomNBT(compound);

        compound.putInt("altarType", this.altarType.ordinal());
        compound.put("inventory", this.inventory.serializeNBT());
        NBTHelper.setStack(compound, "focusItem", this.focusItem);
        NBTHelper.writeList(compound, "knownRecipes", this.knownRecipes, key -> StringTag.valueOf(key.toString()));

        if (this.activeRecipe != null) {
            compound.put("activeRecipe", this.activeRecipe.serialize());
        }
    }

    @Nonnull
    @Override
    public StarlightReceiverAltar provideEndpoint(BlockPos at) {
        return new StarlightReceiverAltar(at);
    }
}
