package hellfirepvp.astralsorcery.common.constellation;

import java.awt.*;

public class DrawnConstellation {

    public static final int CONSTELLATION_DRAW_SIZE = 30;
    public static final float CONSTELLATION_SIZE_PART = (CONSTELLATION_DRAW_SIZE * 2F) / IConstellation.STAR_GRID_WIDTH_HEIGHT;
    public static final float CONSTELLATION_STAR_SIZE = CONSTELLATION_SIZE_PART * 2.5F;

    private final Point point;
    private final IConstellation constellation;

    public DrawnConstellation(Point point, IConstellation constellation) {
        this.point = point;
        this.constellation = constellation;
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    public Point getPoint() {
        return point;
    }
}
