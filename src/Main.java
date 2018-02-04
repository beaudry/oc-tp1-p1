import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

public class Main {
    private static final int NB_SOLIDS = 4;
    private static final int NB_FACES_DISPLAYED = 4;
    private static final int NB_FACES = 6;
    // 0 = blue, 1 = green, 2 = red, 3 = yellow
    private static final int[][] SOLIDS = {
            {3, 2, 1, 2, 0, 1},
            {1, 2, 3, 0, 0, 1},
            {0, 3, 3, 1, 3, 2},
            {3, 2, 0, 3, 1, 2},
    };

    public static void main(String[] args) {
        Model model = new Model("Quatre cubes");

        IntVar[][] visibleFaces = model.intVarMatrix(NB_SOLIDS, NB_FACES_DISPLAYED, 0, NB_FACES - 1);
        IntVar[][] visibleColors = model.intVarMatrix(NB_SOLIDS, NB_FACES_DISPLAYED, 0, NB_FACES_DISPLAYED - 1);

        IntVar[][] visibleFacesTransposed = new IntVar[NB_SOLIDS][NB_FACES_DISPLAYED];
        IntVar[][] visibleColorsTransposed = new IntVar[NB_SOLIDS][NB_FACES_DISPLAYED];

        for (int i = 0; i < NB_SOLIDS; i++) {
            for (int j = 0; j < NB_FACES_DISPLAYED; j++) {
                visibleFacesTransposed[j][i] = visibleFaces[i][j];
                visibleColorsTransposed[j][i] = visibleColors[i][j];
            }
        }

        for (int i = 0; i < NB_SOLIDS; i++) {
            Tuples tuples = new Tuples();

            for (int j = 0; j < NB_FACES; j++) {
                tuples.add(j, SOLIDS[i][j]);
            }

            for (int j = 0; j < NB_FACES_DISPLAYED; j++) {
                model.table(visibleFaces[i][j], visibleColors[i][j], tuples).post();
            }

            for (int j = 0; j < NB_FACES_DISPLAYED / 2; j++) {
                model.arithm(visibleFaces[i][j], "+", visibleFaces[i][j + 2], "=", 5).post();
            }

            model.allDifferent(visibleFaces[i]).post();
            model.allDifferent(visibleColorsTransposed[i]).post();
        }

        Solver solver = model.getSolver();
        Solution solution = solver.findSolution();

        if (solution != null) {
            Arrays.stream(visibleFacesTransposed).forEach(solid -> {
                Arrays.stream(solid).forEach(face -> System.out.printf("%s ", face.getValue()));
                System.out.println();
            });

            System.out.println();

            Arrays.stream(visibleColorsTransposed).forEach(solid -> {
                Arrays.stream(solid).forEach(color -> System.out.printf("%s ", new char[]{'B', 'G', 'R', 'Y'}[color.getValue()]));
                System.out.println();
            });
            System.out.println();
        }

        solver.printStatistics();
    }
}
