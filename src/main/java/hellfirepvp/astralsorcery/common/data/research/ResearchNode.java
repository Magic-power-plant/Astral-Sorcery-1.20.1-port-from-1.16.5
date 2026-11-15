package hellfirepvp.astralsorcery.common.data.research;

import net.minecraft.resources.ResourceLocation;
import hellfirepvp.astralsorcery.common.auxiliary.book.BookLookupRegistry;
import hellfirepvp.astralsorcery.common.data.journal.JournalPage;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
//java.awt.Color specifically, not java.awt.*: this class also imports java.util.*, and java.awt.List would
//make every List reference here ambiguous.
import java.awt.Color;
import java.util.*;

public class ResearchNode {

    private static int counter = 0;

    private final int id;
    private final NodeRenderType nodeRenderType;
    public final float renderPosX, renderPosZ;
    private final String unlocName;

    private ItemStack[] renderItemStacks;
    private SpriteIcon renderSpriteIcon;
    private Color textureColorHint = new Color(0xFFFFFFFF, true);

    private final List<ResearchNode> connectionsTo = new ArrayList<>();
    private final List<JournalPage> pages = new LinkedList<>();

    private ResearchNode(NodeRenderType type, String unlocName, float rPosX, float rPosZ) {
        this.id = counter;
        counter++;
        this.nodeRenderType = type;
        this.renderPosX = rPosX;
        this.renderPosZ = rPosZ;
        this.unlocName = unlocName;
    }

    public ResearchNode(ItemLike item, String unlocName, float renderPosX, float renderPosZ) {
        this(new ItemStack(item), unlocName, renderPosX, renderPosZ);
    }

    public ResearchNode(ItemStack itemStack, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = new ItemStack[] { itemStack };
    }

    public ResearchNode(ItemLike[] items, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            this.renderItemStacks[i] = new ItemStack(items[i]);
        }
    }

    public ResearchNode(ItemStack[] stacks, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.ITEMSTACK, unlocName, renderPosX, renderPosZ);
        this.renderItemStacks = stacks;
    }

    //1.16.5 ResearchNode.java:83-86. The only NodeRenderType.TEXTURE_SPRITE producer; without it that enum
    //constant was unreachable and the QUICK_CHARGE node could not be expressed.
    public ResearchNode(SpriteIcon icon, String unlocName, float renderPosX, float renderPosZ) {
        this(NodeRenderType.TEXTURE_SPRITE, unlocName, renderPosX, renderPosZ);
        this.renderSpriteIcon = Objects.requireNonNull(icon);
    }

    //1.16.5 ResearchNode.java:115-118 / :135-137
    public ResearchNode setTextureColorHintWithAlpha(Color textureColorHint) {
        this.textureColorHint = textureColorHint;
        return this;
    }

    public Color getTextureColorHint() {
        return textureColorHint;
    }

    public SpriteIcon getSpriteTexture() {
        return renderSpriteIcon;
    }

    public ResearchNode addSourceConnectionFrom(ResearchNode node) {
        this.connectionsTo.add(node);
        return this;
    }

    public ResearchNode addSourceConnectionFrom(ResearchNode... node) {
        return addSourceConnectionsFrom(Arrays.asList(node));
    }

    public ResearchNode addSourceConnectionsFrom(Collection<ResearchNode> node) {
        this.connectionsTo.addAll(node);
        return this;
    }

    public List<ResearchNode> getConnectionsTo() {
        return connectionsTo;
    }

    public ResearchNode addPage(JournalPage page) {
        pages.add(page);
        return this;
    }

    public boolean canSee(@Nullable PlayerProgress progress) {
        return true;
    }

    public ResearchNode register(ResearchProgression progression) {
        progression.getRegistrar().accept(this);
        return this;
    }

    public ResearchNode addTomeLookup(ItemLike item, int nodePage, ResearchProgression progression) {
        BookLookupRegistry.registerItemLookup(item, this, nodePage, progression);
        return this;
    }

    public ResearchNode addTomeLookup(ItemStack item, int nodePage, ResearchProgression progression) {
        BookLookupRegistry.registerItemLookup(item, this, nodePage, progression);
        return this;
    }

    public NodeRenderType getNodeRenderType() {
        return nodeRenderType;
    }

    public ItemStack getRenderItemStack() {
        return getRenderItemStack(0);
    }

    public ItemStack getRenderItemStack(long tick) {
        return renderItemStacks[((int) ((tick / 40) % renderItemStacks.length))];
    }

    public List<JournalPage> getPages() {
        return pages;
    }

    public Component getName() {
        return Component.translatable(String.format("astralsorcery.journal.node.%s.name", this.getKey()));
    }

    public String getKey() {
        return this.unlocName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResearchNode that = (ResearchNode) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    /** A sprite-sheet description, with no resource loading or rendering behavior. */
    public record SpriteIcon(ResourceLocation texture, int rows, int columns) {
        public SpriteIcon {
            Objects.requireNonNull(texture, "texture");
            if (rows <= 0 || columns <= 0) throw new IllegalArgumentException("Sprite dimensions must be positive");
        }
    }

    public static enum NodeRenderType {
        ITEMSTACK, TEXTURE_SPRITE
    }

}
