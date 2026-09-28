/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.structure;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.common.structure.match.block.MatchableState;
import hellfirepvp.astralsorcery.common.structure.match.structure.Structure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Small utility that guesses which items a structure might need to be built.
 *
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructureContentHelper
 * Created by HellFirePvP
 * Date: 22.08.2019 / 21:31
 *
 * Embedded from ObserverLib (the {@code getAsStacks} default method of
 * hellfirepvp.observerlib.api.util.ContentSerializable); ObserverLib is a source-only reference for this port,
 * not a runtime dependency. Deliberate deviations:
 *   - it is a STATIC HELPER in the client package rather than a default method on
 *     {@link Structure}. Adding it to that interface would drag client-only types
 *     (and a {@code @OnlyIn(Dist.CLIENT)}) onto a common interface, and an @OnlyIn on an interface member
 *     propagates to every implementor - which would keep the structure patterns off a dedicated server.
 *   - it reads {@link Structure#getContents()} directly instead of requiring a {@code BlockArray}.
 *   - ClientTickHelper#getClientTick becomes {@link ClientScheduler#getClientTick()}.
 */
@OnlyIn(Dist.CLIENT)
public class StructureContentHelper {

    private StructureContentHelper() {}

    /**
     * Returns the structure's potentially required items.
     *
     * @param structure the structure to inspect
     * @param world the world the structure is to be tested in
     * @param player the player to view the structure as blocks
     * @return a list of non-empty item stacks potentially required for this structure.
     */
    public static List<ItemStack> getAsStacks(Structure structure, LevelReader world, Player player) {
        List<ItemStack> out = new LinkedList<>();
        if (structure == null) {
            return out;
        }

        long tick = ClientScheduler.getClientTick();
        for (Map.Entry<BlockPos, ? extends MatchableState> structureEntry : structure.getContents().entrySet()) {
            BlockPos pos = structureEntry.getKey();
            BlockState sample = structureEntry.getValue().getDescriptiveState(tick);

            ItemStack stack = ItemStack.EMPTY;
            if (!sample.getFluidState().isEmpty() && sample.getFluidState().isSource()) {
                Fluid f = sample.getFluidState().getType();
                stack = FluidUtil.getFilledBucket(new FluidStack(f, FluidType.BUCKET_VOLUME));
            }

            if (stack.isEmpty()) {
                try {
                    stack = sample.getCloneItemStack(new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false),
                            world, pos, player);
                } catch (Exception ignored) {}
            }

            if (!stack.isEmpty()) {
                Item needle = stack.getItem();

                ItemStack existing = ItemStack.EMPTY;
                for (ItemStack i : out) {
                    if (i.getItem().equals(needle)) {
                        existing = i;
                        break;
                    }
                }
                if (existing.isEmpty()) {
                    out.add(stack);
                } else {
                    existing.setCount(existing.getCount() + stack.getCount());
                }
            }
        }
        return out;
    }
}
