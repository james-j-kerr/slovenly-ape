public interface Problem {

    double[][] bounds();

    /**
     * Determines if the proposed solution is valid
     * within the constraints of the problem
     * @param params
     * @return
     */
    boolean validates(double[] params);
    double evaluate(double[] params);
    double[] generate();
    int dimensions();
}