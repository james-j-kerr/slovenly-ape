import java.util.*;

public class NovelPSO extends BasePSO {

    private double crossOverRate;
    private double mutateRate;
    private int matingPoolScale;
    private int k;

    public NovelPSO(Problem problem) {
        super(problem);
        crossOverRate = PSOData.DEFAULT_CROSSOVER_RATE;
        mutateRate = PSOData.DEFAULT_MUTATE_RATE;
        matingPoolScale = PSOData.DEFAULT_MATING_POOL_SCALE;
        k = PSOData.DEFAULT_K;
    }

    public void setCrossOverRate(double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("NovelPSO::setCrossOverRate argument must be 0 or greater.");
        }
        crossOverRate = rate;
    }

    public void setMutateRate(double rate) {
        if (rate < 0) {
            throw new IllegalArgumentException("NovelPSO::setCrossOverRate argument must be 0 or greater.");
        }
        mutateRate = rate;
    }

    public void setTournamentSize(int kSize) {
        k = kSize;
    }

    public void setMatingPoolScale(int scale) {
        matingPoolScale = scale;
    }

    @Override
    public void solve() {
        EvolvingParticleSwarm particleSwarm = new EvolvingParticleSwarm();
        int ticks = runTime;
        while (ticks > 0) {
            particleSwarm.updatePopulation();
            particleSwarm.evaluatePopulation();
            record(particleSwarm.getGBest());
            particleSwarm.evolvePopulation();
            ticks--;
        }
    }

    class EvolvingParticleSwarm extends ParticleSwarm {

        private boolean mutate() {
            return PSOData.RANDOM.nextDouble() < mutateRate;
        }

        private boolean crossOver() {
            return PSOData.RANDOM.nextDouble() < crossOverRate;
        }

        private void evolvePopulation() {
            List<Particle> parents = selection();
            List<Particle> offspring = new ArrayList<>();
            Collections.shuffle(parents);

            if (parents.size() % 2 != 0) {
                parents.remove(parents.size() - 1);
            }

            for (int i = 0; i < parents.size(); i += 2) {
                List<Particle> children = Arrays.asList(parents.get(i), parents.get(i + 1));
                if (crossOver()) {
                    List<double[]> positions = blendedCrossOver(
                            children.get(0).getPosition(),
                            children.get(1).getPosition());

                    Particle offspringA = new Particle(
                            positions.get(0), children.get(0).getPBest(), children.get(0).getVelocity());
                    Particle offspringB = new Particle(
                            positions.get(1), children.get(1).getPBest(), children.get(1).getVelocity());
                    children.set(0, offspringA);
                    children.set(1, offspringB);
                }
                else if (mutate()) {
                    children.set(0, mutation(children.get(0)));
                    children.set(1, mutation(children.get(1)));
                }
                offspring.addAll(children);
            }

            population = getNFittest(offspring, population.size());
        }

        private List<Particle> selection() {
            int pool = population.size() * matingPoolScale;
            List<Particle> matingPool = new ArrayList<>(pool);
            for (int i = 0; i < pool; i++) {
                List<Particle> tournament = new ArrayList<>(k);
                for (int j = 0; j < k; j++) {
                    int pos = PSOData.RANDOM.nextInt(population.size());
                    tournament.add(population.get(pos));
                }
                matingPool.add(getFittest(tournament));
            }
            return matingPool;
        }

        private Particle mutation(Particle particle) {
            double[] position = particle.getPosition();
            int mutationIndex = PSOData.RANDOM.nextInt(problem.dimensions());
            position[mutationIndex] =
                    PSOData.RANDOM.nextDouble(problem.bounds()[mutationIndex][0], problem.bounds()[mutationIndex][1]);
            return new Particle(position, particle.getPBest(), particle.getVelocity());
        }

        private List<double[]> blendedCrossOver(double[] parentA, double[] parentB) {
            List<double[]> results = new ArrayList<>();
            int cutPoint = PSOData.RANDOM.nextInt(problem.dimensions());
            double[] offspringA = new double[problem.dimensions()];
            double[] offspringB = new double[problem.dimensions()];
            
            for (int i = 0; i < cutPoint; i++) {
                offspringA[i] = parentA[i];
                offspringB[i] = parentB[i];
            }

            for (int j = cutPoint; j < problem.dimensions(); j++) {
                double[] origins = new double[] {parentA[j], parentB[j]};
                Arrays.sort(origins);

                double difference = origins[1] - origins[0];
                if (difference == 0.0) {
                    origins[0] = problem.bounds()[j][0];
                    origins[1] = problem.bounds()[j][1];
                }

                offspringA[j] = PSOData.RANDOM.nextDouble(
                        origins[0] - 0.5 * difference, origins[1] + 0.5 * difference);
                offspringB[j] = PSOData.RANDOM.nextDouble(
                        origins[0] - 0.5 * difference, origins[1] + 0.5 * difference);
            }

            results.add(offspringA);
            results.add(offspringB);
            return results;
        }

        private Particle getFittest(List<Particle> particles) {
            double bestFitness = Double.MAX_VALUE;
            Particle fittest = null;
            for (Particle particle: particles) {
                double currentFitness = problem.evaluate(particle.getPBest());
                if (currentFitness < bestFitness) {
                    bestFitness = currentFitness;
                    fittest = particle;
                }
            }
            return fittest;
        }

        private List<Particle> getNFittest(List<Particle> particles, int n) {
            Collections.sort(particles,
                    Comparator.comparingDouble(particle -> problem.evaluate(particle.getPBest())));
            return particles.subList(0, n);
        }
    }
}
