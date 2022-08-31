import java.util.ArrayList;
import java.util.List;

public class BasePSO {

    protected Problem problem;
    private double[] bestSolution;
    private double socialCoeff = PSOData.DEFAULT_SOCIAL_COEFF;
    private double cognitiveCoeff = PSOData.DEFAULT_COGNITIVE_COEFF;
    protected int runTime = PSOData.DEFAULT_FUNCTION_EVALUATIONS;
    private List<String> history = new ArrayList<>();

    public BasePSO(Problem p) {
        problem = p;
    }

    public void setSocialCoeff(double socialCoeff) {
        if (socialCoeff < 0) {
            throw new IllegalArgumentException("BasePSO::setSocialCoeff argument must be non-" +
                    "negative; it was " + socialCoeff + ".");
        }
        this.socialCoeff = socialCoeff;
    }

    public void setCognitiveCoeff(double cognitiveCoeff) {
        if (cognitiveCoeff < 0) {
            throw new IllegalArgumentException(("BasePSO::setCognitiveCoeff argument must be non-" +
                    "negative; it was " + cognitiveCoeff + "."));
        }
        this.cognitiveCoeff = cognitiveCoeff;
    }

    public void setRunTime(int runTime) {
        if (runTime < 1) {
            throw new IllegalArgumentException("BasePSO::setRunTime argument must be > 0; it was " +
                    runTime);
        }
        this.runTime = runTime;
    }

    public double[] getBest() {
        return bestSolution;
    }

    public void setBest(double[] best) {
        bestSolution = best;
    }

    protected void record(double[] currentBest) {
        history.add(String.valueOf(problem.evaluate(currentBest)));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String entry : history) {
            sb.append("=> " + entry + "\n");
        }
        return sb.toString();
    }

    public void solve() {
        ParticleSwarm particleSwarm = new ParticleSwarm();
        int ticks = runTime;
        while (ticks > 0) {
            particleSwarm.updatePopulation();
            particleSwarm.evaluatePopulation();
            record(particleSwarm.getGBest());
            ticks--;
        }
    }

    class Particle {

        private final double inertia = PSOData.DEFAULT_INERTIA;
        private final double cognitive = PSOData.computeCognitive(cognitiveCoeff);
        private final double social = PSOData.computeSocial(socialCoeff);
        private double[] pBest;
        private double[] velocity;
        private double[] position;
        private int length;

        /**
         * Particle which has a position within the dimensional space, a
         * pBest (personal best) which represents the historical position which
         * the Particle had the best fitness (or lowest error etc.) and the
         * velocity of the Particle which represent the velocity for the position
         * along each dimension.
         *
         * @param position the position of the Particle
         * @param pBest the 'personal best' of the Particle
         * @param velocity the velocity of the Particle
         */
        public Particle(double[] position, double[] pBest, double[] velocity) {
            if (position.length != pBest.length || position.length != velocity.length) {
                throw new IllegalArgumentException("Particle constructor arguments must be of equal" +
                        " length.");
            }
            this.length = position.length;
            this.position = new double[length];
            this.pBest = new double[length];
            this.velocity = new double[length];
            for (int i = 0; i < length; i++) {
                this.position[i] = position[i];
                this.pBest[i] = pBest[i];
                this.velocity[i] = velocity[i];
            }
        }

        public Particle(double[] initialPos, double[] initialOffset) {
            if (initialPos.length != initialOffset.length) {
                throw new IllegalArgumentException("Particle constructor arguments must be of equal " +
                        "length");
            }
            this.length = initialPos.length;
            this.position = new double[length];
            this.pBest = new double[length];
            this.velocity = new double[length];
            for (int i = 0; i < length; i++) {
                this.position[i] = initialPos[i];
                this.pBest[i] = initialPos[i];
                this.velocity[i] = (initialPos[i] - initialOffset[i]) / 3;
            }
        }

        public double[] getPosition() {
            return position;
        }

        public double[] getVelocity() {
            return velocity;
        }

        public double[] getPBest() {
            return pBest;
        }

        public void setPBest(double[] newBest) {
            if (pBest.length != newBest.length) {
                throw new IllegalArgumentException("Particle::setPBest argument length is " +
                        "not of the correct length.");
            }
            for (int i = 0; i < newBest.length; i++) {
                pBest[i] = newBest[i];
            }
        }

        public void updatePosition() {
            for (int i = 0; i < length; i++)  {
                position[i] += velocity[i];
            }
        }

        /**
         * Particle updates its trajectory by altering its velocity using
         * the gBest (global best position found by the swarm).
         * @param gBest
         */
        public void updateVelocity(double[] gBest)  {
            if (length != gBest.length) {
                throw new IllegalArgumentException("Particle::updateVelocity argument length is " +
                        "not of the correct length.");
            }
            for (int i = 0; i < length; i++) {
                velocity[i] =
                        inertia * velocity[i]
                        + cognitive * Math.random() * (pBest[i] - position[i])
                        + social * Math.random() * (gBest[i] - position[i]);
            }
        }
    }

    class ParticleSwarm {
        protected List<Particle> population;
        private double[] gBest;
        private int size;

        public ParticleSwarm() {
            size = PSOData.BASE_POPULATION + (int) Math.round(Math.sqrt(problem.dimensions()));
            population = new ArrayList<>();
            initialise();
        }

        private void initialise() {
            for (int i = 0; i < size; i++) {
                population.add(new Particle(problem.generate(), problem.generate()));
            }
            gBest = problem.generate();
        }

        public double[] getGBest() {
            return gBest;
        }

        public void setGBest(double[] newGBest) {
            if (gBest.length != newGBest.length) {
                throw new IllegalArgumentException("ParticleSwarm::setGBest argument was not" +
                        " of the correct length");
            }
            for (int i = 0; i < newGBest.length; i++) {
                gBest[i] = newGBest[i];
            }
        }

        protected void evaluatePopulation() {
            for (Particle particle : population) {
                double[] currentPos = particle.getPosition();
                double[] currentBest = particle.getPBest();

                // "invisible wall" -- if the Particle does not satisfy the constraints
                // of the problem, do not evaluate it.
                if (!problem.validates(currentPos)) continue;

                // check if the Particle current position is the best position it has found -
                // i.e. better than its personal best
                if (problem.evaluate(currentPos) < problem.evaluate(currentBest)) {
                    particle.setPBest(currentPos);
                }

                // check if the Particle current position is the best position the
                // population has ever found
                if (problem.evaluate(currentPos) < problem.evaluate(gBest)) {
                    setGBest(currentPos);
                }
            };
        }

        protected void updatePopulation() {
            for (Particle particle : population) {
                particle.updateVelocity(gBest);
                particle.updatePosition();
            }
        }
    }
}
