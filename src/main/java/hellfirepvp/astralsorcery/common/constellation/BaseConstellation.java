package hellfirepvp.astralsorcery.common.constellation;

import hellfirepvp.astralsorcery.common.constellation.star.StarConnection;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseConstellation implements IConstellation {
    private final List<StarLocation> starLocations = new ArrayList<>();
    private final List<StarConnection> connections = new ArrayList<>();

    @Override
    public StarLocation addStar(int x, int y) {
        x %= STAR_GRID_INDEX;
        y %= STAR_GRID_INDEX;
        StarLocation star = new StarLocation(x, y);
        if (!starLocations.contains(star)) {
            starLocations.add(star);
            return star;
        }
        return null;
    }

    @Override
    public StarConnection addConnection(StarLocation star1, StarLocation star2) {
        if (star1 == null || star2 == null || star1.equals(star2)) {
            return null;
        }
        StarConnection connection = new StarConnection(star1, star2);
        if (!connections.contains(connection)) {
            connections.add(connection);
            return connection;
        }
        return null;
    }

    @Override
    public List<StarLocation> getStars() {
        return Collections.unmodifiableList(this.starLocations);
    }

    @Override
    public List<StarConnection> getStarConnections() {
        return Collections.unmodifiableList(this.connections);
    }
}
