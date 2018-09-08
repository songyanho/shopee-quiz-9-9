import java.util.*;

public class ShopeeQuiz {
    private int[][] map = new int[][] {
            {0, 0, 2, 1, 0, 0, 0, 0, 0, 0},
            {2, 0, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 2, 0, 2, 0, 0, 1, 0, 0, 0},
            {1, 0, 0, 0, 0, 2, 0, 0, 1 ,2},
            {1, 0, 0, 0, 0, 1, 2, 0, 2, 0},
            {1, 0, 1, 0, 2, 0, 0, 0, 0, 1},
            {2, 2, 0, 0, 2, 0, 0, 0, 0, 2},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {2, 0, 0, 1, 0, 2, 0, 1, 0, 2},
            {2, 0, 0, 0, 1, 0, 2, 0, 0, 1}
    };

    private static List<Tuple> movements = Arrays.asList(
            new Tuple(-1, 0), // Left
            new Tuple(1, 0), // Right
            new Tuple(0, 1), // Up
            new Tuple(0, -1) // Down
    );

    private HashMap<Tuple, HashMap<Tuple, Integer>> fromToDistance = new HashMap<>();
    private HashSet<Tuple> visitedPetrols = new HashSet<>();
    private ArrayList<Tuple> visitingPetrols = new ArrayList<>();
    private Tuple origin = new Tuple(0, 0);

    private int minDistance = 1000;
    private static int stepsAllowed = 7;

    private ShopeeQuiz() {
        this.visitingPetrols.add(origin);
        this.moveToNextPetrolStation();
    }

    private void moveToNextPetrolStation() {
        while (!visitingPetrols.isEmpty()) {
            Tuple nextStop = visitingPetrols.remove(0);
            visitedPetrols.add(nextStop);
            HashSet<Tuple> options = nextPetrolKioks(nextStop);
            for (Tuple option: options) {
                if (visitedPetrols.contains(option)) {
                    for (Tuple t: visitedPetrols) {
                        if (t.equals(option)) {
                            t.addFrom(option.from);
                        }
                    }
                }
            }
            options.removeAll(visitedPetrols);
            visitingPetrols.addAll(options);
        }
    }

    private HashSet<Tuple> nextPetrolKioks(Tuple from) {
        HashMap<Tuple, Integer> availableSteps = exploreStates(from, ShopeeQuiz.stepsAllowed);
        HashSet<Tuple> petrolKioks = new HashSet<>();
        for (Map.Entry pair: availableSteps.entrySet()) {
            Tuple option = (Tuple) pair.getKey();
            Integer distance = (Integer) pair.getValue();
            if (this.map[option.y][option.x] == 1) {
                petrolKioks.add(option);
                if (this.fromToDistance.getOrDefault(from, null) == null) {
                    this.fromToDistance.put(from, new HashMap<>());
                }
                this.fromToDistance.get(from).put(option, distance);
            }
        }
        return petrolKioks;
    }

    private HashMap<Tuple, Integer> exploreStates(Tuple from, int movesLeft) {
        if (movesLeft <= 0) {
            return new HashMap<>();
        }
        HashMap<Tuple, Integer> nextSteps = new HashMap<>();
        for (Tuple movement: movements) {
            Tuple nextStep = move(from, movement);
            if (nextStep != null) {
                nextSteps.put(nextStep, ShopeeQuiz.stepsAllowed - movesLeft + 1);
                for (Map.Entry pair: exploreStates(nextStep, movesLeft - 1).entrySet()) {
                    Tuple key = (Tuple) pair.getKey();
                    Integer value = (Integer) pair.getValue();
                    if (nextSteps.containsKey(key)) {
                        if (nextSteps.get(key) > value) {
                            nextSteps.put(key, value);
                        }
                    } else {
                        nextSteps.put(key, value);
                    }
                }
            }
        }
        // Remove self
        nextSteps.remove(from);
        return nextSteps;
    }

    private Tuple move(Tuple from, Tuple movement) {
        int targetX = from.x + movement.x;
        int targetY = from.y + movement.y;
        if (!ShopeeQuiz.validState(targetX, targetY)) {
            return null;
        }
        try {
            if (map[targetY][targetX] == 2) {
                return null;
            }
        } catch (Exception e) {
            System.out.format("%d - %d\n", targetX, targetY);
            throw e;
        }
        return new Tuple(targetX, targetY, from);
    }

    private static boolean validState(int x, int y) {
        return !((x < 0 || x >= 10) || (y < 0 || y >= 10));
    }

    private void generatePath() {
        ArrayList<Tuple> path = new ArrayList<>();
        path.add(origin);
        nextPoint(path, 0);
    }

    private void nextPoint(ArrayList<Tuple> stops, int distance) {
        Tuple lastStop = stops.get(stops.size() - 1);
        if (lastStop.x == 9 && lastStop.y == 9) {
            if (distance <= minDistance) {
                for (Tuple s : stops) {
                    System.out.format("(%d,%d) - ", s.x, s.y);
                }
                System.out.format("Distance: %d \n", distance);
                minDistance = distance;
            }
        }
        for (Tuple nextStop: this.fromToDistance.getOrDefault(lastStop, new HashMap<>()).keySet()) {
            if (stops.contains(nextStop)) {
                continue;
            }
            ArrayList<Tuple> latestStops = new ArrayList<>(stops);
            latestStops.add(nextStop);
            nextPoint(latestStops, distance + fromToDistance.get(lastStop).get(nextStop));
        }
    }

    public static void main(String[] args) {
        ShopeeQuiz quiz = new ShopeeQuiz();
        quiz.generatePath();
    }
}

class Tuple {
    int x;
    int y;
    HashSet<Tuple> from = new HashSet<>();

    Tuple(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Tuple(int x, int y, Tuple from) {
        this.x = x;
        this.y = y;
        this.from.add(from);
    }

    public void addFrom(HashSet<Tuple> froms) {
        this.from.addAll(froms);
    }
    @Override
    public int hashCode() {
        return this.x * 10 + this.y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tuple) {
            Tuple objT = (Tuple)obj;
            return objT.x == this.x && objT.y == this.y;
        }
        return false;
    }
}
