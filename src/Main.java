import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;

public class Main {
    enum Color {
        BLUE(0),
        GREEN(1),
        RED(2),
        YELLOW(3);

        private final int value;

        Color(int value) {
            this.value = value;
        }
    }

    private static final int NB_SOLIDS = 4;
    private static final int NB_FACES_DISPLAYED = 4;
    private static final int OPPOSITE_FACE_DISTANCE = NB_FACES_DISPLAYED / 2;
    private static final int NB_FACES = 6;
    private static final int OPPOSITE_FACES_SUM = NB_FACES - 1;
    private static final Color[][] SOLIDS_COLORS = {
            {Color.YELLOW, Color.RED, Color.GREEN, Color.RED, Color.BLUE, Color.GREEN},
            {Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE, Color.BLUE, Color.GREEN},
            {Color.BLUE, Color.YELLOW, Color.YELLOW, Color.GREEN, Color.YELLOW, Color.RED},
            {Color.YELLOW, Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.RED},
    };

    static class Face {
        final IntVar number;
        final IntVar color;

        Face(IntVar number, IntVar color) {
            this.number = number;
            this.color = color;
        }
    }

    public static void main(String[] args) {
        Model model = new Model("Quatre cubes");

        Face[][] visibleFaces = new Face[NB_SOLIDS][NB_FACES_DISPLAYED];
        for (Face[] solid : visibleFaces) {
            Arrays.setAll(solid, face -> new Face(
                    model.intVar(0, NB_FACES - 1),
                    model.intVar(0, NB_FACES_DISPLAYED - 1)
            ));
        }

        Face[][] visibleFacesTransposed = new Face[NB_FACES_DISPLAYED][NB_SOLIDS];
        for (int noSolid = 0; noSolid < NB_SOLIDS; noSolid++) {
            for (int noFace = 0; noFace < NB_FACES_DISPLAYED; noFace++) {
                visibleFacesTransposed[noFace][noSolid] = visibleFaces[noSolid][noFace];
            }
        }

        for (int noSolid = 0; noSolid < NB_SOLIDS; noSolid++) {
            Tuples tuples = new Tuples();

            for (int noFaceOfSolid = 0; noFaceOfSolid < NB_FACES; noFaceOfSolid++) {
                tuples.add(noFaceOfSolid, SOLIDS_COLORS[noSolid][noFaceOfSolid].value);
            }

            for (int noFaceDisplayed = 0; noFaceDisplayed < NB_FACES_DISPLAYED; noFaceDisplayed++) {
                model.table(visibleFaces[noSolid][noFaceDisplayed].number, visibleFaces[noSolid][noFaceDisplayed].color, tuples).post();
            }

            for (int noFaceDisplayed = 0; noFaceDisplayed < OPPOSITE_FACE_DISTANCE; noFaceDisplayed++) {
                model.arithm(visibleFaces[noSolid][noFaceDisplayed].number, "+", visibleFaces[noSolid][noFaceDisplayed + OPPOSITE_FACE_DISTANCE].number, "=", OPPOSITE_FACES_SUM).post();
            }

            model.allDifferent(Arrays.stream(visibleFaces[noSolid]).map(face -> face.number).toArray(IntVar[]::new)).post();
        }

        for (int noLine = 0; noLine < NB_FACES_DISPLAYED; noLine++) {
            model.allDifferent(Arrays.stream(visibleFacesTransposed[noLine]).map(face -> face.color).toArray(IntVar[]::new)).post();
        }

        Solver solver = model.getSolver();
        solver.findSolution();

        for (int noSolid = 0; noSolid < NB_SOLIDS; noSolid++) {
            System.out.printf("   Cube %s    ", noSolid + 1);
        }
        System.out.println();

        for (Face[] line : visibleFacesTransposed) {
            for (Face face : line) {
                System.out.printf("|%-6s (%s)| ", Color.values()[face.color.getValue()], face.number.getValue() + 1);
            }
            System.out.println();
        }
        System.out.println();

        solver.printStatistics();
    }
}