package edu.gsu.dmlab.trash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by thad on 10/24/15.
 */
public class Test {

    public static void main(String [] args){
        HashMap<Integer, ArrayList<Integer>> test = new HashMap<>();
        ArrayList<Integer> list1 = new  ArrayList <Integer>();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        ArrayList<Integer> list2 = new  ArrayList <Integer>();
        list2.add(4);
        list2.add(5);
        list2.add(6);
        test.put(1, list1);
        test.put(2, list2);
        list1.addAll(list2);
        test.put(2, list1);
        list1.add(7);
        test.get(2).add(8);
        for (ArrayList<Integer> list: test.values()){
            System.out.print(list);
        }
    }
}
