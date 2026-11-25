package strategy;

//Tarif Standar
public class StandardRentalStrategy extends RentalStrategy {
    @Override 
    public double calculate(int days, double basePrice){
        return days * basePrice;
    }
}
