package hellfirepvp.astralsorcery.common.entity.goal;

import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public abstract class SpectralToolGoal extends Goal {

    private final EntitySpectralTool entity;
    private final double speed;

    protected int actionCooldown = 0;

    public SpectralToolGoal(EntitySpectralTool entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.TARGET, Flag.LOOK));
    }

    public EntitySpectralTool getEntity() {
        return entity;
    }

    public double getSpeed() {
        return speed;
    }
}
