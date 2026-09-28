package hellfirepvp.astralsorcery.common.util.tick;

import java.util.HashMap;

public class TokenMap<K, V extends TokenMap.MapToken<?>> extends HashMap<K, V> {

    public static interface MapToken<V> {
        public V getValue();
    }

}
