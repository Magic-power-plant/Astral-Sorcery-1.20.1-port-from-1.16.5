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
 * Class: ModelObservatory
 * Created by wiiv
 * Created using Tabula 7.0.0
 */
public class ModelObservatory extends CustomModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(AstralSorcery.MODID, "observatory"), "main");

    private final ModelPart base;
    private final ModelPart tube;
    private final ModelPart seat;
    private final ModelPart base1;
    private final ModelPart base2;
    private final ModelPart base3;
    private final ModelPart base4;
    private final ModelPart base5;
    private final ModelPart base6;
    private final ModelPart base7;
    private final ModelPart base8;

    private final ModelPart seat1;
    private final ModelPart seat2;
    private final ModelPart seat3;
    private final ModelPart seat4;
    private final ModelPart seat5;
    private final ModelPart seat6;
    private final ModelPart seat7;
    private final ModelPart seat8;
    private final ModelPart seat9;
    private final ModelPart seat10;
    private final ModelPart seat11;
    private final ModelPart seat12;
    private final ModelPart seat13;
    private final ModelPart seat14;

    private final ModelPart tube1;
    private final ModelPart tube2;
    private final ModelPart tube3;
    private final ModelPart tube4;
    private final ModelPart tube5;
    private final ModelPart tube6;
    private final ModelPart tube7;
    private final ModelPart tube8;
    private final ModelPart tube9;
    private final ModelPart tube10;
    private final ModelPart tube11;
    private final ModelPart tube12;
    private final ModelPart tube13;
    private final ModelPart tube14;
    private final ModelPart tube15;

    public ModelObservatory() {
        //1.16.5 built the geometry inline in the constructor; 1.20.1 bakes it from a LayerDefinition.
        //bakeRoot() keeps the 1.16.5 `new ModelObservatory()` call shape valid without requiring the
        //EntityModelSet to be populated first.
        this(createLayer().bakeRoot());
    }

    public ModelObservatory(ModelPart root) {
        super((resKey) -> RenderTypesAS.MODEL_OBSERVATORY);

        this.base = root.getChild("base");
        this.base1 = this.base.getChild("base1");
        this.base2 = this.base.getChild("base2");
        this.base3 = this.base.getChild("base3");
        this.base4 = this.base.getChild("base4");
        this.base5 = this.base.getChild("base5");
        this.base6 = this.base.getChild("base6");
        this.base7 = this.base.getChild("base7");
        this.base8 = this.base.getChild("base8");

        this.seat = root.getChild("seat");
        this.seat1 = this.seat.getChild("seat1");
        this.seat2 = this.seat.getChild("seat2");
        this.seat3 = this.seat.getChild("seat3");
        this.seat4 = this.seat.getChild("seat4");
        this.seat5 = this.seat.getChild("seat5");
        this.seat6 = this.seat.getChild("seat6");
        this.seat7 = this.seat.getChild("seat7");
        this.seat8 = this.seat.getChild("seat8");
        this.seat9 = this.seat.getChild("seat9");
        this.seat10 = this.seat.getChild("seat10");
        this.seat11 = this.seat.getChild("seat11");
        this.seat12 = this.seat.getChild("seat12");
        this.seat13 = this.seat.getChild("seat13");
        this.seat14 = this.seat.getChild("seat14");

        //1.16.5: this.seat.addChild(this.tube);
        this.tube = this.seat.getChild("tube");
        this.tube1 = this.tube.getChild("tube1");
        this.tube2 = this.tube.getChild("tube2");
        this.tube3 = this.tube.getChild("tube3");
        this.tube4 = this.tube.getChild("tube4");
        this.tube5 = this.tube.getChild("tube5");
        this.tube6 = this.tube.getChild("tube6");
        this.tube7 = this.tube.getChild("tube7");
        this.tube8 = this.tube.getChild("tube8");
        this.tube9 = this.tube.getChild("tube9");
        this.tube10 = this.tube.getChild("tube10");
        this.tube11 = this.tube.getChild("tube11");
        this.tube12 = this.tube.getChild("tube12");
        this.tube13 = this.tube.getChild("tube13");
        this.tube14 = this.tube.getChild("tube14");
        this.tube15 = this.tube.getChild("tube15");
    }

    //1.16.5: textureWidth = 256; textureHeight = 128
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        //base
        PartDefinition base = root.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 82)
                        .addBox(-12.0F, 18.0F, -16.0F, 24.0F, 6.0F, 28.0F, new CubeDeformation(0.1F)),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        base.addOrReplaceChild("base1",
                CubeListBuilder.create()
                        .texOffs(120, 82)
                        .addBox(-14.0F, 4.0F, -18.0F, 6.0F, 18.0F, 12.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("base2",
                CubeListBuilder.create()
                        .texOffs(224, 52)
                        .addBox(-7.0F, 4.0F, -18.0F, 2.0F, 18.0F, 12.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("base3",
                CubeListBuilder.create()
                        .texOffs(224, 52)
                        .addBox(-4.0F, 4.0F, -18.0F, 2.0F, 18.0F, 12.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("base4",
                CubeListBuilder.create()
                        .texOffs(224, 52)
                        .addBox(-1.0F, 4.0F, -18.0F, 2.0F, 18.0F, 12.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("base5",
                CubeListBuilder.create()
                        .texOffs(180, 52)
                        .addBox(2.0F, 4.0F, -18.0F, 10.0F, 18.0F, 12.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("base6",
                CubeListBuilder.create()
                        .texOffs(192, 0)
                        .addBox(12.0F, -18.0F, -18.0F, 8.0F, 40.0F, 12.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("base7",
                CubeListBuilder.create()
                        .texOffs(156, 82)
                        .addBox(8.0F, 4.0F, -6.0F, 8.0F, 18.0F, 20.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        base.addOrReplaceChild("base8",
                CubeListBuilder.create()
                        .texOffs(192, 82)
                        .addBox(-8.0F, 28.0F, -8.0F, 16.0F, 4.0F, 16.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        //seat
        PartDefinition seat = root.addOrReplaceChild("seat",
                CubeListBuilder.create()
                        .texOffs(144, 28)
                        .addBox(-9.0F, 16.0F, 6.0F, 12.0F, 4.0F, 10.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        seat.addOrReplaceChild("seat1",
                CubeListBuilder.create()
                        .texOffs(144, 42)
                        .addBox(-9.0F, 16.0F, 0.0F, 12.0F, 2.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        seat.addOrReplaceChild("seat2",
                CubeListBuilder.create()
                        .texOffs(144, 10)
                        .addBox(-9.0F, 6.0F, 16.0F, 12.0F, 14.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        seat.addOrReplaceChild("seat3",
                CubeListBuilder.create()
                        .texOffs(144, 0)
                        .addBox(-7.0F, 2.0F, 16.0F, 8.0F, 6.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat4",
                CubeListBuilder.create()
                        .texOffs(140, 82)
                        .addBox(-1.0F, 18.0F, 12.0F, 2.0F, 4.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat5",
                CubeListBuilder.create()
                        .texOffs(156, 82)
                        .addBox(-1.0F, 22.0F, 12.0F, 2.0F, 4.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat6",
                CubeListBuilder.create()
                        .texOffs(156, 82)
                        .addBox(-7.0F, 22.0F, 12.0F, 2.0F, 4.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat7",
                CubeListBuilder.create()
                        .texOffs(232, 0)
                        .addBox(-1.0F, -2.0F, 20.0F, 2.0F, 28.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat8",
                CubeListBuilder.create()
                        .texOffs(232, 0)
                        .addBox(-7.0F, -2.0F, 20.0F, 2.0F, 28.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat9",
                CubeListBuilder.create()
                        .texOffs(232, 2)
                        .addBox(2.0F, -2.0F, 20.0F, 2.0F, 22.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat10",
                CubeListBuilder.create()
                        .texOffs(232, 30)
                        .addBox(-4.0F, -4.0F, 20.0F, 2.0F, 20.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat11",
                CubeListBuilder.create()
                        .texOffs(232, 2)
                        .addBox(-10.0F, -2.0F, 20.0F, 2.0F, 22.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat12",
                CubeListBuilder.create()
                        .texOffs(240, 0)
                        .addBox(2.0F, -6.0F, 20.0F, 2.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat13",
                CubeListBuilder.create()
                        .texOffs(240, 0)
                        .addBox(-4.0F, -8.0F, 20.0F, 2.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        seat.addOrReplaceChild("seat14",
                CubeListBuilder.create()
                        .texOffs(240, 0)
                        .addBox(-10.0F, -6.0F, 20.0F, 2.0F, 4.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        //tube - 1.16.5 attaches the tube to the seat, so it is defined as a child of the seat here
        PartDefinition tube = seat.addOrReplaceChild("tube",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-2.0F, -4.0F, -4.0F, 14.0F, 8.0F, 8.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0.0F, -12.0F, -12.0F, -0.7853981633974483F, 0.0F, 0.0F));

        tube.addOrReplaceChild("tube1",
                CubeListBuilder.create()
                        .texOffs(92, 0)
                        .addBox(-2.0F, -4.0F, -36.0F, 14.0F, 6.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube2",
                CubeListBuilder.create()
                        .texOffs(78, 90)
                        .addBox(2.0F, -2.0F, -30.0F, 2.0F, 2.0F, 26.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube3",
                CubeListBuilder.create()
                        .texOffs(78, 90)
                        .addBox(6.0F, -2.0F, -30.0F, 2.0F, 2.0F, 26.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube4",
                CubeListBuilder.create()
                        .texOffs(92, 28)
                        .addBox(-2.0F, -16.0F, -2.0F, 14.0F, 8.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube5",
                CubeListBuilder.create()
                        .texOffs(92, 12)
                        .addBox(-2.0F, -7.0F, -2.0F, 14.0F, 2.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube6",
                CubeListBuilder.create()
                        .texOffs(92, 12)
                        .addBox(-2.0F, -16.0F, -34.0F, 14.0F, 8.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube7",
                CubeListBuilder.create()
                        .texOffs(92, 12)
                        .addBox(-2.0F, -7.0F, -34.0F, 14.0F, 2.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube8",
                CubeListBuilder.create()
                        .texOffs(92, 12)
                        .addBox(-2.0F, -16.0F, -40.0F, 14.0F, 14.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube9",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -16.0F, -60.0F, 14.0F, 14.0F, 18.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube10",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0.0F, -14.0F, -56.0F, 10.0F, 10.0F, 68.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube11",
                CubeListBuilder.create()
                        .texOffs(92, 50)
                        .addBox(-4.0F, -10.0F, 2.0F, 4.0F, 4.0F, 12.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube12",
                CubeListBuilder.create()
                        .texOffs(44, 32)
                        .addBox(-4.0F, -10.0F, 14.0F, 2.0F, 2.0F, 6.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube13",
                CubeListBuilder.create()
                        .texOffs(0, 48)
                        .addBox(2.0F, -12.0F, 12.0F, 6.0F, 6.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube14",
                CubeListBuilder.create()
                        .texOffs(92, 0)
                        .addBox(6.0F, -18.0F, -44.0F, 2.0F, 2.0F, 48.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        tube.addOrReplaceChild("tube15",
                CubeListBuilder.create()
                        .texOffs(92, 0)
                        .addBox(2.0F, -18.0F, -44.0F, 2.0F, 2.0F, 48.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 128);
    }

    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.seat.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        this.base.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    public void setupRotations(float iYawDegree, float iPitchDegree) {
        float yawRad = (float) Math.toRadians(iYawDegree);
        float pitchRad = (float) Math.toRadians(iPitchDegree);

        this.seat.yRot = yawRad;
        this.base.yRot = yawRad;
        this.tube.xRot = pitchRad;
    }
}
