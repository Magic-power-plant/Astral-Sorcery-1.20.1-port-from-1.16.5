package hellfirepvp.astralsorcery.common.constellation.star;

import java.awt.Point;
import java.util.Objects;

public class StarLocation {
    public final int x;
    public final int y;

    public StarLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getDistanceToOrigin() {
        return x + y;
    }

    public Point asPoint() {
        return new Point(x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StarLocation that)) return false;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
