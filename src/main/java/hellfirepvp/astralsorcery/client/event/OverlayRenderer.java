/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.common.item.base.client.ItemOverlayRender;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: OverlayRenderHelper
 * Created by HellFirePvP
 * Date: 23.02.2020 / 17:15
 */
public class OverlayRenderer {

    public static final OverlayRenderer INSTANCE = new OverlayRenderer();

    private OverlayRenderer() {}

    public void attachEventListeners(IEventBus bus) {
        bus.addListener(EventPriority.LOW, this::onOverlayRender);
    }

    // 1.16.5: RenderGameOverlayEvent.Post filtered to ElementType.ALL. In 1.20.1 that single "whole hud finished"
    // callback is RenderGuiEvent.Post (RenderGuiOverlayEvent is the per-named-overlay split), so the element type
    // check of the reference is carried by the event class itself.
    private void onOverlayRender(RenderGuiEvent.Post event) {
        float pTicks = event.getPartialTick();

        Player player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) {
            return;
        }

        // 1.16.5: event.getMatrixStack() - 1.20.1 hands out a GuiGraphics which owns the PoseStack
        PoseStack renderStack = event.getGuiGraphics().pose();
        for (EquipmentSlot type : EquipmentSlot.values()) {
            if (doHudRender(renderStack, player.getItemBySlot(type), pTicks)) {
                break;
            }
        }
    }

    private boolean doHudRender(PoseStack renderStack, ItemStack heldItem, float pTicks) {
        if (heldItem.isEmpty()) {
            return false;
        }
        Item held = heldItem.getItem();
        if (held instanceof ItemOverlayRender) {
            return ((ItemOverlayRender) held).renderOverlay(renderStack, heldItem, pTicks);
        }
        return false;
    }
}
