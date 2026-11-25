
package strategy;

public class MemberRentalStrategy extends RentalStrategy {
    @Override
    public double calculate(int days, double pricePerDay) {
        // Member aktif mendapat diskon 15%
        double subtotal = days * pricePerDay;
        return subtotal * 0.85;
    }
}