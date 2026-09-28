package hellfirepvp.astralsorcery.common.auxiliary;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AnimalHelper {

    private static final LinkedList<HerdableAnimal> animalHandlers = new LinkedList<>();

    static {
        register(new SquidHandler());
        register(new GenericAnimal());
    }

    public static void registerFirst(HerdableAnimal handler) {
        animalHandlers.addFirst(handler);
    }

    public static void register(HerdableAnimal handler) {
        animalHandlers.add(handler);
    }

    @Nullable
    public static HerdableAnimal getHandler(LivingEntity entity) {
        for (HerdableAnimal herd : animalHandlers) {
            if (herd.handles(entity)) {
                return herd;
            }
        }
        return null;
    }

    public interface HerdableAnimal {

        boolean handles(@Nonnull LivingEntity entity);

        List<ItemStack> generateDrops(@Nonnull LivingEntity entity, Level world, Random rand, float luck);

    }

    public static class GenericAnimal implements HerdableAnimal {

        @Override
        public boolean handles(@Nonnull LivingEntity entity) {
            return entity instanceof Animal;
        }

        @Override
        public List<ItemStack> generateDrops(@Nonnull LivingEntity entity, Level world, Random rand, float luck) {
            return EntityUtils.generateLoot(entity, rand, CommonProxy.DAMAGE_SOURCE_STELLAR, null);
        }
    }

    public static class SquidHandler implements HerdableAnimal {

        @Override
        public boolean handles(@Nonnull LivingEntity entity) {
            return entity instanceof net.minecraft.world.entity.animal.Squid;
        }

        @Override
        public List<ItemStack> generateDrops(@Nonnull LivingEntity entity, Level world, Random rand, float luck) {
            return EntityUtils.generateLoot(entity, rand, CommonProxy.DAMAGE_SOURCE_STELLAR, null);
        }
    }
}
