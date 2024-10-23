package counting_operations.Java_Version;

public class DoubleArrayStack {
    private double[] stack;
    private int topPointer; 
    private int maxSize;

    public DoubleArrayStack(int maxSize) {
        this.maxSize = maxSize;
        this.stack = new double[maxSize];
        this.topPointer = -1;  // Stack is initially empty
    }
    //default to 10
    public DoubleArrayStack() {
        this(10);
    }

    public void push(double value) {
        if (topPointer < maxSize - 1) {  // Check for overflow
            stack[++topPointer] = value;
        } else {
            throw new StackOverflowError("Stack is full");
        }
    }

    public double pop() {
        if (topPointer >= 0) {  // Check for underflow
            return stack[topPointer--];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    public double peek() {
        if (topPointer >= 0) {
            return stack[topPointer];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    public int size() {
        return topPointer+1;
    }


    public boolean isEmpty() {
        return topPointer == -1;
    }

    public boolean isFull() {
        return topPointer == maxSize - 1;
    }
}