package hellfirepvp.astralsorcery.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import hellfirepvp.astralsorcery.client.util.RenderingDrawUtils;
import hellfirepvp.astralsorcery.common.entity.item.EntityItemHighlighted;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderEntityItemHighlighted extends EntityRenderer<ItemEntity> {

    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public RenderEntityItemHighlighted(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    protected int getRenderAmount(ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }
        return i;
    }

    protected boolean shouldSpreadItems() {
        return true;
    }

    protected boolean shouldBob() {
        return true;
    }

    @Override
    public void render(ItemEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        //1.16.5 client/render/entity/RenderEntityItemHighlighted#render opens with this light-ray fan and only
        //then delegates to the vanilla body. This class inlines that vanilla body (it needs its own
        //getRenderAmount/shouldBob overrides) and the mod-specific prologue was lost in the copy, which left
        //EntityItemHighlighted#getHighlightColor with zero callers even though the colour really is assigned
        //(ItemCrystalBase, ItemConstellationPaper). Dropped rock/celestial/attuned crystals and constellation
        //papers therefore had no glow at all.
        if (entity instanceof EntityItemHighlighted highlighted && highlighted.hasColor()) {
            poseStack.pushPose();
            poseStack.translate(0, 0.35F, 0);
            RenderingDrawUtils.renderLightRayFan(poseStack, buffer,
                    highlighted.getHighlightColor(), 160420L + entity.getId(),
                    16, 12, 15);
            poseStack.popPose();
        }

        poseStack.pushPose();
        ItemStack itemstack = entity.getItem();
        int i = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
        this.random.setSeed(i);
        BakedModel bakedmodel = this.itemRenderer.getModel(itemstack, entity.level(), null, entity.getId());
        boolean flag = bakedmodel.isGui3d();
        int j = this.getRenderAmount(itemstack);
        float f1 = shouldBob() ? Mth.sin(((float) entity.getAge() + partialTicks) / 10.0F + entity.bobOffs) * 0.1F + 0.1F : 0;
        float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        poseStack.translate(0.0F, f1 + 0.25F * f2, 0.0F);
        float f3 = entity.getSpin(partialTicks);
        poseStack.mulPose(Axis.YP.rotation(f3));
        if (!flag) {
            float f7 = -0.0F * (float) (j - 1) * 0.5F;
            float f8 = -0.0F * (float) (j - 1) * 0.5F;
            float f9 = -0.09375F * (float) (j - 1) * 0.5F;
            poseStack.translate(f7, f8, f9);
        }
        for (int k = 0; k < j; ++k) {
            poseStack.pushPose();
            if (k > 0) {
                if (flag) {
                    float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    poseStack.translate(shouldSpreadItems() ? f11 : 0, shouldSpreadItems() ? f13 : 0, shouldSpreadItems() ? f10 : 0);
                } else {
                    float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    poseStack.translate(shouldSpreadItems() ? f12 : 0, shouldSpreadItems() ? f14 : 0, 0.0D);
                }
            }
            this.itemRenderer.render(itemstack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, bakedmodel);
            poseStack.popPose();
            if (!flag) {
                poseStack.translate(0.0, 0.0, 0.09375F);
            }
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
