package com.github.gkane1234;

public class DoubleArrayStack {
    /*
    A stack implementation for generic doubles, that has a max size.
    */
    private double[] stack;
    private int topPointer; 
    private int maxSize;
    /**
        Creates a new <code>DoubleArrayStack</code> with a given max size.
        @param maxSize: an <code>int</code> representing the max size of the stack.
    */
    public DoubleArrayStack(int maxSize) {
        
        this.maxSize = maxSize;
        this.stack = new double[maxSize];
        this.topPointer = -1;  // Stack is initially empty
    }
    public DoubleArrayStack() {
        this(10);
    }

    /**
        Pushes a value onto the stack.
        @param value: a <code>double</code> representing the value to push onto the stack.
    */
    public void push(double value) {

        if (topPointer < maxSize - 1) {  // Check for overflow
            stack[++topPointer] = value;
        } else {
            throw new StackOverflowError("Stack is full");
        }
    }

    /**
        Pops a value off the stack.
        @return a <code>double</code> representing the value popped off the stack.
    */
    public double pop() {
        if (topPointer >= 0) { 
            return stack[topPointer--];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    /**
        Peeks at the top value of the stack.
        @return a <code>double</code> representing the value at the top of the stack.
    */
    public double peek() {
        if (topPointer >= 0) {
            return stack[topPointer];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    /**
        Returns the size of the stack.
        @return an <code>int</code> representing the size of the stack.
    */
    public int size() {
        return topPointer+1;
    }

    /**
        Checks if the stack is empty.
        @return a <code>boolean</code> representing whether the stack is empty.
    */
    public boolean isEmpty() {
        return topPointer == -1;
    }

    /**
        Checks if the stack is full.
        @return a <code>boolean</code> representing whether the stack is full.
    */
    public boolean isFull() {
        return topPointer == maxSize - 1;
    }
}