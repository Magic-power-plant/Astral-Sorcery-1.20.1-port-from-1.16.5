package hellfirepvp.astralsorcery;

import hellfirepvp.astralsorcery.client.ClientProxy;
import hellfirepvp.astralsorcery.common.CommonProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AstralSorcery.MODID)
public class AstralSorcery {

    public static final String MODID = "astralsorcery";
    public static final String NAME = "Astral Sorcery";

    public static final Logger LOG = LoggerFactory.getLogger(NAME);

    private static AstralSorcery instance;
    private final CommonProxy proxy;

    // Forge 47 constructs the mod class with either no arguments or an FMLJavaModLoadingContext;
    // an IEventBus parameter (the NeoForge form) fails CONSTRUCT with NoSuchMethodException.
    public AstralSorcery(FMLJavaModLoadingContext context) {
        instance = this;
        IEventBus modEventBus = context.getModEventBus();

        this.proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        this.proxy.initialize();
        this.proxy.attachLifecycle(modEventBus);
        this.proxy.attachEventHandlers(MinecraftForge.EVENT_BUS);
    }

    public static AstralSorcery getInstance() {
        return instance;
    }

    public static CommonProxy getProxy() {
        return getInstance().proxy;
    }

    public static ResourceLocation locate(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation key(String path) {
        return locate(path);
    }
}