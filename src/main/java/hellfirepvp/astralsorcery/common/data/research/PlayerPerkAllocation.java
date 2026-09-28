package hellfirepvp.astralsorcery.common.data.research;

import net.minecraft.Util;

import java.util.UUID;

public class PlayerPerkAllocation {

    private static final UUID UNLOCK_UUID = Util.NIL_UUID;

    private final PerkAllocationType type;
    private final UUID lockUUID;

    private PlayerPerkAllocation(PerkAllocationType type, UUID lockUUID) {
        this.type = type;
        this.lockUUID = lockUUID;
    }

    public static PlayerPerkAllocation unlock() {
        return new PlayerPerkAllocation(PerkAllocationType.UNLOCKED, UNLOCK_UUID);
    }

    public static PlayerPerkAllocation granted(UUID lockUUID) {
        return new PlayerPerkAllocation(PerkAllocationType.GRANTED, lockUUID);
    }

    public PerkAllocationType getType() {
        return type;
    }

    public UUID getLockUUID() {
        return lockUUID;
    }

}
