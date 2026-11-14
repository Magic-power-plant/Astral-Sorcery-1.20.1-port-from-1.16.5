package hellfirepvp.astralsorcery.common.perk;

import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.perk.data.PerkTreeData;
import hellfirepvp.astralsorcery.common.perk.data.PreparedPerkTreeData;
import hellfirepvp.astralsorcery.common.perk.node.RootPerk;
import hellfirepvp.astralsorcery.common.perk.tree.PerkTreePoint;
import hellfirepvp.astralsorcery.common.util.object.SidedReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;



public class PerkTree {

    public static final PerkTree PERK_TREE = new PerkTree();

    private PerkTreeData loadedPerkTree;
    private final SidedReference<PreparedPerkTreeData> treeData = new SidedReference<>();

    private PerkTree() {}

    public Optional<PreparedPerkTreeData> getData(LogicalSide side) {
        return this.treeData.getData(side);
    }

    public Optional<Long> getVersion(LogicalSide side) {
        return this.getData(side).map(PreparedPerkTreeData::getVersion);
    }

    @Nullable
    public AbstractPerk getRootPerk(LogicalSide side, IMajorConstellation constellation) {
        return this.getData(side).map(data -> data.getRootPerk(constellation)).orElse(null);
    }

    public Optional<AbstractPerk> getPerk(LogicalSide side, ResourceLocation key) {
        return this.getPerk(side, perk -> key.equals(perk.getRegistryName()));
    }

    public Optional<AbstractPerk> getPerk(LogicalSide side, Predicate<AbstractPerk> test) {
        return this.getData(side).flatMap(data -> data.getPerk(test));
    }

    public Optional<? extends AbstractPerk> getPerk(LogicalSide side, float x, float y) {
        return this.getData(side).flatMap(data -> data.getPerk(x, y));
    }

    public boolean hasPerk(LogicalSide side, ResourceLocation key) {
        return this.getPerk(side, key).isPresent();
    }

    public Collection<AbstractPerk> getConnectedPerks(LogicalSide side, AbstractPerk perk) {
        return this.getData(side).map(data -> data.getConnectedPerks(perk)).orElse(Collections.emptyList());
    }

    public Collection<AbstractPerk> getPerkPoints(LogicalSide side) {
        return this.getData(side)
                .map(data -> data.getPerkPoints().stream()
                        .map(point -> (AbstractPerk) point.getPerk())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @OnlyIn(Dist.CLIENT)
    public Collection<Tuple<AbstractPerk, AbstractPerk>> getConnections() {
        return this.getData(LogicalSide.CLIENT).map(PreparedPerkTreeData::getConnections).orElse(Collections.emptyList());
    }

    public void updateOriginPerkTree(PerkTreeData perks) {
        this.loadedPerkTree = perks;
    }

    public Optional<Collection<JsonObject>> getLoginPerkData() {
        return Optional.ofNullable(this.loadedPerkTree).map(PerkTreeData::getAsDataTree);
    }

    @OnlyIn(Dist.CLIENT)
    public void receivePerkTree(PreparedPerkTreeData serverTreeData) {
        this.updateTreeData(LogicalSide.CLIENT, serverTreeData);
    }

    public void clearCache(LogicalSide side) {
        this.getData(side).ifPresent(data -> data.clearPerkCache(side));
        this.updateTreeData(side, null);
    }

    public void setupServerPerkTree() {
        if (this.loadedPerkTree != null) {
            this.updateTreeData(LogicalSide.SERVER, this.loadedPerkTree.prepare());
            AstralSorcery.LOG.info("Loaded PerkTree!");
        } else {
            AstralSorcery.LOG.info("No PerkTree data found!");
        }
    }

    private void updateTreeData(LogicalSide side, @Nullable PreparedPerkTreeData newData) {
        this.getData(side).ifPresent(data -> data.getPerkPoints().stream()
                .map(PerkTreePoint::getPerk)
                .forEach(perk -> perk.invalidate(side)));
        this.treeData.setData(side, newData);
        if (newData != null) {
            newData.getPerkPoints().stream()
                    .map(PerkTreePoint::getPerk)
                    .forEach(perk -> perk.validate(side));
        }
    }

}
