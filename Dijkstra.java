import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Dijkstra implements AIModule {
    private boolean isVisited[][];
    private double distance[][];
    private Point prev[][];

    private ArrayList<Point> fringe = new ArrayList<Point>();
    private ArrayList<Point> paths = new ArrayList<Point>();

    private Point getLowestDistancePoint() {
        double min = Double.MAX_VALUE;
        int index = -1;

        for (int i = 0; i < fringe.size(); i++) {
            if (distance[fringe.get(i).x][fringe.get(i).y] < min) {
                min = distance[fringe.get(i).x][fringe.get(i).y];
                index = i;
            }
        }

        return fringe.remove(index);
    }

    private List<Point> reconstructPath(final Point endPoint) {
        Point target = endPoint;
        paths.add(0, endPoint);

        while (true) {
            Point point = prev[target.x][target.y];
            if (point.x == -1 && point.y == -1) { // undefined
                return paths;
            }

            paths.add(0, point);
            target = point;
        }
    }

    /// Creates the path to the goal.
    public List<Point> createPath(final TerrainMap map) {
        final Point startPoint = map.getStartPoint();
        final Point endPoint = map.getEndPoint();

        // Initialization
        isVisited = new boolean[map.getWidth()][map.getHeight()];
        distance = new double[map.getWidth()][map.getHeight()];
        prev = new Point[map.getWidth()][map.getHeight()];

        for (int i = 0; i < map.getWidth(); i++) {
            for (int j = 0; j < map.getHeight(); j++) {
                isVisited[i][j] = false;
                distance[i][j] = Double.MAX_VALUE;
                prev[i][j] = new Point(-1, -1);
            }
        }

        distance[startPoint.x][startPoint.y] = 0;
        fringe.add(startPoint);

        while (!fringe.isEmpty()) {
            Point point = getLowestDistancePoint();
            isVisited[point.x][point.y] = true;

            if (point.x == endPoint.x && point.y == endPoint.y) // goal reached
                break;

            Point[] neighbors = map.getNeighbors(point);

            for (int i = 0; i < neighbors.length; i++) {
                if (isVisited[neighbors[i].x][neighbors[i].y] == true)
                    continue;

                double dist = map.getCost(point, neighbors[i]) + distance[point.x][point.y];
                if (dist < distance[neighbors[i].x][neighbors[i].y]) {
                    distance[neighbors[i].x][neighbors[i].y] = dist;
                    fringe.add(neighbors[i]);
                    prev[neighbors[i].x][neighbors[i].y] = point; // save parent
                }
            }
        }

        return reconstructPath(endPoint);
    }
}
