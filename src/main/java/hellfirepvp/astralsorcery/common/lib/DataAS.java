package hellfirepvp.astralsorcery.common.lib;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.data.world.GatewayCache;
import hellfirepvp.astralsorcery.common.data.world.LightNetworkBuffer;
import hellfirepvp.astralsorcery.common.data.world.RockCrystalBuffer;
import hellfirepvp.astralsorcery.common.data.world.StorageNetworkBuffer;
import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheDomain;
import hellfirepvp.astralsorcery.common.data.world.cache.WorldCacheManager;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: DataAS
 * Created by HellFirePvP
 * Date: 03.07.2019 / 16:46
 *
 * 1:1 with the 1.16.5 class; the only difference is that the domain/save-key creation that 1.16.5
 * performed in {@code common.registry.RegistryData#init()} is inlined into {@link #init()} here and
 * additionally run from a static initializer, so the constants are never observed as {@code null}
 * regardless of which registry phase touches them first. {@link #init()} is idempotent, so a
 * {@code RegistryData.init()} call added later stays harmless.
 *
 * The save-key identifiers below are the on-disk directory and section-file names
 * ("[identifier]_[sectionX]_[sectionZ].dat"). They MUST NOT be changed: they are the save format,
 * and {@code SectionWorldData#readData} splits section file names on '_' expecting exactly 3 parts,
 * so identifiers must not contain underscores either.
 */
public class DataAS {

    private DataAS() {}

    public static WorldCacheDomain DOMAIN_AS;

    public static WorldCacheDomain.SaveKey<GatewayCache> KEY_GATEWAY_CACHE;
    public static WorldCacheDomain.SaveKey<LightNetworkBuffer> KEY_STARLIGHT_NETWORK;
    public static WorldCacheDomain.SaveKey<StorageNetworkBuffer> KEY_STORAGE_NETWORK;
    public static WorldCacheDomain.SaveKey<RockCrystalBuffer> KEY_ROCK_CRYSTAL_BUFFER;

    static {
        init();
    }

    /**
     * Mirrors 1.16.5 {@code RegistryData#init()}.
     */
    public static void init() {
        if (DOMAIN_AS != null) {
            return; //Already set up; WorldCacheManager#createDomain would replace the live domain.
        }
        WorldCacheDomain existing = WorldCacheManager.findDomain(AstralSorcery.MODID);
        DOMAIN_AS = existing != null ? existing : WorldCacheManager.createDomain(AstralSorcery.MODID);

        KEY_GATEWAY_CACHE = DOMAIN_AS.createSaveKey("gateway-cache", GatewayCache::new);
        KEY_STARLIGHT_NETWORK = DOMAIN_AS.createSaveKey("lightnetwork", LightNetworkBuffer::new);
        KEY_STORAGE_NETWORK = DOMAIN_AS.createSaveKey("storagenetwork", StorageNetworkBuffer::new);
        KEY_ROCK_CRYSTAL_BUFFER = DOMAIN_AS.createSaveKey("rock-crystals", RockCrystalBuffer::new);
    }

}
