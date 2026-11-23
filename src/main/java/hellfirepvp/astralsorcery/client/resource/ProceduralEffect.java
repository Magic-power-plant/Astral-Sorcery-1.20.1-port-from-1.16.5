package hellfirepvp.astralsorcery.client.resource;

import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public enum ProceduralEffect {
    ATTUNEMENT("attunement_flare", 8, 6, 0, 0, 0x60EBFF, "attunement_flare"),
    COLLECTOR("collector_crystal_burst", 16, 5, 1, 0, 0x348FB4, "collector_crystal_burst"),
    CRAFT_BURST("craft_burst", 8, 6, 2, 0, 0x6161C5, "craft_burst"),
    CRAFT_FLARE("craft_flare", 8, 8, 3, 0, 0xFFFFFF, "craft_flare"),
    CRYSTAL_1("crystal_burst_effect_1", 8, 5, 4, 1, 0x277EFF, "crystal_burst_effect_1"),
    CRYSTAL_2("crystal_burst_effect_2", 8, 5, 4, 2, 0x2A7EFF, "crystal_burst_effect_2"),
    CRYSTAL_3("crystal_burst_effect_3", 8, 5, 4, 3, 0x3378FF, "crystal_burst_effect_3"),
    DAZZLING("dazzling_aura", 4, 4, 5, 0, 0xB1B1FF),
    ENTITY("entity_flare", 8, 6, 6, 0, 0x624EF7, "entity_flare"),
    LIQUID("fountain_liquid", 8, 4, 7, 0, 0x8888FF, "fountain_liquid"),
    VORTEX("fountain_vortex", 8, 5, 8, 0, 0x8080FF, "fountain_vortex"),
    GEM("gem_crystal_burst", 8, 5, 9, 0, 0xDFDFDF, "gem_crystal_burst"),
    GEM_DAY("gem_crystal_burst_day", 8, 5, 9, 1, 0xFF4B00, "gem_crystal_burst_day"),
    GEM_NIGHT("gem_crystal_burst_night", 8, 5, 9, 2, 0x797979, "gem_crystal_burst_night"),
    GEM_SKY("gem_crystal_burst_sky", 8, 5, 9, 3, 0x055699, "gem_crystal_burst_sky"),
    HOOK("grappling_hook", 8, 4, 10, 0, 0x2A2AF4, "grappling_hook"),
    INFUSION("halo_infusion", 8, 8, 11, 0, 0x502AA7),
    RITUAL("halo_ritual", 8, 6, 12, 0, 0x9BCAFF),
    BEAM("lightbeam", 16, 4, 13, 0, 0xFFFFFF),
    TRANSFER("lightbeam_transfer", 16, 4, 13, 1, 0xFFFFFF),
    RELAY("relay_flare", 8, 6, 14, 0, 0x5353BE),
    STORAGE("starlight_storage", 4, 16, 15, 0, 0xBDF1FF),
    AIR("patreonflares/air", 48, 1, 6, 1, 0x949A6B),
    BLUE("patreonflares/blue", 48, 1, 6, 2, 0x1880F8),
    DARK_GREEN("patreonflares/dark_green", 48, 1, 6, 3, 0x009C0D),
    DARK_RED("patreonflares/dark_red", 48, 1, 6, 4, 0xD30936),
    DAWN("patreonflares/dawn", 48, 1, 6, 5, 0xC13162),
    EARTH("patreonflares/earth", 48, 1, 6, 6, 0x785A31),
    ELDRITCH("patreonflares/eldritch", 48, 1, 6, 7, 0x660577),
    FIRE("patreonflares/fire", 48, 1, 6, 8, 0xE42D04),
    GOLD("patreonflares/gold", 48, 1, 6, 9, 0xF58C2C),
    MONO("patreonflares/gray_mono", 48, 1, 6, 10, 0x505050, "gray_mono"),
    GREEN("patreonflares/green", 48, 1, 6, 11, 0x47B22D),
    MAGENTA("patreonflares/magenta", 48, 1, 6, 12, 0xFA8EFA),
    RED("patreonflares/red", 48, 1, 6, 13, 0xFF126B),
    STANDARD("patreonflares/standard", 48, 1, 6, 14, 0x7951D0),
    WATER("patreonflares/water", 48, 1, 6, 15, 0x3B3D7B),
    WHITE("patreonflares/white", 48, 1, 6, 16, 0xD4FCFC),
    YELLOW("patreonflares/yellow", 48, 1, 6, 17, 0xF3CE2D);

    private static final Map<String, ProceduralEffect> BY_PATH = new HashMap<>();
    static {
        for (ProceduralEffect effect : values()) BY_PATH.put("textures/effect/" + effect.path + ".png", effect);
    }

    private final String path;
    public final int columns, rows, family, variant, rgb;
    public final String program;

    ProceduralEffect(String path, int columns, int rows, int family, int variant, int rgb) {
        this(path, columns, rows, family, variant, rgb, family == 6 ? "corona" : "procedural");
    }

    ProceduralEffect(String path, int columns, int rows, int family, int variant, int rgb, String program) {
        this.path = path;
        this.columns = columns;
        this.rows = rows;
        this.family = family;
        this.variant = variant;
        this.rgb = rgb;
        this.program = "effect_" + program;
    }

    static ProceduralEffect forSprite(ResourceLocation key) {
        return AstralSorcery.MODID.equals(key.getNamespace()) ? BY_PATH.get(key.getPath()) : null;
    }

    public String spritePath() {
        return path;
    }
}
