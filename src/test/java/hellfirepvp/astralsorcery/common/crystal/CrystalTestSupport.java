package hellfirepvp.astralsorcery.common.crystal;

import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.registry.RegistryCrystalProperties;
import hellfirepvp.astralsorcery.common.registry.RegistryCrystalPropertyUsages;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Proxy;

/**
 * Runs the same crystal initialisation CommonProxy#initialize performs during mod construction.
 * <p>
 * Outside FML there is no RegisterEvent, so REGISTRY_CRYSTAL_PROPERTIES stays null. In the game that registry is
 * filled from RegistryCrystalProperties (RegistryRegistries#registerCrystalProperties), and
 * CrystalAttributes.Attribute#deserialize only ever calls getValue(ResourceLocation) on it, so the tests stand in a
 * registry that answers exactly that call from the same index.
 */
final class CrystalTestSupport {

    private static boolean initialized = false;

    private CrystalTestSupport() {}

    static synchronized void init() {
        if (initialized) {
            return;
        }
        RegistryCrystalPropertyUsages.init();
        ConstellationsAS.init(null);
        RegistryCrystalProperties.init();
        RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES = indexBackedRegistry();
        initialized = true;
    }

    @SuppressWarnings("unchecked")
    private static IForgeRegistry<CrystalProperty> indexBackedRegistry() {
        return (IForgeRegistry<CrystalProperty>) Proxy.newProxyInstance(
                CrystalTestSupport.class.getClassLoader(),
                new Class<?>[] { IForgeRegistry.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getValue") && args != null && args.length == 1
                            && args[0] instanceof ResourceLocation key) {
                        return RegistryCrystalProperties.get(key);
                    }
                    throw new UnsupportedOperationException("Not needed by the crystal tests: " + method);
                });
    }
}
