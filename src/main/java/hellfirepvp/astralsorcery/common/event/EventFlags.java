package hellfirepvp.astralsorcery.common.event;

public class EventFlags {

    public static boolean miningFlag = false;

    public static BooleanFlag SWEEP_ATTACK = new BooleanFlag(false);
    public static BooleanFlag LIGHTNING_ARC = new BooleanFlag(false);
    public static BooleanFlag MANTLE_DISCIDIA_ADDED = new BooleanFlag(false);

    public static BooleanFlag CHAIN_MINING = new BooleanFlag(false);
    public static BooleanFlag MINING_SIZE_BREAK = new BooleanFlag(false);
    public static BooleanFlag CHECK_BREAK_SPEED = new BooleanFlag(false);
    public static BooleanFlag CHECK_UNDERWATER_BREAK_SPEED = new BooleanFlag(false);
    public static BooleanFlag PLAY_BLOCK_BREAK_EFFECTS = new BooleanFlag(false);

    public static BooleanFlag SKY_RENDERING = new BooleanFlag(false);
    public static BooleanFlag GUI_CLOSING = new BooleanFlag(false);

    public static BooleanFlag CAN_HAVE_DYN_ENCHANTMENTS = new BooleanFlag(false);

    public static class BooleanFlag {

        private final boolean originalState;
        private boolean flag;

        private BooleanFlag(boolean flag) {
            this.flag = flag;
            this.originalState = flag;
        }

        public boolean isSet() {
            return originalState != flag;
        }

        public synchronized void executeWithFlag(Runnable run) {
            if (originalState == flag) {
                flag = !flag;
                try {
                    run.run();
                } finally {
                    flag = !flag;
                }
            }
        }
    }
}
