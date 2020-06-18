package demo;

import demo.Calculator;

public class MainClass {
    public static void main(String[] args) {
        Calculator c = new Calculator();
        int sum = c.add(2,3);
        System.out.println(sum);
    }
}

