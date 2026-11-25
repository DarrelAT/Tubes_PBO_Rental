package strategy;

public class RentalCalculator {
    private RentalStrategy strategy;

    public RentalCalculator(RentalStrategy strategy){
        this.strategy = strategy;
    }

    public void setStrategy(RentalStrategy strategy){
        this.strategy = strategy;
    }

    public double executeCalculation(int days, double basePrice){
        return strategy.calculate(days, basePrice);
    }
}
