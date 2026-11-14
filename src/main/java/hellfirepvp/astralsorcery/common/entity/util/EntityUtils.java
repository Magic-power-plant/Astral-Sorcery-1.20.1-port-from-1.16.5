package hellfirepvp.astralsorcery.common.entity.util;

import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EntityUtils {

    private static final Random rand = new Random();

    @Nullable
    public static Player getPlayer(UUID playerUUID, LogicalSide side) {
        return side.isClient() ? getPlayerClient(playerUUID) : getPlayerServer(playerUUID);
    }

    @Nullable
    public static Player getPlayerServer(UUID playerUUID) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        return server.getPlayerList().getPlayer(playerUUID);
    }

    @Nullable
    public static Player getPlayerClient(UUID playerUUID) {
        return hellfirepvp.astralsorcery.AstralSorcery.getProxy().findClientPlayer(playerUUID);
    }

    public static void applyPotionEffectAtHalf(LivingEntity entity, MobEffectInstance effect) {
        MobEffectInstance activeEffect = entity.getEffect(effect.getEffect());
        if (activeEffect != null) {
            if (activeEffect.getDuration() <= effect.getDuration() / 2) {
                entity.addEffect(effect);
            }
        } else {
            entity.addEffect(effect);
        }
    }

    public static void applyVortexMotion(Supplier<Vector3> positionSupplier, Consumer<Vector3> addMotion, Vector3 to, double vortexRange, double multiplier) {
        Vector3 pos = positionSupplier.get();
        double diffX = (to.getX() - pos.getX()) / vortexRange;
        double diffY = (to.getY() - pos.getY()) / vortexRange;
        double diffZ = (to.getZ() - pos.getZ()) / vortexRange;
        double dist = Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
        if (1.0D - dist > 0.0D) {
            double dstFactorSq = (1.0D - dist) * (1.0D - dist);
            Vector3 toAdd = new Vector3();
            toAdd.setX(diffX / dist * dstFactorSq * 0.15D * multiplier);
            toAdd.setY(diffY / dist * dstFactorSq * 0.15D * multiplier);
            toAdd.setZ(diffZ / dist * dstFactorSq * 0.15D * multiplier);
            addMotion.accept(toAdd);
        }
    }

    @Nullable
    public static LivingEntity performWorldSpawningAt(ServerLevel world, BlockPos pos, MobCategory category, MobSpawnType reason, boolean ignoreWeighting, int ignoreSpawnCheckFlags) {
        var biome = world.getBiome(pos);
        var spawnList = new ArrayList<>(biome.value().getMobSettings().getMobs(category).unwrap());
        spawnList.removeIf(s -> !s.type.canSummon());
        MobSpawnSettings.SpawnerData entry;
        if (ignoreWeighting) {
            entry = MiscUtils.getRandomEntry(spawnList, rand);
        } else {
            entry = MiscUtils.getWeightedRandomEntry(spawnList, rand, (MobSpawnSettings.SpawnerData ee) -> ee.getWeight().asInt());
        }

        if (entry != null) {
            double x = pos.getX() + 0.5;
            double y = pos.getY();
            double z = pos.getZ() + 0.5;

            if (canEntitySpawnHere(world, pos, entry.type, reason, ignoreSpawnCheckFlags, null)) {
                Mob entity;
                try {
                    entity = (Mob) entry.type.create(world);
                } catch (Exception exception) {
                    return null;
                }
                if (entity == null) {
                    return null;
                }

                entity.moveTo(x, y, z, rand.nextFloat() * 360F, 0F);

                entity.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(pos), reason, null, null);

                world.addFreshEntityWithPassengers(entity);
                return entity;
            }
        }
        return null;
    }

    public static boolean canEntitySpawnHere(ServerLevelAccessor world, BlockPos at, EntityType<?> type, MobSpawnType spawnReason, int ignoreCheckFlags, @Nullable Consumer<Entity> preCheckEntity) {
        if (type.getCategory() == MobCategory.MISC || !type.canSummon() || !world.getWorldBorder().isWithinBounds(at)) {
            return false;
        }
        if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_PLACEMENT_RULES)) {
            var placementType = SpawnPlacements.getPlacementType(type);
            if (!Mob.checkMobSpawnRules((EntityType<? extends Mob>) type, (ServerLevelAccessor) world, spawnReason, at, world.getRandom())) {
                return false;
            }
        }
        if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_BLOCK_COLLISION)) {
            var dims = type.getDimensions();
            var checkBox = new AABB(
                at.getX() + 0.5 - dims.width / 2, at.getY(),
                at.getZ() + 0.5 - dims.width / 2,
                at.getX() + 0.5 + dims.width / 2, at.getY() + dims.height,
                at.getZ() + 0.5 + dims.width / 2);
            if (!world.noCollision(checkBox)) {
                return false;
            }
        }

        Entity entity = type.create(world.getLevel());
        if (entity == null) {
            return false;
        }
        entity.moveTo(at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, world.getRandom().nextFloat() * 360.0F, 0.0F);
        if (preCheckEntity != null) {
            preCheckEntity.accept(entity);
        }

        if (entity instanceof Mob mob) {
            if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_ENTITY_SPAWN_CONDITIONS)) {
                if (!mob.checkSpawnRules((ServerLevelAccessor) world, spawnReason)) {
                    return false;
                }
            }
            if (!SpawnConditionFlags.isSet(ignoreCheckFlags, SpawnConditionFlags.IGNORE_ENTITY_COLLISION)) {
                if (!mob.checkSpawnObstruction((ServerLevelAccessor) world)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nonnull
    public static List<ItemStack> generateLoot(LivingEntity entity, Random rand, DamageSource srcDeath, @Nullable LivingEntity lastAttacker) {
        MinecraftServer srv = ServerLifecycleHooks.getCurrentServer();
        if (srv == null) {
            return Collections.emptyList();
        }
        ServerLevel sw = (ServerLevel) entity.level();

        if (!sw.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT)) {
            return Collections.emptyList();
        }

        ResourceLocation lootTableKey = entity.getLootTable();
        LootTable table = srv.getLootData().getLootTable(lootTableKey);
        LootParams.Builder builder = new LootParams.Builder(sw)
                .withParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, entity.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, srcDeath)
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, srcDeath.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, srcDeath.getDirectEntity());
        if (lastAttacker != null) {
            if (lastAttacker instanceof Player) {
                builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, (Player) lastAttacker)
                        .withLuck(((Player) lastAttacker).getLuck());
            }
        }

        return table.getRandomItems(builder.create(LootContextParamSets.ENTITY));
    }

    @Nullable
    public static <T extends Entity> T getClosestEntity(Level world, Class<T> type, AABB box, Vector3 closestTo) {
        List<T> entities = world.getEntitiesOfClass(type, box, Entity::isAlive);
        return selectClosest(entities, closestTo::distanceSquared);
    }

    public static Predicate<? super Entity> selectEntities(Class<? extends Entity>... entities) {
        return (Predicate<Entity>) entity -> {
            if (entity == null || !entity.isAlive()) return false;
            Class<? extends Entity> clazz = entity.getClass();
            for (Class<? extends Entity> test : entities) {
                if (test.isAssignableFrom(clazz)) return true;
            }
            return false;
        };
    }

    public static Predicate<? super Entity> selectItemClassInstanceof(Class<?> itemClass) {
        return (Predicate<Entity>) entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity)) return false;
            ItemStack i = ((ItemEntity) entity).getItem();
            if (i.isEmpty()) return false;
            return itemClass.isAssignableFrom(i.getItem().getClass());
        };
    }

    public static Predicate<? super Entity> selectItem(Item item) {
        return (Predicate<Entity>) entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity)) return false;
            ItemStack i = ((ItemEntity) entity).getItem();
            if (i.isEmpty()) return false;
            return i.getItem().equals(item);
        };
    }

    public static Predicate<? super Entity> selectItemStack(Function<ItemStack, Boolean> acceptor) {
        return entity -> {
            if (entity == null || !entity.isAlive()) return false;
            if (!(entity instanceof ItemEntity)) return false;
            ItemStack i = ((ItemEntity) entity).getItem();
            if (i.isEmpty()) return false;
            return acceptor.apply(i);
        };
    }

    @Nullable
    public static <T> T selectClosest(Collection<T> elements, Function<T, Double> dstFunc) {
        if (elements.isEmpty()) return null;

        double dstClosest = Double.MAX_VALUE;
        T closestElement = null;
        for (T element : elements) {
            double dst = dstFunc.apply(element);
            if (dst < dstClosest) {
                closestElement = element;
                dstClosest = dst;
            }
        }
        return closestElement;
    }

    public static class SpawnConditionFlags {

        public static final int IGNORE_PLACEMENT_RULES         = 0b0001;
        public static final int IGNORE_ENTITY_COLLISION        = 0b0010;
        public static final int IGNORE_BLOCK_COLLISION         = 0b0100;
        public static final int IGNORE_ENTITY_SPAWN_CONDITIONS = 0b1000;

        public static final int IGNORE_COLLISIONS = IGNORE_BLOCK_COLLISION | IGNORE_ENTITY_COLLISION;
        public static final int IGNORE_SPAWN_CONDITIONS = IGNORE_PLACEMENT_RULES | IGNORE_ENTITY_SPAWN_CONDITIONS;
        public static final int IGNORE_ALL = IGNORE_COLLISIONS | IGNORE_SPAWN_CONDITIONS;

        public static boolean isSet(int flags, int flag) {
            return (flags & flag) != 0;
        }

    }

}
