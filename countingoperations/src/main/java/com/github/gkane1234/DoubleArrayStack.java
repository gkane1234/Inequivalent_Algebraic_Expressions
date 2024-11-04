package com.github.gkane1234;

public class DoubleArrayStack {
    /*
    A stack implementation for generic doubles, that has a max size.
    */
    private double[] stack;
    private int topPointer; 
    private int maxSize;

    public DoubleArrayStack(int maxSize) {
        /*
        Initializes the stack with a given max size.
        */
        this.maxSize = maxSize;
        this.stack = new double[maxSize];
        this.topPointer = -1;  // Stack is initially empty
    }
    public DoubleArrayStack() {
        this(10);
    }

    public void push(double value) {
        /*
        Pushes a value onto the stack.
        */
        if (topPointer < maxSize - 1) {  // Check for overflow
            stack[++topPointer] = value;
        } else {
            throw new StackOverflowError("Stack is full");
        }
    }

    public double pop() {
        /*
        Pops a value off the stack.
        */
        if (topPointer >= 0) { 
            return stack[topPointer--];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    public double peek() {
        /*
        Peeks at the top value of the stack.
        */
        if (topPointer >= 0) {
            return stack[topPointer];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    public int size() {
        /*
        Returns the size of the stack.
        */
        return topPointer+1;
    }


    public boolean isEmpty() {
        /*
        Checks if the stack is empty.
        */
        return topPointer == -1;
    }

    public boolean isFull() {
        /*
        Checks if the stack is full.
        */
        return topPointer == maxSize - 1;
    }
}