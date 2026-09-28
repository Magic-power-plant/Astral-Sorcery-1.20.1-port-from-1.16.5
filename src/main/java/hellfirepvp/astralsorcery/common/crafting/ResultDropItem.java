/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.JsonHelper;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResultDropItem
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:03
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe/interaction; this tree has it at the top
//level of common/crafting. Not moved because the package layout is a tree-wide decision.
public class ResultDropItem extends InteractionResult {

    private ItemStack output = ItemStack.EMPTY;

    ResultDropItem() {
        super(InteractionResultRegistry.ID_DROP_ITEM);
    }

    public static ResultDropItem dropItem(ItemStack output) {
        ResultDropItem drop = new ResultDropItem();
        drop.output = output.copy();
        return drop;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    @Override
    public void doResult(Level world, Vec3 at) {
        //1.16.5 passed its own Vector3 and read it through getX/getY/getZ; vanilla Vec3 exposes the same
        //three components as public fields.
        ItemUtils.dropItemNaturally(world, at.x, at.y, at.z, this.output.copy());
    }

    @Override
    public void read(JsonObject json) throws JsonParseException {
        //Restored the 1.16.5 helper pair (common/util/data/JsonHelper#getItemStack / #serializeItemStack,
        //both present in this tree): the previous inline item/count parsing here silently dropped the
        //stack's NBT tag, which JsonHelper round-trips.
        this.output = JsonHelper.getItemStack(json, "output");
        if (this.output.isEmpty()) {
            throw new JsonParseException("Output stack must not be empty!");
        }
    }

    @Override
    public void write(JsonObject json) {
        json.add("output", JsonHelper.serializeItemStack(this.output));
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        //1.16.5 used common/util/data/ByteBufUtils#readItemStack, which this tree carries; it writes the
        //count separately so stack sizes above 64 survive, unlike FriendlyByteBuf#readItem.
        this.output = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.output);
    }
}
