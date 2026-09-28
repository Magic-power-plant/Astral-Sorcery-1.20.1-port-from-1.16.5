package hellfirepvp.astralsorcery.common.constellation.star;

import java.util.Objects;

public class StarConnection {
    public final StarLocation from;
    public final StarLocation to;

    public StarConnection(StarLocation from, StarLocation to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StarConnection that)) return false;
        return (Objects.equals(from, that.from) && Objects.equals(to, that.to)) ||
                (Objects.equals(from, that.to) && Objects.equals(to, that.from));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(from) ^ Objects.hashCode(to);
    }
}
