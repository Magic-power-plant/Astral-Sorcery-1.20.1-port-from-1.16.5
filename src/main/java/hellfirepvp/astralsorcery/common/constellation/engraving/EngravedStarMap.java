package hellfirepvp.astralsorcery.common.constellation.engraving;

import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.DrawnConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class EngravedStarMap {

    private static final Random rand = new Random();

    private final Map<ResourceLocation, Float> distributions;
    private final List<DrawnConstellation> drawInformation;

    private EngravedStarMap(Map<ResourceLocation, Float> distributions, List<DrawnConstellation> drawnConstellations) {
        this.distributions = distributions;
        this.drawInformation = drawnConstellations;
    }

    public static EngravedStarMap buildStarMap(Level world, List<DrawnConstellation> constellations) {
        float nightPerc = DayTimeHelper.getCurrentDaytimeDistribution(world);

        Map<DrawnConstellation, List<Rectangle.Double>> cstCoordinates = new HashMap<>();
        for (DrawnConstellation drawnCst : constellations) {
            cstCoordinates.put(drawnCst, createConstellationOffsets(drawnCst));
        }

        Map<ResourceLocation, Float> distributionMap = new HashMap<>();
        for (DrawnConstellation drawn : cstCoordinates.keySet()) {
            List<Rectangle.Double> positions = cstCoordinates.get(drawn);
            Set<Rectangle.Double> foundPositions = new HashSet<>();

            for (DrawnConstellation otherCst : cstCoordinates.keySet()) {
                if (drawn.equals(otherCst) || drawn.getConstellation().equals(otherCst.getConstellation())) {
                    continue;
                }

                List<Rectangle.Double> otherPositions = cstCoordinates.get(otherCst);

                for (Rectangle.Double starPos : positions) {
                    for (Rectangle.Double otherStarPos : otherPositions) {
                        if (starPos.intersects(otherStarPos)) {
                            foundPositions.add(starPos);
                        }
                    }
                }
            }

            IConstellation drawnConstellation = drawn.getConstellation();
            float percent = 0.1F + 0.9F * Mth.clamp(((foundPositions.size() * 1.5F) / positions.size()) * nightPerc, 0F, 1F);
            float existingPercent = distributionMap.getOrDefault(drawnConstellation.getRegistryName(), 0.1F);
            if (percent >= existingPercent) {
                distributionMap.put(drawnConstellation.getRegistryName(), percent);
            }
        }
        return new EngravedStarMap(distributionMap, constellations);
    }

    private static List<Rectangle.Double> createConstellationOffsets(DrawnConstellation cst) {
        float width = DrawnConstellation.CONSTELLATION_STAR_SIZE;

        List<Rectangle.Double> positions = new ArrayList<>();
        for (StarLocation star : cst.getConstellation().getStars()) {
            double starX = star.x * DrawnConstellation.CONSTELLATION_SIZE_PART
                    + cst.getPoint().getX()
                    - DrawnConstellation.CONSTELLATION_DRAW_SIZE / 2F
                    - (width / 2F);
            double starY = star.y * DrawnConstellation.CONSTELLATION_SIZE_PART
                    + cst.getPoint().getY()
                    - DrawnConstellation.CONSTELLATION_DRAW_SIZE / 2F
                    - (width / 2F);

            positions.add(new Rectangle.Double(starX, starY, width, width));
        }
        return positions;
    }

    public boolean canAffect(@Nonnull ItemStack stack) {
        for (ResourceLocation key : this.getConstellationKeys()) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst != null) {
                EngravingEffect effect = cst.getEngravingEffect();
                if (effect != null && !effect.getApplicableEffects(stack).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    public ItemStack applyEffects(@Nonnull ItemStack stack) {
        List<EffectEntry> engravings = new ArrayList<>();
        for (ResourceLocation key : this.getConstellationKeys()) {
            IConstellation cst = ConstellationRegistry.getConstellation(key);
            if (cst == null) continue;
            EngravingEffect effect = cst.getEngravingEffect();
            if (effect == null) continue;
            List<EngravingEffect.ApplicableEffect> applicable = effect.getApplicableEffects(stack);
            if (applicable.isEmpty()) continue;
            float distribution = this.getDistribution(cst);
            for (EngravingEffect.ApplicableEffect applicableEffect : applicable) {
                engravings.add(new EffectEntry(applicableEffect, distribution));
            }
        }
        engravings.sort(Comparator.comparing(entry -> {
            if (entry.effect() instanceof EngravingEffect.EnchantmentEffect) {
                if (!((EngravingEffect.EnchantmentEffect) entry.effect()).isIgnoreCompatibility()) {
                    return 0;
                }
                return 1;
            }
            return 2;
        }));
        for (EffectEntry entry : engravings) {
            stack = entry.effect().apply(stack, entry.distribution(), rand);
        }
        return stack;
    }

    private record EffectEntry(EngravingEffect.ApplicableEffect effect, float distribution) {}

    public Collection<DrawnConstellation> getDrawnConstellations() {
        return Collections.unmodifiableCollection(this.drawInformation);
    }

    public Collection<ResourceLocation> getConstellationKeys() {
        return Collections.unmodifiableCollection(this.distributions.keySet());
    }

    public float getDistribution(IConstellation cst) {
        return this.distributions.getOrDefault(cst.getRegistryName(), 0F);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        distributions.forEach((constellationKey, percent) -> {
            CompoundTag cstTag = new CompoundTag();
            NBTHelper.setResourceLocation(cstTag, "cst", constellationKey);
            cstTag.putFloat("percent", percent);
            list.add(cstTag);
        });
        tag.put("distributions", list);
        ListTag listDrawn = new ListTag();
        drawInformation.forEach(drawCst -> {
            CompoundTag cstTag = new CompoundTag();
            NBTHelper.setResourceLocation(cstTag, "cst", drawCst.getConstellation().getRegistryName());
            cstTag.putInt("x", drawCst.getPoint().x);
            cstTag.putInt("y", drawCst.getPoint().y);
            listDrawn.add(cstTag);
        });
        tag.put("drawInformation", listDrawn);
        return tag;
    }

    public static EngravedStarMap deserialize(CompoundTag tag) {
        Map<ResourceLocation, Float> distributionMap = new HashMap<>();
        ListTag list = tag.getList("distributions", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag cstTag = list.getCompound(i);
            ResourceLocation constellationKey = NBTHelper.getResourceLocation(cstTag, "cst");
            float percent = cstTag.getFloat("percent");
            if (percent > 0) {
                distributionMap.put(constellationKey, percent);
            }
        }
        List<DrawnConstellation> drawnConstellations = new ArrayList<>();
        ListTag listDrawn = tag.getList("drawInformation", Tag.TAG_COMPOUND);
        for (int i = 0; i < listDrawn.size(); i++) {
            CompoundTag cstTag = listDrawn.getCompound(i);
            IConstellation cst = ConstellationRegistry.getConstellation(NBTHelper.getResourceLocation(cstTag, "cst"));
            Point offset = new Point(cstTag.getInt("x"), cstTag.getInt("y"));
            drawnConstellations.add(new DrawnConstellation(offset, cst));
        }
        return new EngravedStarMap(distributionMap, drawnConstellations);
    }
}
