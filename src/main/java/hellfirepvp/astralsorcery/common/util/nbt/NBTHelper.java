/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.nbt;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTHelper
 * Created by HellFirePvP
 * Date: 07.05.2016 / 02:15
 */
public class NBTHelper {

    @Nonnull
    public static CompoundTag getPersistentData(Entity entity) {
        return getPersistentData(entity.getPersistentData());
    }

    @Nonnull
    public static CompoundTag getPersistentData(ItemStack item) {
        return getPersistentData(getData(item));
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

    public static boolean hasPersistentData(Entity entity) {
        return hasPersistentData(entity.getPersistentData());
    }

    public static boolean hasPersistentData(ItemStack item) {
        return item.hasTag() && hasPersistentData(item.getTag());
    }

    public static boolean hasPersistentData(CompoundTag base) {
        return base.contains(AstralSorcery.MODID) && base.get(AstralSorcery.MODID) instanceof CompoundTag;
    }


    public static void removePersistentData(Entity entity) {
        removePersistentData(entity.getPersistentData());
    }

    public static void removePersistentData(ItemStack item) {
        if (item.hasTag()) {
            removePersistentData(item.getTag());
        }
    }

    public static void removePersistentData(CompoundTag base) {
        base.remove(AstralSorcery.MODID);
    }

    // 1.16.5 used Forge's Constants.NBT.TAG_* constants; 1.20.1 exposes the same numeric ids as Tag.TAG_*.
    public static void deepMerge(CompoundTag dst, CompoundTag src, boolean uniqueArrayEntries) {
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
    }

    //Stupid NBT stuff ahead. the iterator and the actual .get returns from 2 different lists.
    //Don't use the iterator on ListTag...
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

    public static CompoundTag getData(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null) {
            compound = new CompoundTag();
            stack.setTag(compound);
        }
        return compound;
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
        // 1.16.5 read Block#getRegistryName(); registry names left the entries in 1.20.1, so the
        // key has to be looked up in the registry itself.
        if (ForgeRegistries.BLOCKS.getKey(state.getBlock()) == null) {
            state = Blocks.AIR.defaultBlockState();
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("registryName", ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString());
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
    public static <T extends Comparable<T>> BlockState getBlockStateFromTag(CompoundTag cmp, BlockState _default) {
        ResourceLocation key = ResourceLocation.tryParse(cmp.getString("registryName"));
        if (key == null) return _default;
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if (block == null || block == Blocks.AIR) return _default;
        BlockState state = block.defaultBlockState();
        Collection<Property<?>> properties = state.getProperties();
        ListTag list = cmp.getList("properties", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag propertyTag = list.getCompound(i);
            String valueStr = propertyTag.getString("value");
            String propertyStr = propertyTag.getString("property");
            Property<T> match = (Property<T>) MiscUtils.iterativeSearch(properties, prop -> prop.getName().equalsIgnoreCase(propertyStr));
            if (match != null) {
                try {
                    // 1.16.5: Property#parseValue(String) -> Optional<T>; 1.20.1 renamed it to getValue(String).
                    Optional<T> opt = match.getValue(valueStr);
                    if (opt.isPresent()) {
                        state = state.setValue(match, opt.get());
                    }
                } catch (Throwable tr) {} // Thanks Exu2
            }
        }
        return state;
    }

    public static void setAsSubTag(CompoundTag compound, String tag, Consumer<CompoundTag> applyFct) {
        CompoundTag newTag = new CompoundTag();
        applyFct.accept(newTag);
        compound.put(tag, newTag);
    }

    @Nullable
    public static <T> T readFromSubTag(CompoundTag compound, String tag, Function<CompoundTag, T> readFct) {
        if (compound.contains(tag, Tag.TAG_COMPOUND)) {
            return readFct.apply(compound.getCompound(tag));
        }
        return null;
    }

    //TODO_PORT[AS-NBT-01]: 1.16.5 derived the owning registry from the entry itself via
    // IForgeRegistryEntry#getRegistryType(). That interface no longer exists in 1.20.1 and there is no
    // reverse entry -> registry lookup, so the registry has to be supplied by the caller.
    // (1.16.5 common/util/nbt/NBTHelper.java:354)
    public static <T> void setRegistryEntry(CompoundTag compoundNBT, String tag, IForgeRegistry<T> registry, T entry) {
        setResourceLocation(compoundNBT, tag + "_registry", registry.getRegistryName());
        setResourceLocation(compoundNBT, tag, registry.getKey(entry));
    }

    @Nullable
    public static <T> T getRegistryEntry(CompoundTag compoundNBT, String tag) {
        ResourceLocation registryName = getResourceLocation(compoundNBT, tag + "_registry");
        if (registryName != null) {
            ForgeRegistry<T> registry = RegistryManager.ACTIVE.getRegistry(registryName);
            if (registry != null) {
                ResourceLocation key = getResourceLocation(compoundNBT, tag);
                if (key != null) {
                    return registry.getValue(key);
                }
            }
        }
        return null;
    }

    public static void setResourceLocation(CompoundTag compoundNBT, String tag, ResourceLocation key) {
        compoundNBT.putString(tag, key.toString());
    }

    @Nullable
    public static ResourceLocation getResourceLocation(CompoundTag compoundNBT, String tag) {
        if (compoundNBT.contains(tag)) {
            return ResourceLocation.tryParse(compoundNBT.getString(tag));
        }
        return null;
    }

    public static void setStack(CompoundTag compound, String tag, ItemStack stack) {
        setAsSubTag(compound, tag, stack::save);
    }

    public static ItemStack getStack(CompoundTag compound, String tag) {
        return ObjectUtils.firstNonNull(readFromSubTag(compound, tag, ItemStack::of), ItemStack.EMPTY);
    }

    public static void setFluid(CompoundTag compound, String tag, FluidStack stack) {
        setAsSubTag(compound, tag, stack::writeToNBT);
    }

    public static FluidStack getFluid(CompoundTag compound, String tag) {
        return ObjectUtils.firstNonNull(readFromSubTag(compound, tag, FluidStack::loadFluidStackFromNBT), FluidStack.EMPTY);
    }

    public static void removeUUID(CompoundTag compound, String key) {
        compound.remove(key);
    }

    public static UUID getUUID(CompoundTag compoundNBT, String key, UUID _default) {
        // 1.16.5 CompoundNBT#hasUniqueId/getUniqueId; renamed to hasUUID/getUUID in 1.20.1.
        if (compoundNBT.hasUUID(key)) {
            return compoundNBT.getUUID(key);
        }
        return _default;
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
}
