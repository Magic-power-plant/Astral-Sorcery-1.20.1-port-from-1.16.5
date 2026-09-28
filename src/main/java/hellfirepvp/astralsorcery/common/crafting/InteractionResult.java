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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: InteractionResult
 * Created by HellFirePvP
 * Date: 31.10.2020 / 13:56
 */
//TODO_PORT[AS-PKG-01]: 1.16.5 keeps this in common/crafting/recipe/interaction together with
//InteractionResultRegistry, ResultDropItem and ResultSpawnEntity; this tree has all four at the top level of
//common/crafting. Not moved because the package layout is a tree-wide decision.
//1.16.5 passes its own common/util/data/Vector3 as the position (1.16.5
//common/crafting/recipe/interaction/InteractionResult.java:42); this tree uses vanilla Vec3, which every
//caller and both implementations already agree on.
public abstract class InteractionResult {

    private final ResourceLocation id;

    protected InteractionResult(ResourceLocation id) {
        this.id = id;
    }

    public final ResourceLocation getId() {
        return id;
    }

    public abstract void doResult(Level level, Vec3 at);

    public abstract void read(JsonObject json) throws JsonParseException;

    public abstract void write(JsonObject json);

    public abstract void read(FriendlyByteBuf buffer);

    public abstract void write(FriendlyByteBuf buffer);
}
