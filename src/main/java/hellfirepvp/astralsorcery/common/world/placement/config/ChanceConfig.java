package hellfirepvp.astralsorcery.common.world.placement.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.Random;

public class ChanceConfig {

    public static final Codec<ChanceConfig> CODEC = RecordCodecBuilder.create(codecInstance -> {
        return codecInstance.group(Codec.FLOAT.fieldOf("chance").forGetter(config -> {
            return config.chance;
        })).apply(codecInstance, ChanceConfig::new);
    });

    private final float chance;

    public ChanceConfig(float chance) {
        this.chance = Mth.clamp(chance, 0F, 1F);
    }

    public boolean test(Random rand) {
        return rand.nextFloat() < this.chance;
    }
}
