package hellfirepvp.astralsorcery.api;

/** Version of the documented Java extension contract, independent of mod and payload versions. */
public final class AstralSorceryApi {
    public static final int MAJOR = 1;
    public static final int MINOR = 0;
    public static final String VERSION = "1.0";
    private AstralSorceryApi() {}

    public static boolean supports(int major, int minimumMinor) {
        return major == MAJOR && minimumMinor >= 0 && minimumMinor <= MINOR;
    }

    public static void requireVersion(int major, int minimumMinor) {
        if (!supports(major, minimumMinor)) {
            throw new IllegalStateException("Requires Astral Sorcery API " + major + "."
                    + minimumMinor + "; available: " + VERSION);
        }
    }
}
