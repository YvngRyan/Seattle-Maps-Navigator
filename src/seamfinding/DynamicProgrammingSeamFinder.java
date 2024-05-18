package seamfinding;

import seamfinding.energy.EnergyFunction;

import java.util.List;
import java.util.*;

/**
 * Dynamic programming implementation of the {@link SeamFinder} interface.
 *
 * @see SeamFinder
 */
public class DynamicProgrammingSeamFinder implements SeamFinder {

    @Override
    public List<Integer> findHorizontal(Picture picture, EnergyFunction f) {
        int width = picture.width();
        int height = picture.height();

        double[][] array = new double[width][height];

        //Fil out the leftmost column in the 2-d array
        for (int h = 0; h < height; h++) {
            array[0][h] = f.apply(picture, 0, h);
        }

        //Fill out the rest of the remaining columns, determine the lowest-energy predecessor to the pixel
        for (int w = 1; w < width; w++) {
            for (int h = 0; h < height; h++) {
                array[w][h] = f.apply(picture, w, h);

                double minPrev = Double.POSITIVE_INFINITY;
                for (int neighbor = h - 1; neighbor <= h + 1; neighbor++) {
                    if (neighbor >= 0 && neighbor < height) {
                        minPrev = Math.min(minPrev, array[w - 1][neighbor]);
                    }
                }

                array[w][h] += minPrev;
            }
        }

        // Find ending pixel with min path cost
        int bestPath = 0;
        for (int i = 1; i < height; i++) {
            if (array[width - 1][i] < array[width - 1][bestPath]) {
                bestPath = i;
            }
        }

        // Reconstruct path
        List<Integer> seam = new ArrayList<>(width);
        seam.add(bestPath);

        for (int i = width - 1; i > 0; i--) {
            int bestK = bestPath;
            for (int k = bestPath - 1; k <= bestPath + 1; k++) {
                if (k >= 0 && k < height && array[i - 1][k] < array[i - 1][bestK]) {
                    bestK = k;
                }
            }
            bestPath = bestK;
            seam.add(bestPath);
        }

        Collections.reverse(seam);
        return seam;
    }
}
