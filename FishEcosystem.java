import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FishEcosystem {
    public static void main(String[] args) {
        Ecosystem ecosystem = new Ecosystem(500, 40, 11, 120, 2, 1, 100, 18, 7, 6);
        ecosystem.simulate(100);
    }
}

abstract class Fish {
    String name;
    int population;
    int foodConsumption;
    int age;
    int maxAge;
    public Fish(String name, int population, int foodConsumption, int maxAge) {
        this.name = name;
        this.population = population;
        this.foodConsumption = foodConsumption;
        this.age = 0;
        this.maxAge = maxAge;
    }

    public abstract int feed(int foodAvailable);

    public abstract int reproduce();

    public void age() {
        age++;
        if (age >= maxAge) {
            population -= Math.round(population / 4)+1;
            age = 0;
            population = Math.max(population, 0);
        }
    }
}

class GroupAFish extends Fish {
    public GroupAFish(String name, int population, int foodConsumption,  int maxAge) {
        super(name, population, foodConsumption, maxAge);
    }

    @Override
    public int feed(int foodAvailable) {
        int totalFoodNeeded = population * foodConsumption;
        if (foodAvailable >= totalFoodNeeded) {
            return totalFoodNeeded;
        } else if (foodAvailable < 50) {
            population /= 5;
            return 0;
        } else {
            population = foodAvailable / foodConsumption;
            return foodAvailable;
        }
    }

    @Override
    public int reproduce() {
        long eggsProduced;
        eggsProduced = Math.round(population*1.5);
        population += eggsProduced;
        return (int)eggsProduced;
    }
}

class GroupBFish extends Fish {
    Random random = new Random();
    public GroupBFish(String name, int population, int foodConsumption, int maxAge) {
        super(name, population, foodConsumption, maxAge);
    }

    // uhhhh
    @Override
    public int feed(int unused) {
        return 0;
    }

    public int feed(List<Fish> preyFish) {
        int totalFoodNeeded = population * foodConsumption;
        int totalFoodAvailable = 0;

        for (Fish prey : preyFish) {
            if (prey.population > 0) {
                int foodTaken = Math.min(prey.population, totalFoodNeeded);
                if (prey.population / population > 6) {
                    prey.population = Math.max(0, prey.population - foodTaken * 3);
                } else {
                    int randomAdjustment = (foodTaken / 2 > 0) ? random.nextInt(Math.max(1, foodTaken)) : 0;
                    prey.population = Math.max(0, prey.population - foodTaken / 2 - randomAdjustment);
                }
                totalFoodAvailable += foodTaken;
                totalFoodNeeded -= foodTaken;

                if (totalFoodNeeded <= 0) break;
            }
        }
        if (totalFoodAvailable < 2) {
            population /= 5;
        }
        int adjustmentBound = Math.max(1, population / 3);
        int adjustment = random.nextInt(adjustmentBound);
        population = Math.max(0, population - adjustment);

        return totalFoodAvailable;
    }

    @Override
    public int reproduce() {
        int eggsProduced;
        eggsProduced = Math.round(population / 5);
        population += eggsProduced;
        return eggsProduced;
    }
}

class GroupCFish extends Fish {
    public GroupCFish(String name, int population, int foodConsumption, int maxAge) {
        super(name, population, foodConsumption, maxAge);
    }

    @Override
    public int feed(int foodAvailable) {
        int totalFoodNeeded = population * foodConsumption;
        if (foodAvailable >= totalFoodNeeded) {
            return totalFoodNeeded;
        } else if (foodAvailable < 200) {
            population /= 5;
            return 0;
        } else {
            population = foodAvailable / foodConsumption;
            return foodAvailable;
        }
    }


    @Override
    public int reproduce() {
        int eggsProduced = population*3;
        population += eggsProduced;
        return eggsProduced;
    }
}


class Ecosystem {
    private List<Fish> fishes = new ArrayList<>();
    private int plants;
    private int totalEggs;
    private Random random = new Random();

    public Ecosystem(int plants, int groupA, int groupB, int groupC, int foodConsumptionA, int foodConsumptionB, int totalEggs, int maxAge, int maxAgeB, int foodConsumptionC) {
        this.plants = plants;
        fishes.add(new GroupAFish("Omnivorse (A)", groupA, foodConsumptionA,  maxAge));
        fishes.add(new GroupBFish("Predators (B)", groupB, foodConsumptionB, maxAgeB));
        fishes.add(new GroupCFish("Grass-eaters (C)", groupC, foodConsumptionC, maxAge));
        this.totalEggs = totalEggs;
    }

    public void simulate(int generations) {
        for (int i = 0; i < generations; i++) {
            System.out.println("=== Поколение " + (i + 1) + " ===");
            int consumedEggs = 0;

            for (Fish fish : fishes) {
                if (fish instanceof GroupAFish) {
                    consumedEggs += fish.feed(totalEggs);
                } else if (fish instanceof GroupBFish) {
                    List<Fish> preyFish = new ArrayList<>();
                    for (Fish prey : fishes) {
                        if (!(prey instanceof GroupBFish)) {
                            preyFish.add(prey);
                        }
                    }
                    ((GroupBFish) fish).feed(preyFish);
                } else if (fish instanceof GroupCFish) {
                    if (totalEggs < plants) {
                        plants -= fish.feed(plants);
                    } else {
                        totalEggs -= fish.feed(totalEggs);
                    }
                }
            }

            for (Fish fish : fishes) {
                totalEggs += fish.reproduce();
                fish.age();
            }

            totalEggs += random.nextInt(50);
            totalEggs = Math.max(totalEggs - consumedEggs, 0);
            totalEggs = Math.min(totalEggs, 1000);
            plants += random.nextInt(250) - 25;
            plants = Math.max(plants, 5);

            System.out.println("Растения: " + plants + ", Икра: " + totalEggs);
            for (Fish fish : fishes) {
                System.out.println(fish.name + ": " + fish.population + " (Возраст поколения: " + fish.age + ")");
            }
        }
    }
}
