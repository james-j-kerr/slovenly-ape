import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CarPricePrediction implements Problem {

    public static final int N_INPUTS = 21;
    private static final int HIDDEN_LAYER_SIZE = 2;
    private static final int N_WEIGHTS = N_INPUTS * HIDDEN_LAYER_SIZE + HIDDEN_LAYER_SIZE * 1;
    private static final int N_BIASES = HIDDEN_LAYER_SIZE + 1;
    public static final int N_PARAMETERS = N_WEIGHTS + N_BIASES;

    private List<double[]> dataValues;
    private List<Double> prices;

    public CarPricePrediction(String dataset) throws IOException {
        if (Objects.equals("train", dataset)) load("data/train.csv");
        else if (Objects.equals("validation", dataset)) load("data/validation.csv");
        else if (Objects.equals("test", dataset)) load("data/test.csv");
        else throw new IllegalArgumentException("Must use either the 'train', 'validation' " +
                "or 'test' dataset only. Argument was '" + dataset + ".'");
    }

    private void load(String filePath) throws IOException {
        dataValues = new ArrayList<>();
        prices = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] lineData = line.split(",");

                if (lineData.length != N_INPUTS + 1) {
                    throw new RuntimeException("CarPricePrediction::load failed. A line in " +
                            "the dataset did not contain the correct number of entries.");
                }

                double[] metrics = new double[N_INPUTS];
                for (int i = 0; i < N_INPUTS; i++) {
                    metrics[i] = Double.parseDouble(lineData[i]);
                }
                dataValues.add(metrics);
                prices.add(Double.parseDouble(lineData[N_INPUTS]));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Permissible values for each dimension of the solution
     * @return bounds such that bounds[i][0] is the minimum permissible value
     * for the ith dimension, and bounds[i][1] the maximum permissible value
     */
    @Override
    public double[][] bounds() {
        double[][] bounds = new double[N_PARAMETERS][2];
        double[] boundDims = {-10.0, 10.0};
        for (int i = 0; i < N_PARAMETERS; i++) {
            bounds[i] = boundDims;
        }
        return bounds;
    }

    @Override
    public boolean validates(double[] params) {
        if (params.length != N_PARAMETERS) return false;
        double[][] bounds = bounds();
        for (int i = 0; i < N_PARAMETERS; i++) {
            if (params[i] < bounds[i][0] || params[i] > bounds[i][1]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double evaluate(double[] params) {
        double mse = 0.0;
        for (int i = 0; i < dataValues.size(); i++) {
            double prediction = predict(dataValues.get(i), params);
            mse += Math.pow(prices.get(i) - prediction, 2.0);
        }
        mse /= dataValues.size();
        return mse;
    }

    /**
     * Generate a valid solution that is within the constraints defined by the
     * problem
     * @return candidate which is a randomly generated, valid solution to the
     * problem
     */
    @Override
    public double[] generate() {
        double[] candidate = new double[dimensions()];
        for (int i = 0; i < dimensions(); i++) {
            candidate[i] = bounds()[i][0]
                    + (PSOData.RANDOM.nextDouble() * (bounds()[i][1] - bounds()[i][0]));
        }
        if (validates(candidate)) return candidate;
        else throw new RuntimeException("CarPricePrediction::generate produced an " +
                "invalid candidate solution.");
    }

    @Override
    public int dimensions() {
        return N_PARAMETERS;
    }

    private static double predict(double[] input, double[] params) {
        int weightPos = 0;
        int biasPos = N_WEIGHTS;
        double[] hiddenLayerValues = new double[HIDDEN_LAYER_SIZE];
        for (int i = 0; i < HIDDEN_LAYER_SIZE; i++) {
            double weightedSum = params[biasPos];
            biasPos++;
            for (int j = 0; j < N_INPUTS; j++) {
                weightedSum += input[j] * params[weightPos];
                weightPos++;
            }
            hiddenLayerValues[i] = relu(weightedSum);
        }
        double output = params[biasPos];
        for (int i = 0; i < HIDDEN_LAYER_SIZE; i++) {
            output += hiddenLayerValues[i] * params[weightPos];
            weightPos++;
        }
        return output;
    }

    // rectified linear unit activation function
    private static double relu (double value) {
        return value < 0 ? 0 : value;
    }
}
