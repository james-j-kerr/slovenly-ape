import java.io.IOException;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Running BasePSO:");
            Problem problem = new CarPricePrediction("train");
            BasePSO baseSolver = new BasePSO(problem);
            baseSolver.solve();
            System.out.println(baseSolver);

            System.out.println("Running NovelPSO:");
            NovelPSO novelSolver = new NovelPSO(problem);
            novelSolver.solve();
            System.out.println(novelSolver);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
