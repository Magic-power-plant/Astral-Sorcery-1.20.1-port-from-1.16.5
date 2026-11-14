/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.effect.function.RefreshFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.source.orbital.FXOrbitalInfuserLiquid;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.ColorizationHelper;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.auxiliary.ChaliceHelper;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.blockentity.ChaliceBlockEntity;
import hellfirepvp.astralsorcery.common.blockentity.InfuserBlockEntity;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActiveLiquidInfusionRecipe
 * Created by HellFirePvP
 * Date: 09.11.2019 / 11:24
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 calls this common/crafting/recipe/infusion/ActiveLiquidInfusionRecipe. Not
//moved because the package layout is a tree-wide decision.
//TODO_PORT[AS-INF-04]: this tree carries two parallel active-infusion classes. The reference-faithful port of
//1.16.5's ActiveLiquidInfusionRecipe lives at common/crafting/recipe/infusion/ActiveLiquidInfusionRecipe;
//this class is the divergent *live* one - common/tile/TileInfuser holds, ticks and serializes it. The two are
//siblings, not sub/supertypes, so they cannot be merged from inside this slice. Structural differences
//against 1.16.5, all of them pinned by out-of-lane callers:
// - The constructor is (TileInfuser, LiquidInfusion, UUID) rather than 1.16.5's
//   (World, BlockPos, LiquidInfusion, UUID), because common/tile/TileInfuser.java:155 builds it that way.
//   The private findChalices(Level, BlockPos) below keeps the reference signature.
// - finish(TileInfuser) has no 1.16.5 counterpart: 1.16.5 ran its body inline in
//   TileInfuser#finishRecipe (1.16.5 common/tile/TileInfuser.java:210-213 plus the sound and the known-recipe
//   bookkeeping on lines 217/228). common/tile/TileInfuser.java:119 calls finish(this), so the aggregation
//   stays and carries the reference bodies.
// - getProgress() has no 1.16.5 counterpart either (1.16.5 TileInfuser rendered off getTicksCrafting() /
//   getTotalCraftingTime() directly); common/tile/TileInfuser.java:110 calls it, so it stays.
// - deserialize takes the RecipeManager as a parameter instead of pulling it from
//   common/util/RecipeHelper#getRecipeManager, because common/tile/TileInfuser.java:304/317 hands its
//   level's manager in. The reference's "previous active recipe" parameter is restored as an overload so the
//   client orbital effect survives a re-read (1.16.5 TileInfuser.java:327).
public class ActiveLiquidInfusionRecipe {

    private static final Random rand = new Random();
    private static final int CHALICE_DISTANCE = 8;

    private final LiquidInfusion recipeToCraft;
    private final UUID playerCraftingUUID;

    private int ticksCrafting = 0;
    private final Set<BlockPos> supportingChalices = new HashSet<>();
    private CompoundTag craftingData = new CompoundTag();

    private Object orbitalLiquid = null;

    //1.16.5 signature: (World world, BlockPos center, LiquidInfusion recipeToCraft, UUID playerCraftingUUID).
    //See the TODO_PORT[AS-INF-04] note above - common/tile/TileInfuser.java:155 pins this shape. The extra
    //level null-check has no 1.16.5 counterpart; BlockEntity#getLevel() is @Nullable in 1.20.1 and the tile is
    //constructed before it is added to a level, so the lookup is skipped rather than NPE-ing there.
    public ActiveLiquidInfusionRecipe(InfuserBlockEntity infuser, LiquidInfusion recipeToCraft, UUID playerCraftingUUID) {
        this(recipeToCraft, playerCraftingUUID);

        if (infuser.getLevel() != null && this.recipeToCraft.acceptsChaliceInput()) {
            this.findChalices(infuser.getLevel(), infuser.getBlockPos());
        }
    }

    private ActiveLiquidInfusionRecipe(LiquidInfusion recipeToCraft, UUID playerCraftingUUID) {
        this.recipeToCraft = recipeToCraft;
        this.playerCraftingUUID = playerCraftingUUID;
    }

    //1.16.5: World -> Level in 1.20.1.
    private void findChalices(Level world, BlockPos center) {
        ChaliceHelper.findNearbyChalicesCombined(world, center, this.getChaliceRequiredFluidInput(), CHALICE_DISTANCE)
                //1.16.5: TileEntity#getPos() - renamed to getBlockPos() in 1.20.1.
                .ifPresent(chalices -> chalices.forEach(chalice -> this.supportingChalices.add(chalice.getBlockPos())));
    }

    public boolean matches(InfuserBlockEntity infuser) {
        //1.16.5 passed LogicalSide.SERVER explicitly (1.16.5 line 100). This tree's LiquidInfusion also has a
        //two-argument matches(TileInfuser, Player) that forwards with LogicalSide.SERVER, but the explicit
        //form is what the reference calls, so it is used here as well.
        if (!this.getRecipeToCraft().matches(infuser, this.tryGetCraftingPlayerServer(), LogicalSide.SERVER)) {
            return false;
        }

        if (!this.supportingChalices.isEmpty()) {
            //1.16.5: TileEntity#getWorld() - renamed to getLevel() in 1.20.1.
            if (!ChaliceHelper.doChalicesContainCombined(infuser.getLevel(), this.supportingChalices, this.getChaliceRequiredFluidInput())) {
                this.supportingChalices.clear();
            }
        }
        return true;
    }

    public void tick() {
        this.ticksCrafting++;
    }

    //AS-INF-05 (resolved): called from TileInfuser#tick's client branch, as 1.16.5 does
    // (1.16.5 common/tile/TileInfuser.java:121). Until that branch existed this had zero callers, which also made
    // FXOrbitalInfuserLiquid unreachable.
    @OnlyIn(Dist.CLIENT)
    public void tickClient(InfuserBlockEntity infuser) {
        //1.16.5: FluidAttributes.BUCKET_VOLUME - moved to FluidType.BUCKET_VOLUME in 1.20.1.
        FluidStack required = new FluidStack(this.getRecipeToCraft().getLiquidInput(), FluidType.BUCKET_VOLUME);

        if (orbitalLiquid == null || ((FXOrbitalInfuserLiquid) orbitalLiquid).isRemoved()) {
            ResourceLocation recipeName = this.getRecipeToCraft().getId();

            orbitalLiquid = Effects.spawnSource(
                    new FXOrbitalInfuserLiquid(new Vector3(infuser).add(0.5F, 0, 0.5F), required)
                            .setOrbitAxis(Vector3.RotAxis.Y_AXIS)
                            .setOrbitRadius(2F)
                            .setBranches(4)
                            .setMaxAge(300)
                            .refresh(RefreshFunction.blockEntityExistsAnd(infuser,
                                    (tInfuser, fx) -> tInfuser.getActiveRecipe() != null &&
                                            recipeName.equals(tInfuser.getActiveRecipe().getRecipeToCraft().getId()))));
            ((FXOrbitalInfuserLiquid) orbitalLiquid).setActive();
        }

        for (int i = 0; i < 2; i++) {
            playLiquidEffect(infuser, required);
        }

        for (int i = 0; i < 7; i++) {
            playLiquidPoolEffect(infuser, required);
        }

        if (!this.supportingChalices.isEmpty()) {
            playLiquidDrawEffect(infuser, required);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playLiquidDrawEffect(InfuserBlockEntity infuser, FluidStack required) {
        Collection<BlockPos> chalices = this.supportingChalices;
        if (chalices.isEmpty()) {
            return;
        }
        Vector3 target = new Vector3(infuser).add(0.5, 1.1, 0.5);
        TextureAtlasSprite tas = RenderingUtils.getParticleTexture(required);
        VFXColorFunction<?> colorFn = (fx, pTicks) -> new Color(ColorUtils.getOverlayColor(required));
        for (int i = 0; i < 2 * this.supportingChalices.size(); i++) {
            BlockPos chalice = MiscUtils.getRandomEntry(chalices, rand);
            Vector3 pos = new Vector3(chalice).add(0.5, 1.4, 0.5);

            int maxAge = 30;
            maxAge *= Math.max(pos.distance(target) / 3, 1);

            if (rand.nextInt(3) != 0) {
                MiscUtils.applyRandomOffset(pos, rand, 0.3F);
                Effects.of(EffectTemplatesAS.GENERIC_ATLAS_PARTICLE)
                        .spawn(pos)
                        .setSprite(tas)
                        .selectFraction(0.2F)
                        .setScaleMultiplier(0.01F + rand.nextFloat() * 0.04F)
                        .color(colorFn)
                        .alpha(VFXAlphaFunction.proximity(() -> target, 2F).andThen(VFXAlphaFunction.FADE_OUT))
                        .motion(VFXMotionController.target(target::clone, 0.08F))
                        .setMaxAge(maxAge);
            } else {
                MiscUtils.applyRandomOffset(pos, rand, 0.4F);
                Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(pos)
                        .setScaleMultiplier(0.15F + rand.nextFloat() * 0.1F)
                        .color(colorFn)
                        .alpha(VFXAlphaFunction.proximity(() -> target, 2F).andThen(VFXAlphaFunction.FADE_OUT))
                        .motion(VFXMotionController.target(target::clone, 0.08F))
                        .setMaxAge(maxAge);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playLiquidEffect(InfuserBlockEntity infuser, FluidStack required) {
        //1.16.5: TileInfuser#getRandomInfuserOffset() returned a Vector3; this tree's TileInfuser returns the
        //same absolute position as a vanilla Vec3, so it is wrapped back into a (freshly allocated, hence
        //safely mutable) Vector3 before applyRandomOffset mutates it.
        Vector3 vec = new Vector3(infuser.getRandomInfuserOffset());
        MiscUtils.applyRandomOffset(vec, rand, 0.05F);
        Effects.of(EffectTemplatesAS.GENERIC_ATLAS_PARTICLE)
                .spawn(vec)
                .setSprite(RenderingUtils.getParticleTexture(required))
                .selectFraction(0.2F)
                .setScaleMultiplier(0.03F + rand.nextFloat() * 0.03F)
                .color((fx, pTicks) -> new Color(ColorUtils.getOverlayColor(required)))
                .alpha(VFXAlphaFunction.FADE_OUT)
                .motion(VFXMotionController.target(() -> new Vector3(infuser).add(0.5, 1.1, 0.5), 0.3F))
                .setMaxAge(40);
    }

    @OnlyIn(Dist.CLIENT)
    private void playLiquidPoolEffect(InfuserBlockEntity infuser, FluidStack required) {
        List<BlockPos> posList = InfuserBlockEntity.getLiquidOffsets().stream()
                //1.16.5: BlockPos#add(Vec3i) - renamed to offset(Vec3i) in 1.20.1.
                .map(pos -> pos.offset(infuser.getBlockPos()))
                .collect(Collectors.toList());

        BlockPos at = MiscUtils.getRandomEntry(posList, rand);
        if (at != null) {
            Effects.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(at).add(rand.nextFloat(), 1, rand.nextFloat()))
                    .setScaleMultiplier(0.1F + rand.nextFloat() * 0.15F)
                    .color((fx, pTicks) -> ColorizationHelper.getColor(required).orElse(Color.WHITE))
                    .setAlphaMultiplier(1F)
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setMotion(new Vector3(0, 0.15, 0))
                    .setGravityStrength(0.005F + rand.nextFloat() * 0.008F);
        }
    }

    public void clearEffects() {
        if (orbitalLiquid != null) {
            //Only exists on client after all
            clearClientEffect();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clearClientEffect() {
        ((FXOrbitalInfuserLiquid) orbitalLiquid).requestRemoval();
    }

    public void createItemOutputs(InfuserBlockEntity infuser, Consumer<ItemStack> output) {
        //TODO_PORT[AS-INF-01]: 1.16.5 called ResearchManager.informCraftedInfuser(infuser, this, stack)
        // (1.16.5 common/crafting/recipe/infusion/ActiveLiquidInfusionRecipe.java:235). This tree's
        // common/data/research/ResearchManager has no informCraftedInfuser at all (it only ported
        // informCraftedAltar), and common/data/research is not owned by this slice. To avoid silently
        // dropping the research progression the 1.16.5 wrapper body
        // (1.16.5 common/data/research/ResearchManager.java:503-512) is inlined below against the public
        // ResearchManager#informCrafted; move it back into ResearchManager#informCraftedInfuser once that
        // method exists. The reference-faithful sibling
        // common/crafting/recipe/infusion/ActiveLiquidInfusionRecipe carries the identical treatment.
        Consumer<ItemStack> informer = stack -> this.informCraftedInfuser(infuser, stack);

        ItemStack inputStack = infuser.getItemInput();
        Consumer<ItemStack> handleCrafted = informer.andThen(output);
        if (this.recipeToCraft.doesCopyNBTToOutputs()) {
            handleCrafted = ((Consumer<ItemStack>) stack -> stack.setTag(inputStack.getTag())).andThen(handleCrafted);
        }
        handleCrafted.accept(this.getRecipeToCraft().getOutput(inputStack));
        this.getRecipeToCraft().onRecipeCompletion(infuser);
    }

    //See createItemOutputs: verbatim body of 1.16.5 ResearchManager#informCraftedInfuser
    //(1.16.5 common/data/research/ResearchManager.java:503-512), which does not trigger any advancement -
    //unlike informCraftedAltar - and only forwards to informCrafted.
    private void informCraftedInfuser(@Nonnull InfuserBlockEntity infuser, @Nonnull ItemStack crafted) {
        Player crafter = this.tryGetCraftingPlayerServer();
        if (!(crafter instanceof ServerPlayer)) {
            //1.16.5: AstralSorcery.log - the logger field is named LOG in this tree.
            AstralSorcery.LOG.warn("Infusion finished, player that initialized crafting could not be found!");
            //1.16.5: World#getDimensionKey().getLocation() - Level#dimension().location() in 1.20.1.
            AstralSorcery.LOG.warn("Affected tile: " + infuser.getBlockPos() + " in dim " +
                    (infuser.getLevel() == null ? "<unknown>" : infuser.getLevel().dimension().location()));
            return;
        }

        ResearchManager.informCrafted(crafter, crafted);
    }

    public void consumeInputs(InfuserBlockEntity infuser) {
        //TODO_PORT[AS-INF-02]: 1.16.5 called the three-argument
        // ItemUtils.decrementItem(Supplier<ItemStack>, Consumer<ItemStack> setIntoInventory,
        // Consumer<ItemStack> handleExcess) (1.16.5 common/util/item/ItemUtils.java:83), which shrank the
        // stack by exactly one and merged/overflowed any container item. This tree's
        // common/util/item/ItemUtils only has the four-argument form, which hands the *whole* stack to the
        // extractor and drops the container-item merge logic; common/util/item is not owned by this slice,
        // so the call is routed through the existing four-argument form and the missing overload is reported
        // as an external change. TileInfuser's slot is capped at one item (TileInfuser.java:73-75), so
        // clearing it is the same as shrinking it by one here.
        ItemUtils.decrementItem(infuser::getItemInput,
                stack -> infuser.setItemInput(ItemStack.EMPTY),
                infuser::setItemInput,
                infuser::dropItemOnTop);
    }

    public void consumeFluidsInput(InfuserBlockEntity infuser) {
        float chaliceSupplied = 0F;
        if (!this.supportingChalices.isEmpty()) {
            FluidStack required = this.getChaliceRequiredFluidInput();
            Optional<List<ChaliceBlockEntity>> chalices = ChaliceHelper.findNearbyChalicesCombined(infuser.getLevel(), infuser.getBlockPos(), required, CHALICE_DISTANCE);
            if (chalices.isPresent()) {
                FluidStack left = required.copy();
                for (ChaliceBlockEntity chalice : chalices.get()) {
                    left.shrink(chalice.getTank().drain(left, IFluidHandler.FluidAction.EXECUTE).getAmount());
                    if (left.isEmpty()) {
                        break;
                    }
                }
                if (left.isEmpty()) {
                    return; // Chalices provided fluid
                }
                //Verbatim upstream arithmetic (1.16.5 line 266): required/left, not 1 - left/required. Kept
                //as-is on purpose - this is upstream behaviour, not a translation artifact.
                chaliceSupplied = ((float) required.getAmount()) / left.getAmount();
            }
        }

        LiquidInfusion infusion = this.getRecipeToCraft();
        float chance = infusion.getConsumptionChance() * (1F - chaliceSupplied);
        if (infusion.doesConsumeMultipleFluids()) {
            for (BlockPos at : InfuserBlockEntity.getLiquidOffsets()) {
                if (rand.nextFloat() < chance) {
                    //1.16.5: World#setBlockState -> Level#setBlock, Block#getDefaultState ->
                    //defaultBlockState, and Constants.BlockFlags.DEFAULT_AND_RERENDER (11) is
                    //Block.UPDATE_ALL_IMMEDIATE in 1.20.1.
                    infuser.getLevel().setBlock(at.offset(infuser.getBlockPos()), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
                }
            }
        } else {
            //1.16.5 picks the position *before* rolling the chance, so the two RNG draws happen in that
            //order; kept in the reference order so a seeded run behaves identically.
            BlockPos at = MiscUtils.getRandomEntry(InfuserBlockEntity.getLiquidOffsets(), rand).offset(infuser.getBlockPos());
            if (rand.nextFloat() < chance) {
                infuser.getLevel().setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
            }
        }
    }

    public FluidStack getChaliceRequiredFluidInput() {
        //1.16.5: FluidAttributes.BUCKET_VOLUME - moved to FluidType.BUCKET_VOLUME in 1.20.1.
        int amount = Math.round(FluidType.BUCKET_VOLUME * recipeToCraft.getConsumptionChance());
        amount *= 0.75; //Bonus for using chalices

        amount = recipeToCraft.doesConsumeMultipleFluids() ? amount * InfuserBlockEntity.getLiquidOffsets().size() : amount;
        return new FluidStack(this.getRecipeToCraft().getLiquidInput(), amount);
    }

    public int getTotalCraftingTime() {
        int tickTime = this.recipeToCraft.getCraftingTickTime();

        int fixTime = Math.round(tickTime * 0.25F);
        int chaliceTime = Math.round(tickTime * 0.75F);
        chaliceTime /= this.supportingChalices.size() + 1;

        return fixTime + chaliceTime;
    }

    public CompoundTag getCraftingData() {
        return craftingData;
    }

    public UUID getPlayerCraftingUUID() {
        return playerCraftingUUID;
    }

    public int getTicksCrafting() {
        return ticksCrafting;
    }

    public LiquidInfusion getRecipeToCraft() {
        return recipeToCraft;
    }

    public boolean isFinished() {
        return getTicksCrafting() >= getTotalCraftingTime();
    }

    /**
     * No 1.16.5 counterpart - see the TODO_PORT[AS-INF-04] note on the class. 1.16.5's TileInfuser tracked the
     * progress bar off getTicksCrafting() / getTotalCraftingTime() itself; this tree's
     * common/tile/TileInfuser.java:110 asks the active recipe for it instead.
     */
    public float getProgress() {
        int total = getTotalCraftingTime();
        return total <= 0 ? 1F : Math.min(1F, this.ticksCrafting / (float) total);
    }

    @Nullable
    public Player tryGetCraftingPlayerServer() {
        //1.16.5: LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER). LogicalSidedProvider lost its
        //INSTANCE constant in 1.20.1; ServerLifecycleHooks.getCurrentServer() is the replacement this tree
        //already uses elsewhere (see common/util/RecipeHelper and common/data/research/ResearchHelper). It
        //can be null off-server, which the 1.16.5 call could not be, hence the extra guard.
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) {
            return null;
        }
        //1.16.5: PlayerList#getPlayerByUUID - renamed to getPlayer(UUID) in 1.20.1.
        return srv.getPlayerList().getPlayer(this.getPlayerCraftingUUID());
    }

    /**
     * No 1.16.5 counterpart as a method - this is 1.16.5 TileInfuser#finishRecipe
     * (1.16.5 common/tile/TileInfuser.java:207-229) minus the parts that belong to the tile itself, moved
     * here because this tree's common/tile/TileInfuser.java:119 delegates to it and common/tile is not owned
     * by this slice. Lines 210-213 (setCraftingPlayer / createItemOutputs / consumeInputs /
     * consumeFluidsInput / setCraftingPlayer(null)), line 217 (the finish sound) and line 228 (the
     * known-recipe bookkeeping) are reproduced verbatim.
     * <p>
     * 1.16.5 ran abortCrafting() - which is clearEffects() + activeRecipe = null + markForUpdate() - between
     * the crafting player reset and the sound; here the caller does that right after this method returns
     * (TileInfuser.java:120-124), so the sound and the known-recipe add happen one step earlier in the
     * sequence. Neither reads the active recipe, so the observable result is the same.
     * <p>
     * AS-INF-03 (resolved): the INFUSER_RECIPE_FINISH effect packet and the ambient flare
     * (1.16.5 common/tile/TileInfuser.java:219-227) now live in TileInfuser#finishRecipe, which is where the
     * reference puts them, rather than being smuggled into this recipe class.
     */
    public void finish(InfuserBlockEntity infuser) {
        Player player = this.tryGetCraftingPlayerServer();
        ForgeHooks.setCraftingPlayer(player);
        this.createItemOutputs(infuser, infuser::dropItemOnTop);
        this.consumeInputs(infuser);
        this.consumeFluidsInput(infuser);
        ForgeHooks.setCraftingPlayer(null);

        if (infuser.getLevel() != null) {
            //1.16.5: SoundHelper.playSoundAround(SoundsAS.INFUSER_CRAFT_FINISH, world, pos, 1F, 1F)
            //(1.16.5 common/tile/TileInfuser.java:217). The previous implementation of this method played a
            //vanilla SoundEvents.BEACON_ACTIVATE instead, which has no 1.16.5 counterpart; the mod sound is
            //registered in this tree (common/lib/SoundsAS.java:36) so the reference call is restored.
            SoundHelper.playSoundAround(SoundsAS.INFUSER_CRAFT_FINISH, infuser.getLevel(), infuser.getBlockPos(), 1F, 1F);
        }
        infuser.addKnownRecipe(this.recipeToCraft.getId());
    }

    @Nullable
    public static ActiveLiquidInfusionRecipe deserialize(CompoundTag compound, RecipeManager mgr, @Nullable ActiveLiquidInfusionRecipe prev) {
        //1.16.5 resolved the manager itself through RecipeHelper.getRecipeManager(); this tree's
        //common/tile/TileInfuser hands its level's manager in (TileInfuser.java:304/317). Same null guard.
        if (mgr == null) {
            return null;
        }

        //1.16.5: new ResourceLocation(String) - deprecated in this Forge version, ResourceLocation.parse is
        //the replacement and throws the very same ResourceLocationException on a malformed id.
        ResourceLocation recipeKey = ResourceLocation.parse(compound.getString("recipeToCraft"));
        //1.16.5: RecipeManager#getRecipe(ResourceLocation) - renamed to byKey in 1.20.1.
        Optional<?> recipe = mgr.byKey(recipeKey);
        if (!recipe.isPresent() || !(recipe.get() instanceof LiquidInfusion)) {
            //1.16.5: AstralSorcery.log - the logger field is named LOG in this tree.
            AstralSorcery.LOG.info("Recipe with unknown/invalid name found: " + recipeKey);
            return null;
        }
        LiquidInfusion altarRecipe = (LiquidInfusion) recipe.get();

        //1.16.5: CompoundNBT#getUniqueId - renamed to getUUID in 1.20.1.
        UUID uuidCraft = compound.getUUID("playerCraftingUUID");
        int tick = compound.getInt("ticksCrafting");
        //1.16.5: Constants.NBT.TAG_COMPOUND - the NBT type ids live on net.minecraft.nbt.Tag in 1.20.1.
        ListTag chalices = compound.getList("supportingChalices", Tag.TAG_COMPOUND);

        Set<BlockPos> chalicePositions = new HashSet<>();
        for (int i = 0; i < chalices.size(); i++) {
            CompoundTag tag = chalices.getCompound(i);
            chalicePositions.add(NBTHelper.readBlockPosFromNBT(tag));
        }

        ActiveLiquidInfusionRecipe task = new ActiveLiquidInfusionRecipe(altarRecipe, uuidCraft);
        task.ticksCrafting = tick;
        task.craftingData = compound.getCompound("craftingData");
        task.supportingChalices.addAll(chalicePositions);

        if (prev != null && prev.orbitalLiquid != null) {
            task.orbitalLiquid = prev.orbitalLiquid;
        }
        return task;
    }

    /**
     * Two-argument shorthand with no 1.16.5 counterpart; 1.16.5's only deserialize took the previous active
     * recipe so the client orbital effect could be carried across a re-read
     * (1.16.5 common/tile/TileInfuser.java:327 passes this.activeRecipe). This tree's
     * common/tile/TileInfuser.java:304/317 does not, so the overload exists to keep it compiling - it is
     * reported as an external change so the tile can start passing its previous recipe.
     */
    @Nullable
    public static ActiveLiquidInfusionRecipe deserialize(CompoundTag compound, RecipeManager mgr) {
        return deserialize(compound, mgr, null);
    }

    @Nonnull
    public CompoundTag serialize() {
        //1.16.5 wrote each chalice position through NBTHelper.writeBlockPosToNBT (bposX/bposY/bposZ ints);
        //the previous implementation of this method used a single packed "pos" long instead. Both halves of
        //the round trip live in this class, so the reference format is restored - it also keeps this class
        //save-compatible with its reference-faithful sibling
        //common/crafting/recipe/infusion/ActiveLiquidInfusionRecipe.
        ListTag chalicePositions = new ListTag();
        this.supportingChalices.forEach(pos -> chalicePositions.add(NBTHelper.writeBlockPosToNBT(pos, new CompoundTag())));

        CompoundTag compound = new CompoundTag();
        compound.putString("recipeToCraft", getRecipeToCraft().getId().toString());
        //1.16.5: CompoundNBT#putUniqueId - renamed to putUUID in 1.20.1.
        compound.putUUID("playerCraftingUUID", getPlayerCraftingUUID());
        compound.putInt("ticksCrafting", getTicksCrafting());
        compound.put("craftingData", craftingData);
        compound.put("supportingChalices", chalicePositions);
        return compound;
    }
}
