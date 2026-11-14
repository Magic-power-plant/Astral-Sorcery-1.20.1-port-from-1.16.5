package hellfirepvp.astralsorcery.common.blockentity;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.crafting.ActiveLiquidInfusionRecipe;
import hellfirepvp.astralsorcery.common.crafting.LiquidInfusion;
import hellfirepvp.astralsorcery.common.effect.Effects;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.lib.StructureTypesAS;
import hellfirepvp.astralsorcery.common.lib.BlockEntityTypesAS;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.blockentity.base.TickableBlockEntity;
import hellfirepvp.astralsorcery.common.util.ColorUtils;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.sound.CategorizedSoundEvent;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//1.16.5 TileInfuser.java:75 extends TileEntityTick. This tree had it on TileEntitySynchronized, which carries no
//ticksExisted counter and no client tick branch, so the whole client presentation layer of the Starlight Infuser
//was unreachable: ActiveLiquidInfusionRecipe#tickClient had zero callers, SoundsAS.INFUSER_CRAFT_LOOP and
//INFUSER_CRAFT_START had zero usages tree-wide, and RenderInfuser had to pass a literal 0 as the item age.
//TileEntityTick also already implements TileRequiresMultiblock and provides a cached, client-synced
//hasMultiblock(), which is why the local override below is gone.
public class InfuserBlockEntity extends TickableBlockEntity {

    public static final String TAG_INFUSION_PROGRESS = "infusionProgress";
    public static final String TAG_IS_INFUSING = "isInfusing";
    public static final String TAG_INVENTORY = "inventory";
    public static final String TAG_KNOWN_RECIPES = "knownRecipes";
    public static final String TAG_ACTIVE_RECIPE = "activeRecipe";

    private static final Set<BlockPos> LIQUID_OFFSETS = Set.of(
            new BlockPos( 1, -1,  2),
            new BlockPos( 0, -1,  2),
            new BlockPos(-1, -1,  2),
            new BlockPos( 1, -1, -2),
            new BlockPos( 0, -1, -2),
            new BlockPos(-1, -1, -2),
            new BlockPos( 2, -1,  1),
            new BlockPos( 2, -1,  0),
            new BlockPos( 2, -1, -1),
            new BlockPos(-2, -1,  1),
            new BlockPos(-2, -1,  0),
            new BlockPos(-2, -1, -1)
    );

    private final ItemStackHandler inventory;
    private final Set<ResourceLocation> knownRecipes = new HashSet<>();
    @Nullable
    private ActiveLiquidInfusionRecipe activeRecipe;
    @Nullable
    private CompoundTag pendingActiveRecipe;
    private float infusionProgress = 0F;
    private boolean isInfusing = false;

    //Typed as Object for the same reason TileAltar does it (TileAltar.java:108): the field lives on a common
    //class, and PositionedLoopSound is client-only.
    private Object clientCraftSound = null;

    public InfuserBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypesAS.INFUSER.get(), pos, state);
        this.inventory = new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                markForUpdate();
            }
        };
    }

    @Override
    @Nullable
    public StructureType getRequiredStructureType() {
        return StructureTypesAS.PTYPE_INFUSER;
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null) {
            return;
        }
        if (!level.isClientSide) {
            this.doCraftingCycle();
        } else {
            //1.16.5 TileInfuser.java:117-122. Without this branch ActiveLiquidInfusionRecipe#tickClient - the
            //orbital FX, the liquid pool and the chalice draw beams - never ran, so a running infusion was
            //completely invisible and silent to the player.
            if (this.getActiveRecipe() != null) {
                this.getActiveRecipe().tickClient(this);
                this.doCraftSound();
            }
        }
    }

    //1.16.5 TileInfuser.java:125-144, same shape as TileAltar#doCraftSound in this tree.
    @OnlyIn(Dist.CLIENT)
    private void doCraftSound() {
        //1.16.5: SoundCategory.BLOCKS -> 1.20.1: SoundSource.BLOCKS
        if (SoundHelper.getSoundVolume(SoundSource.BLOCKS) > 0) {
            if (clientCraftSound == null || ((PositionedLoopSound) clientCraftSound).hasStoppedPlaying()) {
                CategorizedSoundEvent sound = SoundsAS.INFUSER_CRAFT_LOOP;

                clientCraftSound = SoundHelper.playSoundLoopFadeInClient(sound,
                        new Vector3(this).add(0.5, 0.5, 0.5),
                        1F,
                        1F,
                        false,
                        (s) -> isRemoved() ||
                                SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0 ||
                                this.getActiveRecipe() == null)
                        .setFadeInTicks(30)
                        .setFadeOutTicks(20);
            }
        } else {
            clientCraftSound = null;
        }
    }

    private void doCraftingCycle() {
        if (this.activeRecipe == null) {
            return;
        }
        if (!this.hasMultiblock() || !this.activeRecipe.matches(this)) {
            this.abortCrafting();
            return;
        }
        if (this.activeRecipe.isFinished()) {
            this.finishRecipe();
            return;
        }
        this.activeRecipe.tick();
        this.infusionProgress = this.activeRecipe.getProgress() * 100F;
        // Persist each tick, but limit block-entity update broadcasts to four per second.
        this.setChanged();
        if (this.ticksExisted % 5 == 0) {
            this.markForUpdate();
        }
    }

    private void finishRecipe() {
        ActiveLiquidInfusionRecipe recipe = this.activeRecipe;
        if (recipe == null) {
            return;
        }
        ResourceLocation recipeName = recipe.getRecipeToCraft().getId();
        recipe.finish(this);
        recipe.clearEffects();
        this.activeRecipe = null;
        this.isInfusing = false;
        this.infusionProgress = 0F;

        //AS-INF-03 (resolved): 1.16.5 TileInfuser.java:219-227 sends the finish effect to everyone within 32
        //blocks and spawns an ambient flare. PktPlayEffect.java:130-131 already routed INFUSER_RECIPE_FINISH to
        //finishCraftingEffects below - nothing ever sent it, so the receiver could not fire. Same shape as the
        //working TileAltar path (TileAltar.java:267-276).
        if (this.level != null && !this.level.isClientSide) {
            PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.INFUSER_RECIPE_FINISH)
                    .addData(buf -> {
                        ByteBufUtils.writeResourceLocation(buf, recipeName);
                        ByteBufUtils.writePos(buf, this.getBlockPos());
                    });
            PacketChannel.sendToAllAround(pkt, this.level, Vec3.atLowerCornerOf(this.getBlockPos()), 32);

            EntityFlare.spawnAmbientFlare(this.level, this.getBlockPos()
                    .offset(-3 + rand.nextInt(7), 1 + rand.nextInt(3), -3 + rand.nextInt(7)));
        }

        this.markForUpdate();
    }

    private void abortCrafting() {
        if (this.activeRecipe != null) {
            this.activeRecipe.clearEffects();
            this.activeRecipe = null;
        }
        if (this.isInfusing || this.infusionProgress != 0F) {
            this.isInfusing = false;
            this.infusionProgress = 0F;
            this.markForUpdate();
        }
    }

    @Nullable
    public LiquidInfusion findRecipe(Player crafter) {
        if (level == null) {
            return null;
        }
        return LiquidInfusion.findRecipe(level, this, crafter);
    }

    public boolean startInfusion(Player crafter) {
        if (level == null || this.activeRecipe != null || getItemInput().isEmpty() || !hasMultiblock()) {
            return false;
        }
        LiquidInfusion recipe = this.findRecipe(crafter);
        if (recipe == null) {
            return false;
        }
        this.activeRecipe = new ActiveLiquidInfusionRecipe(this, recipe, crafter.getUUID());
        this.isInfusing = true;
        this.infusionProgress = 0F;
        this.markForUpdate();
        //1.16.5 TileInfuser.java:249 played this from startCrafting. It was the only usage of
        //SoundsAS.INFUSER_CRAFT_START, which is why that sound had none tree-wide.
        SoundHelper.playSoundAround(SoundsAS.INFUSER_CRAFT_START, SoundSource.BLOCKS, this.level,
                new Vector3(this).add(0.5, 0.5, 0.5), 1F, 1F);
        return true;
    }

    public void stopInfusion() {
        this.abortCrafting();
    }

    //hasMultiblock() is inherited from TileEntityTick, which caches the match, syncs it to the client through
    //writeCustomNBT and fires notifyMultiblockStateUpdate on change. The local override this replaces re-ran the
    //full StructureType#matches on both sides every call and never synced, so the client's copy was meaningless.

    public float getInfusionProgress() {
        return infusionProgress;
    }

    public boolean isInfusing() {
        return isInfusing;
    }

    @Nullable
    public ActiveLiquidInfusionRecipe getActiveRecipe() {
        return activeRecipe;
    }

    @Nonnull
    public ItemStack getItemInput() {
        return this.inventory.getStackInSlot(0);
    }

    @Nonnull
    public ItemStack setItemInput(@Nonnull ItemStack stack) {
        ItemStack previous = getItemInput().copy();
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) {
            copy.setCount(1);
        }
        this.inventory.setStackInSlot(0, copy);
        this.markForUpdate();
        return previous;
    }

    @Nonnull
    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Nonnull
    public static Set<BlockPos> getLiquidOffsets() {
        return LIQUID_OFFSETS;
    }

    @Nonnull
    public Map<BlockPos, Fluid> getLiquids() {
        Map<BlockPos, Fluid> fluids = new HashMap<>();
        if (level == null) {
            return fluids;
        }
        for (BlockPos offset : getLiquidOffsets()) {
            fluids.put(offset, level.getFluidState(getBlockPos().offset(offset)).getType());
        }
        return fluids;
    }

    @Nonnull
    public Vec3 getRandomInfuserOffset() {
        Vec3 vec = Vec3.atLowerCornerOf(getBlockPos()).add(0, 0.8, 0);
        if (level == null) {
            return vec.add(0.5, 0, 0.5);
        }
        return switch (level.random.nextInt(4)) {
            case 3 -> vec.add(0.5, 0, 0.875);
            case 2 -> vec.add(0.5, 0, 0.125);
            case 1 -> vec.add(0.125, 0, 0.5);
            default -> vec.add(0.875, 0, 0.5);
        };
    }

    public void dropItemOnTopNatural(ItemStack stack) {
        if (level != null && !level.isClientSide && !stack.isEmpty()) {
            Vec3 vec = Vec3.atCenterOf(getBlockPos());
            ItemUtils.dropItemNaturally(level, vec.x, vec.y + 1, vec.z, stack);
        }
    }

    public void setInfusionProgress(float progress) {
        this.infusionProgress = progress;
        markForUpdate();
    }

    public void addKnownRecipe(ResourceLocation recipeId) {
        this.knownRecipes.add(recipeId);
        markForUpdate();
    }

    @Nonnull
    public Set<ResourceLocation> getKnownRecipes() {
        return Collections.unmodifiableSet(knownRecipes);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return getCachedCapability(new CapabilityKey(capability, side), () -> inventory).cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    protected void writeCustomNBT(CompoundTag tag) {
        //Required now that this extends TileEntityTick: the base writes ticksExisted / doesSeeSky /
        //hasMultiblock here, and hasMultiblock() reads the synced copy verbatim on the client.
        super.writeCustomNBT(tag);
        tag.putFloat(TAG_INFUSION_PROGRESS, infusionProgress);
        tag.putBoolean(TAG_IS_INFUSING, isInfusing);
        tag.put(TAG_INVENTORY, inventory.serializeNBT());
        ListTag known = new ListTag();
        for (ResourceLocation recipe : knownRecipes) {
            known.add(StringTag.valueOf(recipe.toString()));
        }
        tag.put(TAG_KNOWN_RECIPES, known);
        if (activeRecipe != null) {
            tag.put(TAG_ACTIVE_RECIPE, activeRecipe.serialize());
        }
    }

    @Override
    protected void readCustomNBT(CompoundTag tag) {
        //Mirrors writeCustomNBT above. On an existing save these three keys are simply absent and the base's
        //getInt/getBoolean yield 0/false, which are the correct initial values - so this is additive and old
        //infuser tiles load unchanged.
        super.readCustomNBT(tag);
        infusionProgress = tag.getFloat(TAG_INFUSION_PROGRESS);
        isInfusing = tag.getBoolean(TAG_IS_INFUSING);
        if (tag.contains(TAG_INVENTORY)) {
            inventory.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        }
        knownRecipes.clear();
        if (tag.contains(TAG_KNOWN_RECIPES, Tag.TAG_LIST)) {
            ListTag known = tag.getList(TAG_KNOWN_RECIPES, Tag.TAG_STRING);
            for (int i = 0; i < known.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(known.getString(i));
                if (id != null) {
                    knownRecipes.add(id);
                }
            }
        }
        if (tag.contains(TAG_ACTIVE_RECIPE, Tag.TAG_COMPOUND)) {
            if (level == null) {
                pendingActiveRecipe = tag.getCompound(TAG_ACTIVE_RECIPE).copy();
            } else {
                activeRecipe = ActiveLiquidInfusionRecipe.deserialize(tag.getCompound(TAG_ACTIVE_RECIPE), level.getRecipeManager());
                isInfusing = activeRecipe != null;
            }
        } else {
            activeRecipe = null;
            pendingActiveRecipe = null;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (pendingActiveRecipe != null && level != null) {
            activeRecipe = ActiveLiquidInfusionRecipe.deserialize(pendingActiveRecipe, level.getRecipeManager());
            isInfusing = activeRecipe != null;
            pendingActiveRecipe = null;
        }
    }

    //1.16.5 TileInfuser.java:146-168. Reached from PktPlayEffect.java:130-131 once finishRecipe above actually
    //sends the packet; the body was an empty stub, so even a delivered packet produced nothing.
    @OnlyIn(Dist.CLIENT)
    public static void finishCraftingEffects(PktPlayEffect pkt) {
        ResourceLocation recipeName = ByteBufUtils.readResourceLocation(pkt.getExtraData());
        BlockPos at = ByteBufUtils.readPos(pkt.getExtraData());

        Level world = Minecraft.getInstance().level;
        if (world == null) {
            return;
        }

        InfuserBlockEntity thisInfuser = MiscUtils.getBlockEntityAt(world, at, InfuserBlockEntity.class, false);
        if (thisInfuser != null) {
            //1.16.5: world.getRecipeManager().getRecipes(RecipeTypesAS.TYPE_INFUSION.getType()).get(recipeName).
            //1.20.1's RecipeManager exposes byKey directly; the instanceof keeps the "infusion recipes only"
            //guard the lookup-by-type gave, exactly as TileAltar#finishCraftingEffects does here.
            Recipe<?> recipe = world.getRecipeManager().byKey(recipeName).orElse(null);
            if (recipe instanceof LiquidInfusion infusion) {
                //1.16.5: FluidAttributes.BUCKET_VOLUME -> 1.20.1: FluidType.BUCKET_VOLUME
                FluidStack stack = new FluidStack(infusion.getLiquidInput(), FluidType.BUCKET_VOLUME);
                Vector3 pos = new Vector3(at).add(0.5, 1, 0.5);
                for (int i = 0; i < 30; i++) {
                    playLiquidFinish(pos, stack);
                }
            }
        }
    }

    //1.16.5 TileInfuser.java:170-184.
    @OnlyIn(Dist.CLIENT)
    private static void playLiquidFinish(Vector3 at, FluidStack stack) {
        Vector3 motion = new Vector3();
        MiscUtils.applyRandomOffset(motion, rand, 0.05F);

        Effects.of(EffectTemplatesAS.GENERIC_ATLAS_PARTICLE)
                .spawn(at)
                .setSprite(RenderingUtils.getParticleTexture(stack))
                .selectFraction(0.2F)
                .setScaleMultiplier(0.02F + rand.nextFloat() * 0.05F)
                .setMotion(motion)
                .color((fx, pTicks) -> new Color(ColorUtils.getOverlayColor(stack)))
                .alpha(VFXAlphaFunction.FADE_OUT)
                .setMaxAge(40);
    }
}
