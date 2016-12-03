//package com.bridgelabz.camscanner;

/**
 * Created by Nadimuddin on 7/11/16.
 */

public class StringAlpha
{
    public static void main(String[] args)
    {
        char ch[] = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
        String str = "this is my string";
        int count=0;
        int i, j;

        for (i=0; i<ch.length; i++)
        {
            for (j=0; j<str.length(); j++)
            {
                if(str.charAt(j) == ch[i])
                {
                    count++;
                    break;
                }
            }
        }

        if(count == ch.length)
            System.out.println("True:: All alphabets are present");
        else
            System.out.println("False:: some alphabets are not present");
    }
}
