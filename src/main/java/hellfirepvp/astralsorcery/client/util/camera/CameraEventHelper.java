package hellfirepvp.astralsorcery.client.util.camera;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Port of 1.16.5 {@code CameraEventHelper}: hides the real local player while a camera transformer is active
 * (the {@link EntityClientReplacement} is rendered in its place).
 */
public class CameraEventHelper {

    public static void attachEventListeners(IEventBus eventBus) {
        eventBus.addListener(CameraEventHelper::onPlayerRender);
    }

    private static void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (event.getEntity() == Minecraft.getInstance().player && ClientCameraManager.INSTANCE.hasActiveTransformer()) {
            event.setCanceled(true);
        }
    }

}
