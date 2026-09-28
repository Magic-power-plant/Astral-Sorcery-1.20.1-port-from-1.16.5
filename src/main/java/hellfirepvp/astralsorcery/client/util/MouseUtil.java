/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.util;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MouseUtil
 * Created by HellFirePvP
 * Date: 14.02.2020 / 21:52
 */
public class MouseUtil {

    private static boolean preventGuiChange = false;

    public static void attachEventListeners(IEventBus eventBus) {
        eventBus.addListener(EventPriority.HIGHEST, MouseUtil::onGuiOpen);
    }

    public static void ungrab() {
        //1.16.5: Minecraft#mouseHelper#ungrabMouse -> 1.20.1: Minecraft#mouseHandler#releaseMouse
        Minecraft.getInstance().mouseHandler.releaseMouse();
    }

    public static void grab() {
        preventGuiChange = true;
        Minecraft.getInstance().mouseHandler.grabMouse();
    }

    //1.16.5: GuiOpenEvent -> 1.20.1: ScreenEvent.Opening (still @Cancelable; canceling keeps the current screen)
    private static void onGuiOpen(ScreenEvent.Opening event) {
        if (preventGuiChange) {
            preventGuiChange = false;
            event.setCanceled(true);
        }
    }

}
