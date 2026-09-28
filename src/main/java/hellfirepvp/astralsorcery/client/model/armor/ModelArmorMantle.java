/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ModelArmorMantle
 * Created by HellFirePvP
 * Date: 17.02.2020 / 21:21
 */
public class ModelArmorMantle extends CustomArmorModel<LivingEntity> {

    //TODO adjust arms at some point

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(AstralSorcery.MODID, "armor_mantle"), "main");

    //1.16.5 created these four as bare `new ModelRenderer(this)` roots and then assigned them over
    //BipedModel's bipedBody/bipedLeftArm/bipedRightArm/bipedHead fields inside render().
    //In 1.20.1 HumanoidModel#body/#leftArm/#rightArm/#head are `public final`, so the swap is impossible.
    //Instead the LayerDefinition below declares the vanilla-named roots ("body", "left_arm", "right_arm",
    //"head") as empty, un-posed parts carrying the mantle geometry as children - which is exactly the
    //steady state 1.16.5 reached after its first render() call.
    //VERIFY_PORT[AS-OBJ-02]: 1.16.5 rendered the *vanilla* biped head/body/arms for a single frame before
    //the swap took effect (and 1.16.5's replacements also dropped the vanilla arm rotation points of
    //+-5/2/0 down to 0/0/0, which this port reproduces). The 1.20.1 port has no such first frame.
    private final ModelPart bodyReplacement, lArm, rArm, headReplacement;

    private final ModelPart cowl;
    private final ModelPart mantle_l;
    private final ModelPart mantle_r;

    private final ModelPart bodyAnchor;
    //1.16.5 field name: `body` - renamed here because HumanoidModel already declares a `body` field.
    private final ModelPart mantleBody;
    private final ModelPart plate;

    private final ModelPart armLAnchor;
    private final ModelPart armLpauldron;
    private final ModelPart fitting_l;

    private final ModelPart armRAnchor;
    private final ModelPart armRpauldron;
    private final ModelPart fitting_r;

    public ModelArmorMantle() {
        //Keeps the 1.16.5 `new ModelArmorMantle()` call shape (used by ItemMantle#getArmorModel) valid
        //without requiring the EntityModelSet to have been reloaded first.
        this(createLayer().bakeRoot());
    }

    public ModelArmorMantle(ModelPart root) {
        super(root);

        this.headReplacement = this.head;
        this.bodyReplacement = this.body;
        this.lArm = this.leftArm;
        this.rArm = this.rightArm;

        this.cowl = this.headReplacement.getChild("cowl");

        this.bodyAnchor = this.bodyReplacement.getChild("bodyAnchor");
        this.mantleBody = this.bodyAnchor.getChild("mantleBody");
        this.plate = this.mantleBody.getChild("plate");
        this.mantle_l = this.mantleBody.getChild("mantle_l");
        this.mantle_r = this.mantleBody.getChild("mantle_r");

        this.armLAnchor = this.lArm.getChild("armLAnchor");
        this.armLpauldron = this.armLAnchor.getChild("armLpauldron");
        this.fitting_l = this.armLpauldron.getChild("fitting_l");

        this.armRAnchor = this.rArm.getChild("armRAnchor");
        this.armRpauldron = this.armRAnchor.getChild("armRpauldron");
        this.fitting_r = this.armRpauldron.getChild("fitting_r");
    }

    //1.16.5: textureWidth = 64; textureHeight = 128
    public static LayerDefinition createLayer() {
        float s = 0.01F;
        CubeDeformation deform = new CubeDeformation(s);

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        //1.16.5: this.headReplacement = new ModelRenderer(this); -> assigned over bipedHead
        PartDefinition headReplacement = root.addOrReplaceChild("head",
                CubeListBuilder.create(), PartPose.ZERO);

        headReplacement.addOrReplaceChild("cowl",
                CubeListBuilder.create()
                        .texOffs(0, 33)
                        .addBox(-4.5F, -4.0F, -4.0F, 9.0F, 5.0F, 9.0F, deform),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2617993877991494F, 0.0F, 0.0F));

        //1.16.5 hid bipedHeadwear/bipedRightLeg/bipedLeftLeg outright, so they carry no geometry here.
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        //1.16.5: this.bodyReplacement = new ModelRenderer(this); -> assigned over bipedBody
        PartDefinition bodyReplacement = root.addOrReplaceChild("body",
                CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition bodyAnchor = bodyReplacement.addOrReplaceChild("bodyAnchor",
                CubeListBuilder.create()
                        .texOffs(0, 41)
                        .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F, deform),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mantleBody = bodyAnchor.addOrReplaceChild("mantleBody",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, -0.5F, -3.0F, 9.0F, 6.0F, 6.0F, deform),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        mantleBody.addOrReplaceChild("plate",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-3.5F, -0.5F, -1.0F, 7.0F, 7.0F, 2.0F, deform),
                PartPose.offsetAndRotation(0.0F, 1.0F, -3.0F, 0.08726646259971647F, 0.0F, 0.0F));

        //1.16.5: mantle_l.mirror = true
        mantleBody.addOrReplaceChild("mantle_l",
                CubeListBuilder.create()
                        .texOffs(0, 47)
                        .mirror()
                        .addBox(-8.0F, -3.5F, 1.0F, 9.0F, 21.0F, 5.0F, deform)
                        .mirror(false),
                PartPose.offsetAndRotation(6.25F, 2.0F, 0.0F, 0.08726646259971647F, 0.2617993877991494F, 0.0F));

        mantleBody.addOrReplaceChild("mantle_r",
                CubeListBuilder.create()
                        .texOffs(0, 47)
                        .addBox(-1.0F, -3.5F, 1.0F, 9.0F, 21.0F, 5.0F, deform),
                PartPose.offsetAndRotation(-6.25F, 2.0F, 0.0F, 0.08726646259971647F, -0.2617993877991494F, 0.0F));

        //1.16.5: this.lArm = new ModelRenderer(this); -> assigned over bipedLeftArm
        PartDefinition lArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create(), PartPose.ZERO);

        //1.16.5: armLAnchor.mirror = true
        PartDefinition armLAnchor = lArm.addOrReplaceChild("armLAnchor",
                CubeListBuilder.create()
                        .texOffs(0, 41)
                        .mirror()
                        .addBox(-6.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, deform)
                        .mirror(false),
                PartPose.offset(4.0F, 2.0F, 0.0F));

        //1.16.5: armLpauldron.mirror = true
        PartDefinition armLpauldron = armLAnchor.addOrReplaceChild("armLpauldron",
                CubeListBuilder.create()
                        .texOffs(0, 21)
                        .mirror()
                        .addBox(-5.45F, -4.0F, -3.0F, 5.0F, 6.0F, 6.0F, deform)
                        .mirror(false),
                PartPose.offset(0.0F, 0.0F, -0.0F));

        armLpauldron.addOrReplaceChild("fitting_l",
                CubeListBuilder.create()
                        .texOffs(18, 12)
                        .addBox(-6.0F, -2.0F, -1.0F, 4.0F, 1.0F, 2.0F, deform),
                PartPose.offsetAndRotation(0.5F, -3.0F, 0.0F, 0.0F, 0.0F, 0.08726646259971647F));

        //1.16.5: this.rArm = new ModelRenderer(this); -> assigned over bipedRightArm
        PartDefinition rArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create(), PartPose.ZERO);

        //1.16.5: armRAnchor.mirror = true
        PartDefinition armRAnchor = rArm.addOrReplaceChild("armRAnchor",
                CubeListBuilder.create()
                        .texOffs(0, 41)
                        .mirror()
                        .addBox(4.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, deform)
                        .mirror(false),
                PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition armRpauldron = armRAnchor.addOrReplaceChild("armRpauldron",
                CubeListBuilder.create()
                        .texOffs(0, 21)
                        .addBox(0.45F, -4.0F, -3.0F, 5.0F, 6.0F, 6.0F, deform),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        armRpauldron.addOrReplaceChild("fitting_r",
                CubeListBuilder.create()
                        .texOffs(18, 12)
                        .addBox(1.5F, -2.0F, -1.0F, 4.0F, 1.0F, 2.0F, deform),
                PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.0F, 0.0F, -0.08726646259971647F));

        return LayerDefinition.create(mesh, 64, 128);
    }

    //1.16.5: render(MatrixStack, IVertexBuilder, int, int, float, float, float, float)
    @Override
    public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.bodyAnchor.visible = true;
        this.armRAnchor.visible = true;
        this.armLAnchor.visible = true;
        //1.16.5 toggled `bipedHead` here, which - from the second render() call onwards - was the
        //headReplacement part this port uses unconditionally.
        this.headReplacement.visible = true;

        this.hat.visible = false;
        this.rightLeg.visible = false;
        this.leftLeg.visible = false;

        //1.16.5 performed the bipedBody/bipedRightArm/bipedLeftArm/bipedHead field swap right here;
        //see the comment on the replacement fields for why 1.20.1 bakes it into the LayerDefinition.
        super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
