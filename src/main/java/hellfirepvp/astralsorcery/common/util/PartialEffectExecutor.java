package hellfirepvp.astralsorcery.common.util;

import java.util.Random;

public class PartialEffectExecutor {

    private static final Random random = new Random();

    private final Random rand;
    private final float amount;
    private float currentAmount;

    public PartialEffectExecutor(float amount) {
        this(amount, random);
    }

    public PartialEffectExecutor(float amount, Random rand) {
        this.rand = rand;
        this.amount = amount;
        this.currentAmount = amount;
    }

    public boolean canExecute() {
        return currentAmount > 1 || rand.nextFloat() < currentAmount;
    }

    public void markExecution() {
        currentAmount -= 1F;
    }

    public void reset() {
        this.currentAmount = this.amount;
    }

    public boolean executeAll(Runnable run) {
        boolean ranAtLeastOnce = false;
        while (canExecute()) {
            markExecution();
            run.run();
            ranAtLeastOnce = true;
        }
        return ranAtLeastOnce;
    }
}
