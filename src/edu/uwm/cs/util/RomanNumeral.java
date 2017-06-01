package edu.uwm.cs.util;

import java.util.NavigableMap;
import java.util.TreeMap;

// http://stackoverflow.com/questions/12967896/converting-integers-to-roman-numerals-java
// http://stackoverflow.com/users/1420681/bhlangonijr
public class RomanNumeral {

    private final static NavigableMap<Integer, String> map = new TreeMap<Integer, String>();

    static {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }

    /**
     * Convert a positive integer to a Roman numeral.
     * Numbers greater than 3999 just use longer and longer strings of M's.
     * @param number, must be positive
     * @return Roman numeral for this number
     */
    public final static String toString(int number) {
    	if (number <= 0) throw new IllegalArgumentException("Romans didn't handle 0 or negative numbers");
        int l =  map.floorKey(number);
        if ( number == l ) {
            return map.get(number);
        }
        return map.get(l) + toString(number-l);
    }

}