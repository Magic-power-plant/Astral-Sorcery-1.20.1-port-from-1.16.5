package hellfirepvp.astralsorcery.common.util;

import javax.annotation.Nullable;

public interface ServerLifecycleListener {

    public void onServerStart();

    public void onServerStop();

    public static ServerLifecycleListener stop(Runnable onStop) {
        return wrap(null, onStop);
    }

    public static ServerLifecycleListener start(Runnable onStart) {
        return wrap(onStart, null);
    }

    public static ServerLifecycleListener wrap(@Nullable Runnable onStart, @Nullable Runnable onStop) {
        return new ServerLifecycleListener() {
            @Override
            public void onServerStart() {
                if (onStart != null) {
                    onStart.run();
                }
            }

            @Override
            public void onServerStop() {
                if (onStop != null) {
                    onStop.run();
                }
            }
        };
    }

}
