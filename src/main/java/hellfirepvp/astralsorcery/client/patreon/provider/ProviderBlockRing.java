/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.patreon.provider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hellfirepvp.astralsorcery.common.patreon.effect.FlareColor;
import hellfirepvp.astralsorcery.common.patreon.effect.PatreonEffectProvider;
import hellfirepvp.astralsorcery.client.patreon.effect.TypeBlockRing;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ProviderBlockRing
 * Created by HellFirePvP
 * Date: 31.08.2019 / 10:31
 */
public class ProviderBlockRing implements PatreonEffectProvider<TypeBlockRing> {

    //1.16.5: private static final JsonParser PARSER = new JsonParser(); + PARSER.parse(String) - both the
    //instance constructor and the instance parse(String) are deprecated on the gson shipped with 1.20.1;
    //the static JsonParser#parseString is the identical code path and is what the rest of this port uses.

    @Override
    public TypeBlockRing buildEffect(UUID playerUUID, List<String> effectParameters) throws Exception {
        UUID effectUniqueId = UUID.fromString(effectParameters.get(0));
        FlareColor fc = null;
        if (!"null".equals(effectParameters.get(1))) {
            fc = FlareColor.valueOf(effectParameters.get(1));
        }

        float distance = Float.parseFloat(effectParameters.get(2));
        float rotationAngle = Float.parseFloat(effectParameters.get(3));
        int repeats = Integer.parseInt(effectParameters.get(4));
        int tickRotationSpeed = Integer.parseInt(effectParameters.get(5));
        JsonArray jo = (JsonArray) JsonParser.parseString(effectParameters.get(6));
        HashMap<BlockPos, BlockState> pattern = new HashMap<>();
        for (JsonElement patternElement : jo) {
            JsonObject obj = (JsonObject) patternElement;
            BlockPos pos = new BlockPos(
                    obj.getAsJsonPrimitive("posX").getAsInt(),
                    obj.getAsJsonPrimitive("posY").getAsInt(),
                    obj.getAsJsonPrimitive("posZ").getAsInt());
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(obj.getAsJsonPrimitive("block").getAsString()));
            //1.16.5: Block#getDefaultState() -> Block#defaultBlockState()
            pattern.put(pos, b.defaultBlockState());
        }
        return new TypeBlockRing(effectUniqueId,
                fc, playerUUID,
                distance,
                rotationAngle,
                repeats,
                tickRotationSpeed,
                pattern);
    }
}
