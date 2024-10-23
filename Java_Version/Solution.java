package counting_operations.Java_Version;
public class Solution{
    private static final int DECIMAL_PLACES = 3;
    private double[] subValues;
    private Expression expression;
    
    public Solution(Expression expression, double[] subValues) {
        this.expression=expression;
        this.subValues=subValues;
    }

    public String display() {
        ArrayStack<String> stack = new ArrayStack<>(expression.order.length);
        byte values_pointer =0;
        byte operations_pointer = 0;
        for (boolean isNumber : this.expression.order) {
            if (isNumber) {
                double next = this.subValues[this.expression.values[values_pointer++]];
                if (next == Math.round(next)) {
                    stack.push(String.valueOf(Math.round(next)));
                } else {
                    stack.push(String.format("%." + DECIMAL_PLACES + "f", next));;
                }
            } else {
                byte opCode = expression.operations[operations_pointer++];
                String b = stack.pop();
                String a = stack.pop();
                String combinedExpression = "("+a+String.valueOf(Operation.OPERATIONS[opCode])+b+")";
                stack.push(combinedExpression);
            }   
        }
        return stack.peek();
    }

    @Override
    public String toString() {
        return this.display();
    }

}
