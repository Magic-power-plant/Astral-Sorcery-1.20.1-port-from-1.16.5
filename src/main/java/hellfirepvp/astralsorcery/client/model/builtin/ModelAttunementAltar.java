/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.model.builtin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.lib.RenderTypesAS;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModelAttunementAltar
 * Created by wiiv
 */
public class ModelAttunementAltar extends CustomModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(AstralSorcery.MODID, "attunement_altar"), "main");

    private final ModelPart base;
    private final ModelPart hovering;

    public ModelAttunementAltar() {
        //1.16.5 built the geometry inline in the constructor; 1.20.1 bakes it from a LayerDefinition.
        //bakeRoot() keeps the 1.16.5 `new ModelAttunementAltar()` call shape valid without requiring the
        //EntityModelSet to be populated first.
        this(createLayer().bakeRoot());
    }

    public ModelAttunementAltar(ModelPart root) {
        super((resKey) -> RenderTypesAS.MODEL_ATTUNEMENT_ALTAR);

        this.base = root.getChild("base");
        this.hovering = root.getChild("hovering");
    }

    //1.16.5: textureWidth = 128; textureHeight = 32
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-10.0F, -14.0F, -10.0F, 20.0F, 6.0F, 20.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 16.0F, 0.0F));

        root.addOrReplaceChild("hovering",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(-2.0F, -16.0F, -2.0F)); //was -14, -14

        return LayerDefinition.create(mesh, 128, 32);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    public void renderHovering(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, float offX, float offZ, float perc) {
        float distance = 0.9453125F;
        this.hovering.setPos(-2F + (16F * offX * distance), -16F, -2F + (16F * offZ * distance));
        this.setRotateAngle(this.hovering, offZ * 0.39269908169872414F * perc, 0, offX * -0.39269908169872414F * perc);
        this.hovering.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
