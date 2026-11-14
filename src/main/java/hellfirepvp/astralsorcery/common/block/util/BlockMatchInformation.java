package hellfirepvp.astralsorcery.common.block.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import hellfirepvp.astralsorcery.common.item.util.ItemUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Predicate;

public class BlockMatchInformation implements Predicate<BlockState> {

    //Not final: for the tag form this starts out empty and is filled in by resolveDisplay() on first use.
    private ItemStack display;

    private BlockState matchState;
    private boolean matchExact;

    private TagKey<Block> matchTag;
    private ResourceLocation matchTagKey;

    public BlockMatchInformation(TagKey<Block> matchTag) {
        this(matchTag, ItemStack.EMPTY);
    }

    //No empty-display check on the tag form. Block tags are bound only once the whole datapack reload has
    //finished (MinecraftServer#reloadResources -> ReloadableServerResources#updateRegistryTags), which is
    //strictly after RecipeManager has parsed this recipe, so BuiltInRegistries.BLOCK#getTag cannot succeed at
    //parse time on 1.20.1 - 1.16.5 could, because it read the live TagCollectionManager. Vanilla has the same
    //constraint and solves it the same way: Ingredient.TagValue keeps the TagKey and resolves it on first use.
    //Throwing here instead killed the whole recipe - astralsorcery:block_transmutation/iron_starmetal failed with
    //"unable to create valid itemstack from block tag forge:ores/iron!" and never loaded.
    public BlockMatchInformation(TagKey<Block> matchTag, ItemStack display) {
        this.matchTag = matchTag;
        this.matchTagKey = matchTag.location();
        this.display = display;
    }

    public BlockMatchInformation(BlockState matchState, boolean matchExact) {
        this(matchState, ItemUtils.createBlockStack(matchState), matchExact);
    }

    public BlockMatchInformation(BlockState matchState, ItemStack display, boolean matchExact) {
        this.matchState = matchState;
        this.display = display;
        this.matchExact = matchExact;

        if (this.display.isEmpty()) {
            throw new IllegalArgumentException("No display ItemStack passed, and " + ForgeRegistries.BLOCKS.getKey(matchState.getBlock()) + " has no associated ItemBlock!");
        }
    }

    private static ItemStack createDisplayStack(TagKey<Block> blockTag) {
        Optional<net.minecraft.core.HolderSet.Named<Block>> tag = BuiltInRegistries.BLOCK.getTag(blockTag);
        if (tag.isPresent()) {
            for (net.minecraft.core.Holder<Block> holder : tag.get()) {
                ItemStack blockStack = ItemUtils.createBlockStack(holder.value().defaultBlockState());
                if (!blockStack.isEmpty()) {
                    return blockStack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isValid() {
        if (this.matchState != null && this.matchState.getBlock() instanceof AirBlock) {
            return false;
        }
        return true;
    }

    //Resolves - and then caches - the display stack of the tag form. Safe to call repeatedly before the tags are
    //bound: it simply keeps returning EMPTY until the tag has contents.
    @Nonnull
    private ItemStack resolveDisplay() {
        if (this.display != null && !this.display.isEmpty()) {
            return this.display;
        }
        if (this.matchTag != null) {
            ItemStack resolved = createDisplayStack(this.matchTag);
            if (!resolved.isEmpty()) {
                this.display = resolved;
            }
            return resolved;
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public ItemStack getDisplayStack() {
        return this.resolveDisplay().copy();
    }

    @Override
    public boolean test(BlockState state) {
        if (this.matchState != null) {
            return this.matchExact ? BlockUtils.matchStateExact(state, this.matchState) : state.is(this.matchState.getBlock());
        }
        if (this.matchTag != null) {
            return state.is(this.matchTag);
        }
        return false;
    }

    public static BlockMatchInformation read(JsonObject object) {
        if (object.has("block")) {
            BlockState state = BlockStateHelper.deserializeObject(object);
            boolean fullyDefined = !BlockStateHelper.isMissingStateInformation(object);
            ItemStack display = new ItemStack(state.getBlock());
            if (object.has("display")) {
                display = JsonHelper.getItemStack(object, "display");
            }
            return new BlockMatchInformation(state, display, fullyDefined);
        } else if (object.has("tag")) {
            TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(object.get("tag").getAsString()));
            if (object.has("display")) {
                ItemStack display = JsonHelper.getItemStack(object, "display");
                return new BlockMatchInformation(blockTag, display);
            }
            return new BlockMatchInformation(blockTag);
        }
        throw new JsonSyntaxException("Neither block nor tag defined for block transmutation match information!");
    }

    public JsonObject serializeJson() {
        JsonObject out = new JsonObject();
        if (this.matchState != null) {
            BlockStateHelper.serializeObject(out, this.matchState, this.matchExact);
            out.add("display", JsonHelper.serializeItemStack(this.getDisplayStack()));
        } else if (this.matchTag != null) {
            out.add("tag", new JsonPrimitive(this.matchTagKey.toString()));
        }
        return out;
    }

    public static BlockMatchInformation read(FriendlyByteBuf buf) {
        int type = buf.readInt();
        ItemStack display = ByteBufUtils.readItemStack(buf);
        switch (type) {
            case 0:
                BlockState state = ByteBufUtils.readBlockState(buf);
                boolean exactMatch = buf.readBoolean();
                return new BlockMatchInformation(state, display, exactMatch);
            case 1:
                String tagName = ByteBufUtils.readString(buf);
                TagKey<Block> blockTag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(tagName));
                return new BlockMatchInformation(blockTag, display);
        }
        throw new IllegalArgumentException("Unknown block transmutation match type: " + type);
    }

    public void serialize(FriendlyByteBuf buf) {
        int type = this.matchState != null ? 0 : 1;
        buf.writeInt(type);
        //resolveDisplay(), not the raw field: recipes are synced to clients on login, long after the tags bound,
        //so the tag form can and must send a real display stack over the wire.
        ByteBufUtils.writeItemStack(buf, this.resolveDisplay());

        switch (type) {
            case 0:
                ByteBufUtils.writeBlockState(buf, this.matchState);
                buf.writeBoolean(this.matchExact);
                break;
            case 1:
                ByteBufUtils.writeString(buf, this.matchTagKey.toString());
                break;
        }
    }
}
