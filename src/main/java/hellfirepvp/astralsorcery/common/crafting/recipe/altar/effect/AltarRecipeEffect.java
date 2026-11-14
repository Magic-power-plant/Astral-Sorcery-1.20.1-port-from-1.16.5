/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.effect;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.ActiveSimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarType;
import hellfirepvp.astralsorcery.common.blockentity.altar.AltarBlockEntity;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AltarRecipeEffect
 * Created by HellFirePvP
 * Date: 23.09.2019 / 17:46
 */
//1.16.5 extended ForgeRegistryEntry<AltarRecipeEffect> for its registry name; that class is gone in 1.20.1,
//so the registry name is tracked locally - same convention as CustomRecipeSerializer/StructureType in this tree.
public abstract class AltarRecipeEffect {

    protected static final int INDEX_NOISE_PLANE_LAYER1 = 0;
    protected static final int INDEX_NOISE_PLANE_LAYER2 = 1;
    protected static final int INDEX_CRAFT_FLARE = 2;

    protected static final Random rand = new Random();
    private static final Vector3[] offsetPillarsT2 = new Vector3[] {
            new Vector3( 2, 0,  2),
            new Vector3(-2, 0,  2),
            new Vector3( 2, 0, -2),
            new Vector3(-2, 0, -2)
    };
    private static final Vector3[] offsetPillarsT3 = new Vector3[] {
            new Vector3( 3, 0,  3),
            new Vector3(-3, 0,  3),
            new Vector3( 3, 0, -3),
            new Vector3(-3, 0, -3)
    };

    private ResourceLocation registryName = null;

    public <T extends AltarRecipeEffect> T setRegistryName(ResourceLocation registryName) {
        this.registryName = registryName;
        return (T) this;
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return this.registryName;
    }

    protected static Vector3 getRandomPillarOffset(AltarType type) {
        switch (type) {
            case ATTUNEMENT:
                return offsetPillarsT2[rand.nextInt(offsetPillarsT2.length)].clone();
            case CONSTELLATION:
            case RADIANCE:
                return offsetPillarsT3[rand.nextInt(offsetPillarsT3.length)].clone();
        }
        return new Vector3();
    }

    protected static Vector3 getPillarOffset(AltarType type, int index) {
        switch (type) {
            case ATTUNEMENT:
                return offsetPillarsT2[index % offsetPillarsT2.length].clone();
            case CONSTELLATION:
            case RADIANCE:
                return offsetPillarsT3[index % offsetPillarsT3.length].clone();
        }
        return new Vector3();
    }

    protected static int getPillarHeight(AltarType type) {
        switch (type) {
            case ATTUNEMENT:
                return 2;
            case CONSTELLATION:
            case RADIANCE:
                return 3;
        }
        return 0;
    }

    protected static int getPillarAmount(AltarType type) {
        if (type == AltarType.DISCOVERY) {
            return 0;
        }
        return 4;
    }

    protected static Vector3 getFocusRelayOffset(AltarType type) {
        switch (type) {
            case RADIANCE:
                return new Vector3(0, 4.5, 0);
        }
        return new Vector3();
    }


    @OnlyIn(Dist.CLIENT)
    protected long getClientTick() {
        return ClientScheduler.getClientTick();
    }

    @OnlyIn(Dist.CLIENT)
    public abstract void onTick(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state);

    @OnlyIn(Dist.CLIENT)
    public abstract void onTESR(AltarBlockEntity altar, ActiveSimpleAltarRecipe.CraftingState state, PoseStack renderStack, MultiBufferSource buffer, float pTicks, int combinedLight);

    @OnlyIn(Dist.CLIENT)
    public abstract void onCraftingFinish(AltarBlockEntity altar, boolean isChaining);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AltarRecipeEffect that = (AltarRecipeEffect) o;
        return Objects.equals(this.getRegistryName(), that.getRegistryName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getRegistryName());
    }
}
