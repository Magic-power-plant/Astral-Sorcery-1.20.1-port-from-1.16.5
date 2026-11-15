package hellfirepvp.astralsorcery.common.data.research;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResearchSpriteIconTest {
    @Test
    void spriteNodesKeepTextureLayoutAndNodeMetadataWithoutLoadingResources() {
        var icon = new ResearchNode.SpriteIcon(ResourceLocation.parse("astralsorcery:textures/effect/relay_flare.png"), 6, 8);
        var node = new ResearchNode(icon, "QUICK_CHARGE", 0.75F, 4.5F);
        assertSame(icon, node.getSpriteTexture());
        assertEquals(ResearchNode.NodeRenderType.TEXTURE_SPRITE, node.getNodeRenderType());
        assertEquals("QUICK_CHARGE", node.getKey());
        assertEquals(0.75F, node.renderPosX);
        assertEquals(4.5F, node.renderPosZ);
        assertEquals(6, icon.rows());
        assertEquals(8, icon.columns());
        assertEquals("astralsorcery:textures/effect/relay_flare.png", icon.texture().toString());
        assertThrows(NullPointerException.class, () -> new ResearchNode.SpriteIcon(null, 6, 8));
        assertThrows(IllegalArgumentException.class, () -> new ResearchNode.SpriteIcon(icon.texture(), 0, 8));
        assertThrows(IllegalArgumentException.class, () -> new ResearchNode.SpriteIcon(icon.texture(), 6, -1));
    }
}
