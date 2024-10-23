package counting_operations.Java_Version;

public class ArrayStack<T> {
    private T[] stack;
    private int topPointer; 
    private int maxSize;

    @SuppressWarnings("unchecked")
    public ArrayStack(int maxSize) {
        this.maxSize = maxSize;
        this.stack = (T[]) new Object[maxSize];  // Generic array creation
        this.topPointer = -1;  // Stack is initially empty
    }

    // Default constructor that initializes to a stack of size 10
    public ArrayStack() {
        this(10);
    }

    public void push(T value) {
        if (topPointer < maxSize - 1) {  // Check for overflow
            stack[++topPointer] = value;
        } else {
            throw new StackOverflowError("Stack is full");
        }
    }

    public T pop() {
        if (topPointer >= 0) {  // Check for underflow
            return stack[topPointer--];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    public T peek() {
        if (topPointer >= 0) {
            return stack[topPointer];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    public int size() {
        return topPointer + 1;
    }

    public boolean isEmpty() {
        return topPointer == -1;
    }

    public boolean isFull() {
        return topPointer == maxSize - 1;
    }
}

