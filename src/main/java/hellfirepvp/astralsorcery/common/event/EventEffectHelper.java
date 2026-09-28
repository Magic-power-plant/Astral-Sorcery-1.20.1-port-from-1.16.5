package hellfirepvp.astralsorcery.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class EventEffectHelper {

    public static void spawnStarlightParticles(Level level, Vec3 pos, int count) {
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < count; i++) {
                double offsetX = (Math.random() - 0.5) * 2;
                double offsetY = Math.random() * 2;
                double offsetZ = (Math.random() - 0.5) * 2;
                serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 1, 0, 0, 0, 0);
            }
        }
    }

    public static void spawnCollectedStarlight(Level level, BlockPos pos, int amount) {
        int particles = Math.min(amount / 10, 20);
        spawnStarlightParticles(level, Vec3.atCenterOf(pos), particles);
    }

    public static float calculatePlayerStarlightExposure(Player player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        
        int openSkyBlocks = 0;
        int totalChecks = 16;
        
        for (int i = 0; i < totalChecks; i++) {
            double angle = (i / (double) totalChecks) * Math.PI * 2;
            double dx = Math.cos(angle) * 2;
            double dz = Math.sin(angle) * 2;
            BlockPos checkPos = pos.offset((int) dx, 5, (int) dz);
            if (canSeeSky(level, checkPos)) {
                openSkyBlocks++;
            }
        }
        
        return openSkyBlocks / (float) totalChecks;
    }

    private static boolean canSeeSky(Level level, BlockPos pos) {
        return level.dimension() == Level.OVERWORLD;
    }

    public static void applyConstellationEffect(Player player, float intensity) {
        if (intensity <= 0) return;
    }
}