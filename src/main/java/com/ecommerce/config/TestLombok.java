package com.ecommerce.config;

import lombok.Data;

@Data
public class TestLombok
{
    private String name;

    public static void main(String[] args)
    {
        System.out.printf("Hello");
        Test test = new Test();
        test.setVar();
    }
}
class Test
{
    public void setVar(){
            TestLombok testLombok = new TestLombok();
            testLombok.setName(" In Class Test");
        System.out.println(testLombok.getName());
    }
}
