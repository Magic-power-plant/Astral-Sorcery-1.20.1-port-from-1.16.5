package hellfirepvp.astralsorcery.common.util.loot;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class LootCollector implements Consumer<ItemStack> {

    private final Consumer<ItemStack> chaining;
    private List<ItemStack> collectedOutput = new ArrayList<>();

    public LootCollector(Consumer<ItemStack> chaining) {
        this.chaining = chaining;
    }

    @Override
    public void accept(ItemStack stack) {
        this.collectedOutput.add(stack);
    }

    public List<ItemStack> getCollectedOutput() {
        return Collections.unmodifiableList(this.collectedOutput);
    }

    public void setCollectedOutput(List<ItemStack> collectedOutput) {
        this.collectedOutput = collectedOutput;
    }

    public void flush() {
        this.getCollectedOutput().forEach(this.chaining);
    }
}
