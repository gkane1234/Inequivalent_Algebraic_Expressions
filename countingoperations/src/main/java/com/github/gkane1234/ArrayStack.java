package com.github.gkane1234;
/** 
    A static size stack implementation using an array.
*/
public class ArrayStack<T> {

    private T[] stack;
    private int topPointer; 
    private int maxSize;

    /**
        Constructor for the ArrayStack class.
        @param maxSize: an <code>int</code> representing the maximum size of the stack.
    */
    @SuppressWarnings("unchecked")
    public ArrayStack(int maxSize) {
        this.maxSize = maxSize;
        this.stack = (T[]) new Object[maxSize];  // Generic array creation
        this.topPointer = -1;  // Stack is initially empty
    }

    /**
        Default constructor for the ArrayStack class.
        Initializes to a stack of size 10.
    */
    public ArrayStack() {
        this(10);
    }

    /**
        Pushes a value onto the stack.
        @param value: the value to push onto the stack.
    */
    public void push(T value) {
        if (topPointer < maxSize - 1) {  // Check for overflow
            stack[++topPointer] = value;
        } else {
            throw new StackOverflowError("Stack is full");
        }
    }

    /**
        Pops a value off the stack.
        @return a <code>T</code> representing the value popped off the stack.
    */
    public T pop() {
        if (topPointer >= 0) {  // Check for underflow
            return stack[topPointer--];
        } else {
            throw new IllegalStateException("Stack is empty");
        }
    }

    /**
        Peeks at the top value of the stack.
        @return a <code>T</code> representing the value at the top of the stack.
    */
    public T peek() {
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
        return topPointer + 1;
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

