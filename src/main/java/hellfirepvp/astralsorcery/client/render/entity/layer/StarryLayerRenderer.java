/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import hellfirepvp.astralsorcery.client.registry.RegistryRenderTypes;
import hellfirepvp.astralsorcery.common.util.object.CacheReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StarryLayerRenderer
 * Created by HellFirePvP
 * Date: 06.01.2021 / 16:00
 *
 * 1.16.5 -&gt; 1.20.1: BipedArmorLayer -&gt; HumanoidArmorLayer, BipedModel -&gt; HumanoidModel, IEntityRenderer -&gt;
 * RenderLayerParent, PlayerEntity -&gt; Player, EquipmentSlotType -&gt; EquipmentSlot (and
 * EquipmentSlotType#getSlotType/Group -&gt; EquipmentSlot#getType/Type), MatrixStack -&gt; PoseStack,
 * IRenderTypeBuffer -&gt; MultiBufferSource, IEntityRenderer#getEntityModel -&gt;
 * RenderLayer#getParentModel, BipedModel#setModelAttributes -&gt; HumanoidModel#copyPropertiesTo,
 * BipedArmorLayer#setModelSlotVisible -&gt; HumanoidArmorLayer#setPartVisibility and Model#render -&gt;
 * Model#renderToBuffer.
 *
 * Two constructor differences are unavoidable:
 * - 1.16.5 {@code new PlayerModel<>(float modelSize, boolean smallArms)} baked its own geometry from a float
 *   expansion. In 1.20.1 PlayerModel takes an already baked ModelPart root, so the expansion is expressed as a
 *   {@link CubeDeformation} fed through {@code PlayerModel.createMesh(..)} and baked here. 64x64 is the player
 *   model's texture size, exactly what the 1.16.5 constructor hardcoded.
 * - 1.20.1 HumanoidArmorLayer additionally wants a ModelManager (for vanilla trim rendering). This layer never
 *   delegates to {@code super.render(..)}, but the live manager is handed over rather than null so the
 *   superclass is never left in an invalid state.
 */
public class StarryLayerRenderer<E extends LivingEntity, M extends HumanoidModel<E>> extends HumanoidArmorLayer<E, M, HumanoidModel<E>> {

    private static final List<CacheReference<RenderType>> RENDER_TYPES = IntStream.range(0, 2)
            .mapToObj((i) -> new CacheReference<>(() -> RegistryRenderTypes.createDepthProjectionType(i)))
            .collect(Collectors.toList());
    private static final HumanoidModel MODEL_HEAD = new PlayerModel<>(bakePlayerRoot(-0.5F, false), false);
    private static final HumanoidModel MODEL_ARMOR = new PlayerModel<>(bakePlayerRoot(0F, false), false);
    private static final HumanoidModel MODEL_ARMOR_SMALL = new PlayerModel<>(bakePlayerRoot(0F, true), true);

    private static BiPredicate<Player, EquipmentSlot> renderTest = (p, type) -> false;

    private final boolean slimRender;

    public StarryLayerRenderer(RenderLayerParent<E, M> entityRendererIn, boolean slimRender) {
        super(entityRendererIn, MODEL_ARMOR, MODEL_ARMOR, Minecraft.getInstance().getModelManager());
        this.slimRender = slimRender;
    }

    private static ModelPart bakePlayerRoot(float expand, boolean slim) {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(expand), slim), 64, 64).bakeRoot();
    }

    public static void addRender(BiPredicate<Player, EquipmentSlot> render) {
        renderTest = renderTest.or(render);
    }

    @Override
    public void render(PoseStack renderStack, MultiBufferSource buffer, int light, E entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof Player)) {
            return;
        }

        for (EquipmentSlot slotType : EquipmentSlot.values()) {
            if (slotType.getType() == EquipmentSlot.Type.ARMOR) {
                if (renderTest.test((Player) entity, slotType)) {
                    HumanoidModel<E> model = slotType == EquipmentSlot.HEAD ? MODEL_HEAD : this.slimRender ? MODEL_ARMOR_SMALL : MODEL_ARMOR;
                    this.renderArmorPart(renderStack, buffer, slotType, light, model);
                }
            }
        }
    }

    private void renderArmorPart(PoseStack renderStack, MultiBufferSource buffer, EquipmentSlot slotType, int light, HumanoidModel<E> model) {
        this.getParentModel().copyPropertiesTo(model);
        this.setPartVisibility(model, slotType);
        for (CacheReference<RenderType> renderType : RENDER_TYPES) {
            model.renderToBuffer(renderStack, buffer.getBuffer(renderType.get()), light, OverlayTexture.NO_OVERLAY, 0.4F, 0.4F, 1F, 0.1F);
        }
    }
}
