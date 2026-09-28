package hellfirepvp.astralsorcery.common.util.data;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO_PORT[AS-PKG-01]: 1.16.5 has a single NBTHelper at common/util/nbt/NBTHelper.java. This target tree
// carries a second copy in common/util/data/; both are kept in sync until the package divergence is resolved.
public class NBTHelper {

    @Nonnull
    public static CompoundTag getPersistentData(ItemStack stack) {
        return getPersistentData(getData(stack));
    }

    @Nonnull
    public static CompoundTag getPersistentData(net.minecraft.world.entity.Entity entity) {
        return getPersistentData(entity.getPersistentData());
    }

    @Nonnull
    public static CompoundTag getPersistentData(CompoundTag base) {
        CompoundTag compound;
        if (hasPersistentData(base)) {
            compound = base.getCompound(AstralSorcery.MODID);
        } else {
            compound = new CompoundTag();
            base.put(AstralSorcery.MODID, compound);
        }
        return compound;
    }

    public static boolean hasPersistentData(ItemStack stack) {
        return stack.hasTag() && hasPersistentData(stack.getTag());
    }

    public static boolean hasPersistentData(net.minecraft.world.entity.Entity entity) {
        return hasPersistentData(entity.getPersistentData());
    }

    public static boolean hasPersistentData(CompoundTag base) {
        return base.contains(AstralSorcery.MODID) && base.get(AstralSorcery.MODID) instanceof CompoundTag;
    }

    public static void removePersistentData(ItemStack stack) {
        if (stack.hasTag()) {
            removePersistentData(stack.getTag());
        }
    }

    public static void removePersistentData(net.minecraft.world.entity.Entity entity) {
        removePersistentData(entity.getPersistentData());
    }

    public static void removePersistentData(CompoundTag base) {
        base.remove(AstralSorcery.MODID);
    }

    @Nonnull
    public static CompoundTag getData(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null) {
            compound = new CompoundTag();
            stack.setTag(compound);
        }
        return compound;
    }

    public static void setResourceLocation(CompoundTag compoundNBT, String tag, ResourceLocation key) {
        compoundNBT.putString(tag, key.toString());
    }

    @Nullable
    public static ResourceLocation getResourceLocation(CompoundTag compoundNBT, String tag) {
        if (compoundNBT.contains(tag)) {
            return ResourceLocation.parse(compoundNBT.getString(tag));
        }
        return null;
    }

    public static CompoundTag writeBlockPosToNBT(BlockPos pos, CompoundTag compound) {
        compound.putInt("bposX", pos.getX());
        compound.putInt("bposY", pos.getY());
        compound.putInt("bposZ", pos.getZ());
        return compound;
    }

    public static BlockPos readBlockPosFromNBT(CompoundTag compound) {
        int x = compound.getInt("bposX");
        int y = compound.getInt("bposY");
        int z = compound.getInt("bposZ");
        return new BlockPos(x, y, z);
    }

    public static CompoundTag deepMerge(CompoundTag dst, CompoundTag src, boolean uniqueArrayEntries) {
        for (String s : src.getAllKeys()) {
            Tag nbtElement = src.get(s);
            if (nbtElement.getId() == Tag.TAG_COMPOUND) {
                if (dst.contains(s, Tag.TAG_COMPOUND)) {
                    deepMerge(dst.getCompound(s), (CompoundTag) nbtElement, uniqueArrayEntries);
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Tag.TAG_LIST) {
                if (dst.contains(s, Tag.TAG_LIST)) {
                    ListTag dstList = (ListTag) dst.get(s);
                    ListTag srcList = (ListTag) nbtElement;
                    if (dstList.getElementType() == srcList.getElementType()) {
                        deepMergeList(dstList, srcList);
                    } else {
                        dst.put(s, srcList.copy());
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Tag.TAG_INT_ARRAY) {
                if (dst.contains(s, Tag.TAG_INT_ARRAY)) {
                    IntArrayTag dstArr = (IntArrayTag) dst.get(s);
                    IntArrayTag srcArr = (IntArrayTag) nbtElement;
                    if (uniqueArrayEntries) {
                        for (IntTag element : srcArr) {
                            if (!dstArr.contains(element)) {
                                dstArr.add(element);
                            }
                        }
                    } else {
                        dstArr.addAll(srcArr);
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Tag.TAG_LONG_ARRAY) {
                if (dst.contains(s, Tag.TAG_LONG_ARRAY)) {
                    LongArrayTag dstArr = (LongArrayTag) dst.get(s);
                    LongArrayTag srcArr = (LongArrayTag) nbtElement;
                    if (uniqueArrayEntries) {
                        for (LongTag element : srcArr) {
                            if (!dstArr.contains(element)) {
                                dstArr.add(element);
                            }
                        }
                    } else {
                        dstArr.addAll(srcArr);
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else if (nbtElement.getId() == Tag.TAG_BYTE_ARRAY) {
                if (dst.contains(s, Tag.TAG_BYTE_ARRAY)) {
                    ByteArrayTag dstArr = (ByteArrayTag) dst.get(s);
                    ByteArrayTag srcArr = (ByteArrayTag) nbtElement;
                    if (uniqueArrayEntries) {
                        for (ByteTag element : srcArr) {
                            if (!dstArr.contains(element)) {
                                dstArr.add(element);
                            }
                        }
                    } else {
                        dstArr.addAll(srcArr);
                    }
                } else {
                    dst.put(s, nbtElement.copy());
                }
            } else {
                dst.put(s, nbtElement.copy());
            }
        }
        return dst;
    }

    private static void deepMergeList(ListTag dst, ListTag src) {
        for (int j = 0; j < src.size(); j++) {
            Tag toAdd = src.get(j);
            boolean found = false;
            for (int i = 0; i < dst.size(); i++) {
                Tag existing = dst.get(i);
                if (existing.equals(toAdd)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                dst.add(toAdd.copy());
            }
        }
    }

    @Nonnull
    public static <E, N extends Tag> List<E> readList(CompoundTag nbt, String key, int type, Function<N, E> deserializer) {
        if (!nbt.contains(key, Tag.TAG_LIST)) {
            return new ArrayList<>();
        }
        return readList(nbt.getList(key, type), deserializer);
    }

    @Nonnull
    public static <E, N extends Tag> List<E> readList(ListTag nbt, Function<N, E> deserializer) {
        return nbt.stream()
                .map(n -> deserializer.apply((N) n))
                .collect(Collectors.toList());
    }

    @Nonnull
    public static <E, N extends Tag> Set<E> readSet(CompoundTag nbt, String key, int type, Function<N, E> deserializer) {
        if (!nbt.contains(key, Tag.TAG_LIST)) {
            return new HashSet<>();
        }
        return readSet(nbt.getList(key, type), deserializer);
    }

    @Nonnull
    public static <E, N extends Tag> Set<E> readSet(ListTag nbt, Function<N, E> deserializer) {
        return nbt.stream()
                .map(n -> deserializer.apply((N) n))
                .collect(Collectors.toSet());
    }

    public static <E> void writeList(CompoundTag tag, String key, Collection<E> collection, Function<E, Tag> serializer) {
        tag.put(key, writeList(collection, serializer));
    }

    public static <E> ListTag writeList(Collection<E> collection, Function<E, Tag> serializer) {
        ListTag nbt = new ListTag();
        nbt.addAll(collection.stream()
                .map(serializer)
                .collect(Collectors.toList()));
        return nbt;
    }

    public static <T> void writeOptional(CompoundTag nbt, String key, @Nullable T object, BiConsumer<CompoundTag, T> writer) {
        nbt.putBoolean(key + "_present", object != null);
        if (object != null) {
            CompoundTag write = new CompoundTag();
            writer.accept(write, object);
            nbt.put(key, write);
        }
    }

    @Nullable
    public static <T> T readOptional(CompoundTag nbt, String key, Function<CompoundTag, T> reader) {
        return readOptional(nbt, key, reader, null);
    }

    @Nullable
    public static <T> T readOptional(CompoundTag nbt, String key, Function<CompoundTag, T> reader, T _default) {
        if (nbt.getBoolean(key + "_present")) {
            CompoundTag read = nbt.getCompound(key);
            return reader.apply(read);
        }
        return _default;
    }

    public static <T extends Enum<T>> void writeEnum(CompoundTag nbt, String key, T enumValue) {
        nbt.putInt(key, enumValue.ordinal());
    }

    public static <T extends Enum<T>> T readEnum(CompoundTag nbt, String key, Class<T> enumClazz) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("Passed class is not an enum!");
        }
        return enumClazz.getEnumConstants()[nbt.getInt(key)];
    }

    public static void setBlockState(CompoundTag cmp, String key, BlockState state) {
        CompoundTag serialized = getBlockStateNBTTag(state);
        cmp.put(key, serialized);
    }

    @Nullable
    public static BlockState getBlockState(CompoundTag cmp, String key) {
        return getBlockStateFromTag(cmp.getCompound(key));
    }

    @Nonnull
    public static CompoundTag getBlockStateNBTTag(BlockState state) {
        if (state.getBlock().builtInRegistryHolder().key().location() == null) {
            state = Blocks.AIR.defaultBlockState();
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("registryName", state.getBlock().builtInRegistryHolder().key().location().toString());
        ListTag properties = new ListTag();
        for (Property property : state.getProperties()) {
            CompoundTag propTag = new CompoundTag();
            try {
                propTag.putString("value", property.getName(state.getValue(property)));
            } catch (Exception exc) {
                continue;
            }
            propTag.putString("property", property.getName());
            properties.add(propTag);
        }
        tag.put("properties", properties);
        return tag;
    }

    @Nullable
    public static BlockState getBlockStateFromTag(CompoundTag cmp) {
        return getBlockStateFromTag(cmp, null);
    }

    @Nullable
    public static BlockState getBlockStateFromTag(CompoundTag cmp, BlockState _default) {
        ResourceLocation key = ResourceLocation.parse(cmp.getString("registryName"));
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if (block == null || block == Blocks.AIR) return _default;
        BlockState state = block.defaultBlockState();
        java.util.Collection<Property> properties = (java.util.Collection) state.getProperties();
        ListTag list = cmp.getList("properties", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag propertyTag = list.getCompound(i);
            String valueStr = propertyTag.getString("value");
            String propertyStr = propertyTag.getString("property");
            for (Property prop : properties) {
                if (prop.getName().equalsIgnoreCase(propertyStr)) {
                    try {
                        Optional<?> opt = prop.getValue(valueStr);
                        if (opt.isPresent()) {
                            state = state.setValue(prop, (Comparable) opt.get());
                        }
                    } catch (Throwable ignored) {}
                    break;
                }
            }
        }
        return state;
    }

    public static void writeOptionalSubTag(CompoundTag compound, String tag, Consumer<CompoundTag> applyFct) {
        CompoundTag newTag = new CompoundTag();
        applyFct.accept(newTag);
        compound.put(tag, newTag);
    }

    @Nullable
    public static <T> T readOptionalSubTag(CompoundTag compound, String tag, Function<CompoundTag, T> readFct) {
        if (compound.contains(tag, Tag.TAG_COMPOUND)) {
            return readFct.apply(compound.getCompound(tag));
        }
        return null;
    }

    public static <T> void setRegistryEntry(CompoundTag compoundNBT, String tag, T entry, java.util.function.Function<T, ResourceLocation> keyFn, java.util.function.Supplier<ResourceLocation> registryKeyFn) {
        setResourceLocation(compoundNBT, tag + "_registry", registryKeyFn.get());
        setResourceLocation(compoundNBT, tag, keyFn.apply(entry));
    }

    @Nullable
    public static <T> T getRegistryEntry(CompoundTag compoundNBT, String tag, java.util.function.Function<ResourceLocation, T> valueFn, java.util.function.Function<ResourceLocation, IForgeRegistry<T>> registryFn) {
        ResourceLocation registryName = getResourceLocation(compoundNBT, tag + "_registry");
        if (registryName != null) {
            IForgeRegistry<T> registry = registryFn.apply(registryName);
            if (registry != null) {
                ResourceLocation key = getResourceLocation(compoundNBT, tag);
                if (key != null) {
                    return registry.getValue(key);
                }
            }
        }
        return null;
    }

    public static void setStack(CompoundTag compound, String tag, ItemStack stack) {
        writeOptionalSubTag(compound, tag, stack::save);
    }

    public static ItemStack getStack(CompoundTag compound, String tag) {
        ItemStack result = readOptionalSubTag(compound, tag, ItemStack::of);
        return result == null ? ItemStack.EMPTY : result;
    }

    public static void setFluid(CompoundTag compound, String tag, FluidStack stack) {
        writeOptionalSubTag(compound, tag, stack::writeToNBT);
    }

    public static FluidStack getFluid(CompoundTag compound, String tag) {
        FluidStack result = readOptionalSubTag(compound, tag, FluidStack::loadFluidStackFromNBT);
        return result == null ? FluidStack.EMPTY : result;
    }

    public static CompoundTag writeVector3(Vector3 v) {
        CompoundTag cmp = new CompoundTag();
        writeVector3(v, cmp);
        return cmp;
    }

    public static CompoundTag writeVector3(Vector3 v, CompoundTag compound) {
        compound.putDouble("vecPosX", v.getX());
        compound.putDouble("vecPosY", v.getY());
        compound.putDouble("vecPosZ", v.getZ());
        return compound;
    }

    public static Vector3 readVector3(CompoundTag compound) {
        return new Vector3(
                compound.getDouble("vecPosX"),
                compound.getDouble("vecPosY"),
                compound.getDouble("vecPosZ"));
    }

    public static CompoundTag writeBoundingBox(AABB box, CompoundTag tag) {
        tag.putDouble("boxMinX", box.minX);
        tag.putDouble("boxMinY", box.minY);
        tag.putDouble("boxMinZ", box.minZ);
        tag.putDouble("boxMaxX", box.maxX);
        tag.putDouble("boxMaxY", box.maxY);
        tag.putDouble("boxMaxZ", box.maxZ);
        return tag;
    }

    public static AABB readBoundingBox(CompoundTag tag) {
        return new AABB(
                tag.getDouble("boxMinX"),
                tag.getDouble("boxMinY"),
                tag.getDouble("boxMinZ"),
                tag.getDouble("boxMaxX"),
                tag.getDouble("boxMaxY"),
                tag.getDouble("boxMaxZ"));
    }

    public static void removeUUID(CompoundTag compound, String key) {
        compound.remove(key);
    }

    public static UUID getUUID(CompoundTag compoundNBT, String key, UUID _default) {
        if (compoundNBT.hasUUID(key)) {
            return compoundNBT.getUUID(key);
        }
        return _default;
    }
}
