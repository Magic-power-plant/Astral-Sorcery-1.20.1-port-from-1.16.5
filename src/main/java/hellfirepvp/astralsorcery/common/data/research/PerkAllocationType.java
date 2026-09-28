package hellfirepvp.astralsorcery.common.data.research;

public enum PerkAllocationType {

    UNLOCKED("unlocked"),
    GRANTED("granted");

    private final String saveKey;

    PerkAllocationType(String saveKey) {
        this.saveKey = saveKey;
    }

    public final String getSaveKey() {
        return saveKey;
    }

}
