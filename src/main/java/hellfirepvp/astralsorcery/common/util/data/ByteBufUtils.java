package hellfirepvp.astralsorcery.common.util.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hellfirepvp.astralsorcery.common.block.util.BlockStateHelper;
import hellfirepvp.astralsorcery.common.perk.source.ModifierManager;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSourceProvider;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ByteBufUtils {

    public static void writePos(FriendlyByteBuf buf, BlockPos pos) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
    }

    public static BlockPos readPos(FriendlyByteBuf buf) {
        return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void writeResourceLocation(FriendlyByteBuf buf, ResourceLocation loc) {
        buf.writeUtf(loc.toString());
    }

    public static ResourceLocation readResourceLocation(FriendlyByteBuf buf) {
        return ResourceLocation.parse(buf.readUtf());
    }

    /**
     * Encodes a modifier source together with the provider-specific payload
     * needed to reconstruct it on the client.
     */
    public static void writeModifierSource(FriendlyByteBuf buf, ModifierSource source) {
        ResourceLocation providerName = source.getProviderName();
        writeResourceLocation(buf, providerName);

        ModifierSourceProvider<?> provider = ModifierManager.getProvider(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown modifier source provider: " + providerName);
        }
        writeProviderPayload(provider, source, buf);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void writeProviderPayload(ModifierSourceProvider provider, ModifierSource source, FriendlyByteBuf buf) {
        provider.serialize(source, buf);
    }

    public static ModifierSource readModifierSource(FriendlyByteBuf buf) {
        ResourceLocation providerName = readResourceLocation(buf);
        ModifierSourceProvider<?> provider = ModifierManager.getProvider(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("Unknown modifier source provider: " + providerName);
        }
        return provider.deserialize(buf);
    }

    public static void writeCompound(FriendlyByteBuf buf, CompoundTag tag) {
        buf.writeNbt(tag);
    }

    @Nullable
    public static CompoundTag readCompound(FriendlyByteBuf buf) {
        return buf.readNbt();
    }

    public static void writeUUID(FriendlyByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID readUUID(FriendlyByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeString(FriendlyByteBuf buf, String toWrite) {
        buf.writeUtf(toWrite);
    }

    public static String readString(FriendlyByteBuf buf) {
        return buf.readUtf();
    }

    public static void writeTextComponent(FriendlyByteBuf buf, Component cmp) {
        writeString(buf, Component.Serializer.toJson(cmp));
    }

    public static MutableComponent readTextComponent(FriendlyByteBuf buf) {
        return Component.Serializer.fromJson(readString(buf));
    }

    public static void writeItemStack(FriendlyByteBuf buf, net.minecraft.world.item.ItemStack stack, boolean writeSize) {
        buf.writeItem(stack);
    }

    public static net.minecraft.world.item.ItemStack readItemStack(FriendlyByteBuf buf, boolean writeSize) {
        return buf.readItem();
    }

    public static void writeItemStack(FriendlyByteBuf buf, net.minecraft.world.item.ItemStack stack) {
        buf.writeItem(stack);
    }

    public static net.minecraft.world.item.ItemStack readItemStack(FriendlyByteBuf buf) {
        return buf.readItem();
    }

    public static void writeBlockState(FriendlyByteBuf buf, BlockState state) {
        writeString(buf, BlockStateHelper.serialize(state));
    }

    public static BlockState readBlockState(FriendlyByteBuf buf) {
        return BlockStateHelper.deserialize(readString(buf));
    }

    @Nullable
    public static <T> T readOptional(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> readFct) {
        if (buf.readBoolean()) {
            return readFct.apply(buf);
        }
        return null;
    }

    public static <T> void writeOptional(FriendlyByteBuf buf, @Nullable T object, BiConsumer<FriendlyByteBuf, T> applyFct) {
        buf.writeBoolean(object != null);
        if (object != null) {
            applyFct.accept(buf, object);
        }
    }

    public static <T> void writeCollection(FriendlyByteBuf buf, @Nullable Collection<T> list, BiConsumer<FriendlyByteBuf, T> iterationFct) {
        if (list != null) {
            buf.writeInt(list.size());
            list.forEach(e -> iterationFct.accept(buf, e));
        } else {
            buf.writeInt(-1);
        }
    }

    @Nullable
    public static <T> List<T> readList(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> readFct) {
        int size = buf.readInt();
        if (size == -1) return null;
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readFct.apply(buf));
        }
        return list;
    }

    public static <T extends Enum<T>> void writeEnumValue(FriendlyByteBuf buf, T value) {
        buf.writeInt(value.ordinal());
    }

    public static <T extends Enum<T>> T readEnumValue(FriendlyByteBuf buf, Class<T> enumClazz) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("Passed class is not an enum!");
        }
        return enumClazz.getEnumConstants()[buf.readInt()];
    }

    public static <T extends Enum<T>> void writeEnum(FriendlyByteBuf buf, T value) {
        writeEnumValue(buf, value);
    }

    public static <T extends Enum<T>> T readEnum(FriendlyByteBuf buf, Class<T> enumClazz) {
        return readEnumValue(buf, enumClazz);
    }

    public static void writeVanillaRegistryEntry(FriendlyByteBuf buf, ResourceKey<?> key) {
        writeResourceLocation(buf, key.registry());
        writeResourceLocation(buf, key.location());
    }

    public static <T> ResourceKey<T> readVanillaRegistryEntry(FriendlyByteBuf buf) {
        ResourceLocation registryName = readResourceLocation(buf);
        ResourceLocation location = readResourceLocation(buf);
        return ResourceKey.create(ResourceKey.createRegistryKey(registryName), location);
    }

    public static void writeVector(FriendlyByteBuf buf, Vector3 vec) {
        buf.writeDouble(vec.getX());
        buf.writeDouble(vec.getY());
        buf.writeDouble(vec.getZ());
    }

    public static Vector3 readVector(FriendlyByteBuf buf) {
        return new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static <K, V> void writeMap(FriendlyByteBuf buf,
                                       @Nullable Map<K, V> map,
                                       BiConsumer<FriendlyByteBuf, K> keySerializer,
                                       BiConsumer<FriendlyByteBuf, V> valueSerializer) {
        if (map != null) {
            buf.writeInt(map.size());
            for (Map.Entry<K, V> entry : map.entrySet()) {
                keySerializer.accept(buf, entry.getKey());
                valueSerializer.accept(buf, entry.getValue());
            }
        } else {
            buf.writeInt(-1);
        }
    }

    @Nullable
    public static <K, V> Map<K, V> readMap(FriendlyByteBuf buf,
                                           Function<FriendlyByteBuf, K> readKey,
                                           Function<FriendlyByteBuf, V> readValue) {
        int size = buf.readInt();
        if (size == -1) {
            return null;
        }
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(readKey.apply(buf), readValue.apply(buf));
        }
        return map;
    }

    public static void writeJsonObject(FriendlyByteBuf buf, JsonObject object) {
        writeString(buf, object.toString());
    }

    public static JsonObject readJsonObject(FriendlyByteBuf buf) {
        return JsonParser.parseString(readString(buf)).getAsJsonObject();
    }

    public static void writeFluidStack(FriendlyByteBuf byteBuf, @Nonnull FluidStack stack) {
        stack.writeToPacket(byteBuf);
    }

    @Nonnull
    public static FluidStack readFluidStack(FriendlyByteBuf byteBuf) {
        return FluidStack.readFromPacket(byteBuf);
    }

    //1.16.5 CompressedStreamTools -> 1.20.1 NbtIo
    public static void writeNBTTag(FriendlyByteBuf byteBuf, @Nonnull CompoundTag tag) {
        try (DataOutputStream dos = new DataOutputStream(new ByteBufOutputStream(byteBuf))) {
            NbtIo.write(tag, dos);
        } catch (Exception exc) {}
    }

    @Nonnull
    public static CompoundTag readNBTTag(FriendlyByteBuf byteBuf) {
        try (DataInputStream dis = new DataInputStream(new ByteBufInputStream(byteBuf))) {
            return NbtIo.read(dis);
        } catch (Exception exc) {}
        throw new IllegalStateException("Could not load NBT Tag from incoming byte buffer!");
    }

}
