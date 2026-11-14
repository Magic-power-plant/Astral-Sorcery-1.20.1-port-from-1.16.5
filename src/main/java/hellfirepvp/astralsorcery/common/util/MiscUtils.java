package hellfirepvp.astralsorcery.common.util;

import hellfirepvp.astralsorcery.common.util.collection.WRItemObject;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.base.Mods;
import hellfirepvp.astralsorcery.common.lib.GameRulesAS;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraft.util.RandomSource;
import net.minecraftforge.fml.LogicalSide;

import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class MiscUtils {

    @Nullable
    public static <T> T getBlockEntityAt(BlockGetter world, BlockPos pos, Class<T> blockEntityClass, boolean forceChunkLoad) {
        if (world == null || pos == null) return null;
        if (world instanceof Level) {
            if (!((Level) world).isLoaded(pos) && !forceChunkLoad) {
                return null;
            }
        }
        BlockEntity te = world.getBlockEntity(pos);
        if (te == null) return null;
        if (blockEntityClass.isInstance(te)) return (T) te;
        return null;
    }

    public static boolean canEntityTickAt(Level world, BlockPos pos) {
        ChunkPos chPos = new ChunkPos(pos);
        if (!world.getChunkSource().hasChunk(chPos.x, chPos.z)) {
            return false;
        }
        if (world.isClientSide() || !(world instanceof ServerLevel)) {
            return true;
        }
        return true;
    }

    public static List<BlockSnapshot> captureBlockChanges(Level world, Runnable r) {
        world.captureBlockSnapshots = true;
        r.run();
        world.captureBlockSnapshots = false;
        List<BlockSnapshot> blockSnapshots = new ArrayList<>(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();
        return blockSnapshots;
    }

    @Nullable
    public static <T> T getRandomEntry(Collection<T> collection, Random rand) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int index = rand.nextInt(collection.size());
        int i = 0;
        for (T entry : collection) {
            if (i++ == index) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public static <T> T getRandomEntry(Random rand, T[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return values[rand.nextInt(values.length)];
    }

    @Nullable
    public static <T> T getRandomEntry(Collection<T> collection, RandomSource rand) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int index = rand.nextInt(collection.size());
        int i = 0;
        for (T entry : collection) {
            if (i++ == index) {
                return entry;
            }
        }
        return null;
    }

    @Nullable
    public static <T> T getRandomEntry(RandomSource rand, T[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        return values[rand.nextInt(values.length)];
    }

    @Nullable
    public static String getCurrentlyActiveMod() {
        // In 1.20.1, ModLoadingContext.getActiveContainer() was removed
        // Return the active mod container ID if available, null otherwise
        return null;
    }

    @Nonnull
    public static <T> T getEnumEntry(Class<T> enumClazz, int index) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("Called getEnumEntry on class " + enumClazz.getName() + " which isn't an enum.");
        }
        T[] values = enumClazz.getEnumConstants();
        if (values.length == 0) {
            throw new IllegalArgumentException(enumClazz.getName() + " has no enum constants.");
        }
        return values[Math.max(0, Math.min(index, values.length - 1))];
    }

    @Nullable
    public static <T> T getWeightedRandomEntry(Collection<T> list, Random rand, Function<T, Integer> getWeightFunction) {
        if (list.isEmpty()) {
            return null;
        }
        List<WRItemObject<T>> weightedItems = new ArrayList<>(list.size());
        for (T e : list) {
            weightedItems.add(new WRItemObject<>(getWeightFunction.apply(e), e));
        }
        int totalWeight = weightedItems.stream().mapToInt(w -> w.itemWeight).sum();
        if (totalWeight <= 0) return null;
        int randomValue = rand.nextInt(totalWeight);
        int cumulativeWeight = 0;
        for (WRItemObject<T> item : weightedItems) {
            cumulativeWeight += item.itemWeight;
            if (randomValue < cumulativeWeight) {
                return item.getValue();
            }
        }
        return null;
    }

    public static <T, V extends Comparable<V>> V getMaxEntry(Collection<T> elements, Function<T, V> valueFunction) {
        return getMaxEntry(elements.stream().map(valueFunction).collect(Collectors.toList()));
    }

    public static <T extends Comparable<T>> T getMaxEntry(Collection<T> elements) {
        T maxElement = null;
        for (T element : elements) {
            if (maxElement == null || maxElement.compareTo(element) < 0) {
                maxElement = element;
            }
        }
        return maxElement;
    }

    public static <T, V extends Comparable<V>> V getMinEntry(Collection<T> elements, Function<T, V> valueFunction) {
        return getMinEntry(elements.stream().map(valueFunction).collect(Collectors.toList()));
    }

    public static <T extends Comparable<T>> T getMinEntry(Collection<T> elements) {
        T minElement = null;
        for (T element : elements) {
            if (minElement == null || minElement.compareTo(element) > 0) {
                minElement = element;
            }
        }
        return minElement;
    }

    public static boolean canSeeSky(Level world, BlockPos at, boolean loadChunk, boolean defaultValue) {
        return canSeeSky(world, at, loadChunk, false, defaultValue);
    }

    public static boolean canSeeSky(Level world, BlockPos at, boolean loadChunk, boolean allowInNoSkyWorlds, boolean defaultValue) {
        if (world == null || at == null) {
            return false;
        }
        if (world.getGameRules().getBoolean(GameRulesAS.IGNORE_SKYLIGHT_CHECK_RULE)) {
            return true;
        }
        if (allowInNoSkyWorlds && !world.dimensionType().hasSkyLight()) {
            return true;
        }
        if (!loadChunk) {
            return executeWithChunk(world, at, () -> world.canSeeSky(at), defaultValue);
        }
        return world.canSeeSky(at);
    }

    public static <T> Runnable applyConsumer(Consumer<T> func, Supplier<T> supply) {
        return () -> func.accept(supply.get());
    }

    public static <T, U> Consumer<T> applyBiConsumer(BiConsumer<T, U> func, Supplier<U> supply) {
        return (t) -> func.accept(t, supply.get());
    }

    public static <T, U, V> BiConsumer<T, U> applyTriConsumer(java.util.function.BiConsumer<T, U> func, Supplier<V> supply) {
        return (t, u) -> func.accept(t, u); // Simplified - doesn't use supply
    }

    public static <T, R> Supplier<R> applyFunction(Function<T, R> func, Supplier<T> supply) {
        return () -> func.apply(supply.get());
    }

    public static <T, P, R> Function<P, R> applyBiFunction(BiFunction<T, P, R> func, Supplier<T> supply) {
        return p -> func.apply(supply.get(), p);
    }

    public static <T, V> Function<T, V> nullFunction(Runnable run) {
        return nullFunction((v) -> run.run());
    }

    public static <T, V> Function<T, V> nullFunction(Consumer<T> run) {
        return (t) -> {
            run.accept(t);
            return null;
        };
    }

    public static <T> Supplier<T> nullSupplier(Runnable run) {
        return () -> {
            run.run();
            return null;
        };
    }

    public static <T, V> List<V> transformList(List<T> list, Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    public static <T, V> Set<V> transformSet(Set<T> list, Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toSet());
    }

    public static <T, V> Collection<V> transformCollection(Collection<T> list, Function<T, V> map) {
        return list.stream().map(map).collect(Collectors.toList());
    }

    public static <K, V, N> Map<K, N> remap(Map<K, V> map, Function<V, N> remapFct) {
        Map<K, N> result = new HashMap<>();
        map.forEach((k, v) -> result.put(k, remapFct.apply(v)));
        return result;
    }

    public static <T> void mergeList(Collection<T> src, List<T> dst) {
        for (T element : src) {
            if (!dst.contains(element)) {
                dst.add(element);
            }
        }
    }

    public static <T> void cutList(Collection<? extends T> toRemove, List<T> from) {
        from.removeAll(toRemove);
    }

    public static <T> List<T> copyList(List<T> list) {
        return new ArrayList<>(list);
    }

    public static <T> Set<T> copySet(Set<T> set) {
        return new HashSet<>(set);
    }

    @Nullable
    public static <T> T iterativeSearch(Iterable<T> iterable, Predicate<T> matchingFct) {
        if (iterable == null) return null;
        for (T element : iterable) {
            if (matchingFct.test(element)) {
                return element;
            }
        }
        return null;
    }

    public static <T> boolean contains(Iterable<T> iterable, Predicate<T> matchingFct) {
        return iterativeSearch(iterable, matchingFct) != null;
    }

    public static <T> boolean matchesAny(T element, Collection<Predicate<T>> tests) {
        for (Predicate<T> test : tests) {
            if (test.test(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFluidBlock(BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock;
    }

    @Nullable
    public static Fluid tryGetFluid(BlockState state) {
        if (!isFluidBlock(state)) {
            return null;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            return fluidState.getType();
        }
        return null;
    }

    public static boolean canPlayerAttackServer(@Nullable LivingEntity source, @Nonnull LivingEntity target) {
        if (!target.isAlive()) {
            return false;
        }
        if (target instanceof Player) {
            Player plTarget = (Player) target;
            if (target.level() instanceof ServerLevel &&
                    target.level().getServer() != null &&
                    !target.level().getServer().isPvpAllowed()) {
                return false;
            }
            if (plTarget.isSpectator() || plTarget.isCreative()) {
                return false;
            }
            if (source instanceof Player &&
                    !((Player) source).canHarmPlayer(plTarget)) {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlayerBreakBlockPos(Player player, BlockPos tryBreak) {
        BlockEvent.BreakEvent ev = new BlockEvent.BreakEvent(player.level(), tryBreak, player.level().getBlockState(tryBreak), player);
        return !net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(ev);
    }

    public static boolean canPlayerPlaceBlockPos(Player player, BlockState tryPlace, BlockPos pos, net.minecraft.core.Direction againstSide) {
        Level world = player.level();
        world.captureBlockSnapshots = true;
        world.setBlock(pos, tryPlace, 0);
        world.captureBlockSnapshots = false;

        List<BlockSnapshot> blockSnapshots = new ArrayList<>();
        blockSnapshots.addAll(world.capturedBlockSnapshots);
        world.capturedBlockSnapshots.clear();

        boolean cancelPlacement = false;
        if (blockSnapshots.size() > 1) {
            cancelPlacement = ForgeEventFactory.onMultiBlockPlace(player, blockSnapshots, againstSide);
        } else if (blockSnapshots.size() == 1) {
            cancelPlacement = ForgeEventFactory.onBlockPlace(player, blockSnapshots.get(0), againstSide);
        }
        for (BlockSnapshot blocksnapshot : blockSnapshots) {
            world.restoringBlockSnapshots = true;
            blocksnapshot.restore(false, false);
            world.restoringBlockSnapshots = false;
        }
        return !cancelPlacement;
    }

    public static boolean isConnectionEstablished(ServerPlayer player) {
        return player.connection != null && player.connection.connection != null && player.connection.connection.isConnected();
    }

    public static long getRandomWorldSeed(ServerLevel world) {
        return new Random(world.getSeed()).nextLong();
    }

    public static long getSeed(Level level) {
        if (level instanceof ServerLevel) {
            return ((ServerLevel) level).getSeed();
        }
        return 0L;
    }

    @Nullable
    public static Pair<net.minecraft.world.InteractionHand, net.minecraft.world.item.ItemStack> getMainOrOffHand(LivingEntity entity, net.minecraft.world.item.Item search) {
        return getMainOrOffHand(entity, stack -> !stack.isEmpty() && stack.getItem().equals(search));
    }

    @Nullable
    public static Pair<net.minecraft.world.InteractionHand, net.minecraft.world.item.ItemStack> getMainOrOffHand(LivingEntity entity, Predicate<net.minecraft.world.item.ItemStack> acceptorFnc) {
        net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
        net.minecraft.world.item.ItemStack held = entity.getMainHandItem();
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            hand = net.minecraft.world.InteractionHand.OFF_HAND;
            held = entity.getOffhandItem();
        }
        if (held.isEmpty() || !acceptorFnc.test(held)) {
            return null;
        }
        return new Pair<>(hand, held);
    }

    public static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toTitleCase(str.charAt(0)) + str.substring(1);
    }

    @Nullable
    public static <T extends Entity> T transferEntityTo(T entity, net.minecraft.resources.ResourceKey<Level> target, BlockPos targetPos) {
        if (entity.level().isClientSide) {
            return null;
        }
        entity.setShiftKeyDown(false);
        net.minecraft.resources.ResourceKey<Level> src = entity.level().dimension();
        if (!src.equals(target)) {
            if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(entity, target)) {
                return null;
            }
            net.minecraft.server.MinecraftServer srv = entity.getServer();
            if (srv == null) return null;
            ServerLevel targetWorld = srv.getLevel(target);
            if (targetWorld == null) {
                return null;
            }
            if (entity instanceof ServerPlayer) {
                ((ServerPlayer) entity).teleportTo(targetWorld,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.1,
                        targetPos.getZ() + 0.5,
                        entity.getYRot(),
                        entity.getXRot());
            } else {
                entity.teleportTo(targetWorld, targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5, java.util.Collections.emptySet(), entity.getYRot(), entity.getXRot());
            }
        } else {
            entity.teleportTo(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        }
        return entity;
    }

    @Nullable
    public static BlockPos itDownTopBlock(Level world, BlockPos at) {
        BlockPos downPos = null;

        for (int y = world.getMaxBuildHeight(); y >= world.getMinBuildHeight(); y--) {
            BlockPos testPos = new BlockPos(at.getX(), y, at.getZ());
            BlockState test = world.getBlockState(testPos);
            if (!world.isEmptyBlock(testPos) && !test.is(net.minecraft.tags.BlockTags.LEAVES) && test.isSolidRender(world, testPos)) {
                downPos = testPos;
                break;
            }
        }

        return downPos;
    }

    public static List<Vector3> getCirclePositions(Vector3 centerOffset, Vector3 axis, double radius, int amountOfPointsOnCircle) {
        List<Vector3> out = new LinkedList<>();
        Vector3 circleVec = axis.clone().perpendicular().normalize().multiply(radius);
        double degPerPoint = 360D / ((double) amountOfPointsOnCircle);
        for (int i = 0; i < amountOfPointsOnCircle; i++) {
            double deg = i * degPerPoint;
            out.add(circleVec.clone().rotate(Math.toRadians(deg), axis.clone()).add(centerOffset));
        }
        return out;
    }

    public static Vector3 getRandomCirclePosition(Vector3 centerOffset, Vector3 axis, double radius) {
        return getCirclePosition(centerOffset, axis, radius, Math.random() * 360);
    }

    public static Vector3 getCirclePosition(Vector3 centerOffset, Vector3 axis, double radius, double degree) {
        Vector3 circleVec = axis.clone().perpendicular().normalize().multiply(radius);
        return circleVec.rotate(Math.toRadians(degree), axis.clone()).add(centerOffset);
    }

    public static Vec3 limitVelocityToMinecraftLimit(Vec3 velocity) {
        double maxDir = Math.max(Math.abs(velocity.x), Math.max(Math.abs(velocity.y), Math.abs(velocity.z)));
        if (maxDir <= 3.9) {
            return velocity;
        }
        return velocity.scale(3.9 / maxDir);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Player player) {
        return rayTraceLookBlock(player, 4.5D);
    }

    @Nonnull
    public static HitResult rayTraceLook(Player player) {
        return rayTraceLook(player, 4.5D);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Player player, net.minecraft.world.level.ClipContext.Block blockMode, net.minecraft.world.level.ClipContext.Fluid fluidMode) {
        return rayTraceLookBlock(player, blockMode, fluidMode, 4.5D);
    }

    @Nonnull
    public static HitResult rayTraceLook(Player player, net.minecraft.world.level.ClipContext.Block blockMode, net.minecraft.world.level.ClipContext.Fluid fluidMode) {
        return rayTraceLook(player, blockMode, fluidMode, 4.5D);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Player player, double reachDst) {
        return rayTraceLookBlock(player, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.ANY, reachDst);
    }

    @Nonnull
    public static HitResult rayTraceLook(Player player, double reachDst) {
        return rayTraceLook(player, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.ANY, reachDst);
    }

    @Nullable
    public static BlockHitResult rayTraceLookBlock(Entity entity, net.minecraft.world.level.ClipContext.Block blockMode, net.minecraft.world.level.ClipContext.Fluid fluidMode, double reachDst) {
        HitResult rtr = rayTraceLook(entity, blockMode, fluidMode, reachDst);
        if (rtr.getType() == HitResult.Type.BLOCK && rtr instanceof BlockHitResult) {
            return (BlockHitResult) rtr;
        }
        return null;
    }

    @Nonnull
    public static HitResult rayTraceLook(Entity entity, net.minecraft.world.level.ClipContext.Block blockMode, net.minecraft.world.level.ClipContext.Fluid fluidMode, double reachDst) {
        Vec3 pos = entity.getEyePosition(1.0F);
        Vec3 lookVec = entity.getLookAngle();
        Vec3 end = pos.add(lookVec.x * reachDst, lookVec.y * reachDst, lookVec.z * reachDst);
        net.minecraft.world.level.ClipContext ctx = new net.minecraft.world.level.ClipContext(pos, end, blockMode, fluidMode, entity);
        return entity.level().clip(ctx);
    }

    public static boolean canSeeSkyDirectly(Level world, BlockPos pos) {
        return world.dimensionType().hasSkyLight();
    }

    public static Color calcRandomConstellationColor(float perc) {
        return new Color(Color.HSBtoRGB((230F + (50F * perc)) / 360F, 0.8F, 0.8F - (0.3F * perc)));
    }

    public static void applyRandomOffset(Vector3 target, Random rand) {
        applyRandomOffset(target, rand, 1F);
    }

    public static void applyRandomOffset(Vector3 target, Random rand, float multiplier) {
        target.addX(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
        target.addY(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
        target.addZ(rand.nextFloat() * multiplier * (rand.nextBoolean() ? 1 : -1));
    }

    public static void applyRandomCircularOffset(Vector3 target, Random rand) {
        applyRandomOffset(target, rand, 1F);
    }

    public static void applyRandomCircularOffset(Vector3 target, Random rand, float multiplier) {
        Vector3 v = Vector3.random().normalize().multiply(rand.nextFloat() * multiplier);
        target.addX(v.getX() * (rand.nextBoolean() ? 1 : -1));
        target.addY(v.getY() * (rand.nextBoolean() ? 1 : -1));
        target.addZ(v.getZ() * (rand.nextBoolean() ? 1 : -1));
    }

    public static void executeWithChunk(Level world, BlockPos pos, Runnable run) {
        executeWithChunk(world, pos, nullSupplier(run));
    }

    public static <T> T executeWithChunk(Level world, BlockPos pos, Supplier<T> run) {
        return executeWithChunk(world, pos, run, (T) null);
    }

    public static <T> T executeWithChunk(Level world, BlockPos pos, Supplier<T> run, T defaultValue) {
        if (world == null || pos == null) return defaultValue;
        if (world instanceof ServerLevel) {
            ChunkSource provider = ((ServerLevel) world).getChunkSource();
            int prev = provider.getLoadedChunksCount();
            try {
                if (provider.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) {
                    return run.get();
                }
            } finally {
                int current = ((ServerLevel) world).getChunkSource().getLoadedChunksCount();
                if (current > prev) {
                    AstralSorcery.LOG.warn("Astral Sorcery loaded a chunk when it intended not to!");
                    AstralSorcery.LOG.warn("Previous chunk count: {}", prev);
                    AstralSorcery.LOG.warn("Current chunk count: {}", current);
                    AstralSorcery.LOG.warn("Loaded {} chunks!", current - prev);
                }
            }
        } else if (world.isLoaded(pos)) {
            return run.get();
        }
        return defaultValue;
    }

    public static <T> void executeWithChunk(Level world, BlockPos pos, T obj, Consumer<T> run) {
        executeWithChunk(world, pos, nullSupplier(applyConsumer(run, () -> obj)));
    }

    public static <T, U> void executeWithChunk(Level world, BlockPos pos, T obj, U obj1, BiConsumer<T, U> run) {
        T capturedObj = obj;
        executeWithChunk(world, pos, () -> { run.accept(capturedObj, obj1); });
    }

    public static <T, R> R executeWithChunk(Level world, BlockPos pos, T obj, Function<T, R> run) {
        return executeWithChunk(world, pos, applyFunction(run, () -> obj));
    }

    public static <T, R> R executeWithChunk(Level world, BlockPos pos, T obj, Function<T, R> run, R _default) {
        return executeWithChunk(world, pos, applyFunction(run, () -> obj), _default);
    }

    public static <T> T eitherOf(Random r, T... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)];
    }

    @SafeVarargs
    public static <T> T eitherOf(Random r, Supplier<T>... selection) {
        if (selection.length == 0) {
            return null;
        }
        return selection[r.nextInt(selection.length)].get();
    }

    @SafeVarargs
    public static <T> Optional<T> tryMultiple(Supplier<T>... suppliers) {
        for (Supplier<T> supplier : suppliers) {
            try {
                return Optional.ofNullable(supplier.get());
            } catch (Exception exc) {
                AstralSorcery.LOG.error("Error in tryMultiple", exc);
            }
        }
        return Optional.empty();
    }

    public static boolean isPlayerFakeMP(ServerPlayer player) {
        if (player instanceof FakePlayer) {
            return true;
        }

        boolean isModdedPlayer = false;
        for (Mods mod : Mods.values()) {
            if (!mod.isPresent()) {
                continue;
            }
            Class<?> specificPlayerClass = mod.getExtendedPlayerClass();
            if (specificPlayerClass != null) {
                if (player.getClass() != ServerPlayer.class && player.getClass() == specificPlayerClass) {
                    isModdedPlayer = true;
                    break;
                }
            }
        }
        if (!isModdedPlayer && player.getClass() != ServerPlayer.class) {
            return true;
        }

        if (player.connection == null) {
            return true;
        }
        try {
            player.getIpAddress();
            player.connection.connection.getRemoteAddress().toString();
        } catch (Exception exc) {
            return true;
        }
        return false;
    }

    public static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() { return first; }
        public B getSecond() { return second; }
    }

    public static class WRItemObject<T> {
        public final int itemWeight;
        private final T value;

        public WRItemObject(int itemWeight, T value) {
            this.itemWeight = Math.max(itemWeight, 0);
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }
}
