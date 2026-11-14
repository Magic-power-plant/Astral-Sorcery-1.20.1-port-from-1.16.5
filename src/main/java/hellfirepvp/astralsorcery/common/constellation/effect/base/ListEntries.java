package hellfirepvp.astralsorcery.common.constellation.effect.base;

import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.entity.util.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListEntries {

    public static class EntitySpawnEntry extends CounterEntry {

        private EntityType<?> type;

        public EntitySpawnEntry(BlockPos pos) {
            super(pos);
        }

        public EntitySpawnEntry(BlockPos pos, EntityType<?> type) {
            super(pos);
            this.type = type;
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);
            this.type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(nbt.getString("entity")));
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {
            super.writeToNBT(nbt);
            nbt.putString("entity", ForgeRegistries.ENTITY_TYPES.getKey(this.type).toString());
        }

        public static EntitySpawnEntry createEntry(ServerLevel world, BlockPos pos, MobSpawnType reason) {
            var biome = world.getBiome(pos);
            MobCategory category = DayTimeHelper.isNight(world) ? MobCategory.MONSTER : MobCategory.CREATURE;
            var applicable = new ArrayList<>(biome.value().getMobSettings().getMobs(category).unwrap());
            if (applicable.isEmpty()) {
                return null;
            }
            Collections.shuffle(applicable);
            MobSpawnSettings.SpawnerData entry = applicable.get(world.random.nextInt(applicable.size()));
            EntityType<?> type = entry.type;
            if (type != null && type.canSummon() && EntityUtils.canEntitySpawnHere(world, pos, type, reason, EntityUtils.SpawnConditionFlags.IGNORE_SPAWN_CONDITIONS,
                    (e) -> e.addTag(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY))) {
                return new EntitySpawnEntry(pos, type);
            }
            return null;
        }

        public void spawn(ServerLevel world, MobSpawnType reason) {
            if (this.type == null) {
                return;
            }

            Entity e = this.type.create(world);
            if (e != null) {
                e.addTag(ConstellationEffectRegistry.ENTITY_TAG_LUCERNA_SKIP_ENTITY);

                BlockPos at = getPos();
                double x = at.getX() + 0.5;
                double y = at.getY() + 0.5;
                double z = at.getZ() + 0.5;
                e.setPos(x, y, z);
                e.setYRot(world.random.nextFloat() * 360.0F);
                e.setXRot(0.0F);
                if (e instanceof Mob mob) {
                    mob.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(at), reason, null, null);
                    if (!mob.checkSpawnObstruction((ServerLevelAccessor) world)) {
                        e.discard();
                        return;
                    }
                }
                world.addFreshEntity(e);
                world.levelEvent(2004, e.blockPosition(), 0);
                world.levelEvent(2004, e.blockPosition(), 0);
            }
        }
    }

    public static class CounterMaxEntry extends CounterEntry {

        private int maxCount;

        public CounterMaxEntry(BlockPos pos) {
            super(pos);
        }

        public CounterMaxEntry(BlockPos pos, int maxCount) {
            super(pos);
            this.maxCount = maxCount;
        }

        public int getMaxCount() {
            return maxCount;
        }

        public void setMaxCount(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {
            super.writeToNBT(nbt);
            nbt.putInt("maxCount", this.maxCount);
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);
            this.maxCount = nbt.getInt("maxCount");
        }
    }

    public static class CounterEntry extends PosEntry {

        private int counter = 0;

        public CounterEntry(BlockPos pos) {
            super(pos);
        }

        public int getCounter() {
            return counter;
        }

        public void setCounter(int counter) {
            this.counter = counter;
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {
            super.writeToNBT(nbt);
            nbt.putInt("counter", this.counter);
        }

        @Override
        public void readFromNBT(CompoundTag nbt) {
            super.readFromNBT(nbt);
            this.counter = nbt.getInt("counter");
        }
    }

    public static class PosEntry implements CEffectAbstractList.ListEntry {

        private final BlockPos pos;

        public PosEntry(BlockPos pos) {
            this.pos = pos;
        }

        @Override
        public BlockPos getPos() {
            return this.pos;
        }

        @Override
        public void writeToNBT(CompoundTag nbt) {}

        @Override
        public void readFromNBT(CompoundTag nbt) {}
    }
}
