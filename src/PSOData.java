import java.util.Random;

public class PSOData {
    public static final double DEFAULT_SOCIAL_COEFF = 0.5;
    public static final double DEFAULT_COGNITIVE_COEFF = 0.5;
    public static final int DEFAULT_FUNCTION_EVALUATIONS = 1000;
    public static final double DEFAULT_INERTIA = 1.0 / 2.0 * Math.log(2.0);
    public static final int BASE_POPULATION = 20;
    public static final double DEFAULT_CROSSOVER_RATE = 0.25;
    public static final double DEFAULT_MUTATE_RATE = 0.1;
    public static final int DEFAULT_MATING_POOL_SCALE = 3;
    public static final int DEFAULT_K = 6;
    public static final Random RANDOM = new Random();


    public static double computeSocial() {
        return computeSocial(DEFAULT_SOCIAL_COEFF);
    }

    public static double computeSocial(double socialCoeff) {
        return compute(socialCoeff);
    }

    public static double computeCognitive() {
        return computeCognitive(DEFAULT_COGNITIVE_COEFF);
    }

    public static double computeCognitive(double cognitiveCoeff) {
        return compute(cognitiveCoeff);
    }

    private static double compute(double coeff) {
        return coeff + Math.log(2.0);
    }
}
