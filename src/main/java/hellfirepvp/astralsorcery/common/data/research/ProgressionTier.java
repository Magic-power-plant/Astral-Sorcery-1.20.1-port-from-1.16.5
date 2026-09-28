package hellfirepvp.astralsorcery.common.data.research;

public enum ProgressionTier {
    DISCOVERY,
    BASIC_CRAFT,
    ATTUNEMENT,
    CONSTELLATION_CRAFT,
    TRAIT_CRAFT,
    BRILLIANCE;

    public boolean hasNextTier() {
        return ordinal() < values().length - 1;
    }

    public ProgressionTier next() {
        return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    public boolean isThisLaterOrEqual(ProgressionTier other) {
        return ordinal() >= other.ordinal();
    }

    public boolean isThisLater(ProgressionTier other) {
        return ordinal() > other.ordinal();
    }
}
