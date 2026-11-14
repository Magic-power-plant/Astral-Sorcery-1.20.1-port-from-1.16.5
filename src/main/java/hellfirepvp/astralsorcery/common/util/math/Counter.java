package hellfirepvp.astralsorcery.common.util.math;

public class Counter {

    private int value;

    public Counter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void decrement() {
        value--;
    }

    public void increment() {
        value++;
    }

}
