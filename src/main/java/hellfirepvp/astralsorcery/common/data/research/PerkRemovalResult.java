package hellfirepvp.astralsorcery.common.data.research;

public enum PerkRemovalResult {

    FAILURE,
    REMOVE_ALLOCATION,
    REMOVE_ALLOCATION_TYPE,
    REMOVE_PERK;

    public boolean isFailure() {
        return this == FAILURE;
    }

    public boolean removesAllocationType() {
        return this == REMOVE_ALLOCATION_TYPE || this.removesPerk();
    }

    public boolean removesPerk() {
        return this == REMOVE_PERK;
    }

}
