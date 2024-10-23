package counting_operations.Java_Version;
import java.util.function.BiFunction;

public class Operation {
    public static Operation[] OPERATIONS = new Operation[] {
        new Operation((a, b) -> a + b, true,'+'),
        new Operation((a, b) -> a - b, false,'-'),
        new Operation((a, b) -> a * b, true,'*'),
        new Operation((a, b) -> (b != 0) ? a / b : null, false,'/')};

    public static void changeOperations(Operation[] newOperations) {
        OPERATIONS=newOperations;
    }

    private BiFunction<Double, Double, Double> operationFunction;
    private boolean commutative;
    private char name;

    public Operation(BiFunction<Double, Double, Double> operationFunction, boolean commutative,char name) {
        this.name=name;
        this.commutative = commutative;
        this.operationFunction = operationFunction;
    }

    public double apply(double a, double b) {
        return operationFunction.apply(a, b);
    }
    public Boolean isCommutative(){
        return this.commutative;
    }
    public char getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return String.valueOf(this.name);
    }
}