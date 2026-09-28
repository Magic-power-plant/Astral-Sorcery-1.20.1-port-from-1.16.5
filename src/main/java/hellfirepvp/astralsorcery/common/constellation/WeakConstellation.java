package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.resources.ResourceLocation;

import java.awt.Color;

public class WeakConstellation extends Constellation.Weak {
    public WeakConstellation(ResourceLocation id, String name, float brightness, float maxSize, int color) {
        super(id.getPath(), new Color(color));
    }
}
