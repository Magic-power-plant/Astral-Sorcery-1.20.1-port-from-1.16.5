package hellfirepvp.astralsorcery.common.util;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

import java.util.Stack;

public class BlockDropCaptureAssist {

    public static final BlockDropCaptureAssist INSTANCE = new BlockDropCaptureAssist();

    private static final Stack<NonNullList<ItemStack>> capturing = new Stack<>();

    private BlockDropCaptureAssist() {}

    public void onDrop(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel && event.getEntity() instanceof ItemEntity) {
            ItemStack itemStack = ((ItemEntity) event.getEntity()).getItem();
            if (!capturing.isEmpty()) {
                event.setCanceled(true);
                if (!itemStack.isEmpty()) {
                    if (!capturing.isEmpty()) {
                        capturing.peek().add(itemStack);
                    }
                }
                event.getEntity().discard();
            }
        }
    }

    public static void startCapturing() {
        capturing.push(NonNullList.create());
    }

    public static NonNullList<ItemStack> getCapturedStacksAndStop() {
        return capturing.pop();
    }

}
