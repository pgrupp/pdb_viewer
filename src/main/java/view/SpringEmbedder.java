package view;

/**
 * simple spring embedder
 * Created by huson on 10/31/15.
 */
public class SpringEmbedder {
    /**
     * Computes a spring embedding of a graph
     *
     * @param iterations         number of iterations
     * @param numberOfNodes      number of nodes
     * @param edges              edges as pairs of node ids (between 0 and numberOfNodes-1)
     * @param initialCoordinates initial coordinates or null. If given, must not be constant
     */
    public static double[][] computeSpringEmbedding(int iterations, int numberOfNodes, int[][] edges, double[][] initialCoordinates) {
        if (numberOfNodes < 2)
            return new double[][]{{0.0, 0.0}};

        final int width; // approximate window width
        final int height; // approximate window height

        final double[][] coordinates = new double[numberOfNodes][2];
        if (initialCoordinates != null) {
            System.arraycopy(initialCoordinates, 0, coordinates, 0, initialCoordinates.length);
            double minX=10000000;
            double maxX=-10000000;
            double minY=10000000;
            double maxY=-10000000;
            for (double[] coordinate : coordinates) {
                minX = Math.min(coordinate[0], minX);
                maxX = Math.max(coordinate[0], maxX);
                minY = Math.min(coordinate[0], minY);
                maxY = Math.max(coordinate[0], maxY);
            }
            width=(int)(maxX-minX);
            height=(int)(maxY-minY);
        } else {
            // position on a circle of radius "width/2"
            width = 200;
            height = 200;

            int count = 0;
            for (int v = 0; v < numberOfNodes; v++) {
                coordinates[v][0] = (float) (0.5*width * Math.sin(2.0 * Math.PI * (double) count / (double) numberOfNodes));
                coordinates[v][1] = (float) (0.5*height * Math.cos(2.0 * Math.PI * (double) count / (double) numberOfNodes));
                count++;
            }
        }

        // compute node degrees
        final int[] deg = new int[numberOfNodes];
        for (int[] e : edges) {
            deg[e[0]]++;
            deg[e[1]]++;
        }

        // run iterations of spring embedding:
        final double log2 = Math.log(2);

        for (int count = 1; count < iterations; count++) {
            final double k = Math.sqrt(width * height / numberOfNodes) / 2;

            final double l2 = 25 * log2 * Math.log(1 + count);

            final double tx = width / l2;
            final double ty = height / l2;

            final double[][] array = new double[numberOfNodes][2];

            // repulsions

            for (int v = 0; v < numberOfNodes; v++) {
                double xv = coordinates[v][0];
                double yv = coordinates[v][1];

                for (int u = 0; u < numberOfNodes; u++) {
                    if (u == v)
                        continue;
                    double xdist = xv - coordinates[u][0];
                    double ydist = yv - coordinates[u][1];
                    double dist = xdist * xdist + ydist * ydist;
                    if (dist < 1e-3)
                        dist = 1e-3;
                    double repulse = k * k / dist;
                    array[v][0] += repulse * xdist;
                    array[v][1] += repulse * ydist;
                }

                for (int[] edge : edges) {
                    int a = edge[0];
                    int b = edge[1];
                    if (a == v || b == v)
                        continue;
                    double xdist = xv - (coordinates[a][0] + coordinates[b][0]) / 2;
                    double ydist = yv - (coordinates[a][1] + coordinates[b][1]) / 2;
                    double dist = xdist * xdist + ydist * ydist;
                    if (dist < 1e-3) dist = 1e-3;
                    double repulse = k * k / dist;
                    array[v][0] += repulse * xdist;
                    array[v][1] += repulse * ydist;
                }
            }

            // attractions

            for (int[] edge : edges) {
                final int u = edge[0];
                final int v = edge[1];

                double xdist = coordinates[v][0] - coordinates[u][0];
                double ydist = coordinates[v][1] - coordinates[u][1];

                double dist = Math.sqrt(xdist * xdist + ydist * ydist);

                dist /= ((deg[u] + deg[v]) / 16.0);

                array[v][0] -= xdist * dist / k;
                array[v][1] -= ydist * dist / k;

                array[u][0] += xdist * dist / k;
                array[u][1] += ydist * dist / k;
            }

            // exclusions

            for (int v = 0; v < numberOfNodes; v++) {
                double xd = array[v][0];
                double yd = array[v][1];

                final double dist = Math.sqrt(xd * xd + yd * yd);

                xd = tx * xd / dist;
                yd = ty * yd / dist;

                coordinates[v][0] += xd;
                coordinates[v][1] += yd;
            }
        }
        return coordinates;
    }

    /**
     * place coordinates into a desired rectangle
     *
     * @param coordinates coordinates
     * @param xMin        desired min x
     * @param xMax        desired max x
     * @param yMin        desired min y
     * @param yMax        desired max y
     */
    public static void centerCoordinates(double[][] coordinates, int xMin, int xMax, int yMin, int yMax) {
        double cxMin = Double.MAX_VALUE;
        double cxMax = Double.MIN_VALUE;
        double cyMin = Double.MAX_VALUE;
        double cyMax = Double.MIN_VALUE;

        for (double[] apt : coordinates) {
            cxMin = Math.min(cxMin, apt[0]);
            cxMax = Math.max(cxMax, apt[0]);
            cyMin = Math.min(cyMin, apt[1]);
            cyMax = Math.max(cyMax, apt[1]);
        }

        if ((cxMax - cxMin) != 0 && (cyMax - cyMin) != 0) {
            final double factor = (Math.min(((xMax - xMin) / (cxMax - cxMin)), ((yMax - yMin) / (cyMax - cyMin))));
            final double dX = ((xMax + xMin - factor * (cxMax + cxMin)) / 2);
            final double dY = ((yMax + yMin - factor * (cyMax + cyMin)) / 2);

            for (double[] apt : coordinates) {
                apt[0] = factor * apt[0] + dX;
                apt[1] = factor * apt[1] + dY;
            }
        }
    }
}
