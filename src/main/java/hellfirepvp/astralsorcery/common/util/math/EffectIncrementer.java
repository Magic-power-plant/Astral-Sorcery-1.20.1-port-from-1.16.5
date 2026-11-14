package hellfirepvp.astralsorcery.common.util.math;

import net.minecraft.util.Mth;

public class EffectIncrementer {

    private final int cap;
    private int current = 0;

    public EffectIncrementer(int max) {
        this.cap = max;
    }

    public void update(boolean increment) {
        if (increment) {
            this.current++;
        } else {
            this.current--;
        }
        this.current = Mth.clamp(this.current, 0, this.cap);
    }

    public int get() {
        return current;
    }

    public float getAsPercentage() {
        return this.current / ((float) this.cap);
    }
}
