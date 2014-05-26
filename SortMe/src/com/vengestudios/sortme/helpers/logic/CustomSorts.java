package com.vengestudios.sortme.helpers.logic;

import java.util.Comparator;
import java.util.List;

public class CustomSorts {

	/**
	 * Does an insertion sort
	 * @param l A list of elements
	 * @Postconditions
	 * If the Comparable requests an ascending order
	 *     l.get(0) =< ... =< l.get(l.size()-1)
	 * else
	 *     l.get(0) >= ... >= l.get(l.size()-1)
	 */
    public static <T extends Comparable<T>> void insertionSort(List<T> l) {
        for (int sz=l.size(), i=1; i<sz; i++) {
            int j = i;
            while (j > 0) {
                T prev = l.get(j-1);
                T thisOne = l.get(j);
                if (prev.compareTo(thisOne) > 0) {
                    l.set(j-1, thisOne);
                    l.set(j, prev);
                } else {
                    break;
                }
                j--;
            }
        }
    }

	/**
	 * Does an insertion sort
	 * @param l A array of elements
	 * @Postconditions
	 * If the Comparable requests an ascending order
	 *     l[0] =< ... =< l[l.length-1]
	 * else
	 *     l[0] >= ... >= l[l.length-1]
	 */
    public static <T, S extends Comparator<T>> void insertionSort(T [] l, S comparator) {
        for (int sz=l.length, i=1; i<sz; i++) {
            int j = i;
            while (j > 0) {
                T prev = l[j-1];
                T thisOne = l[j];
                if (comparator.compare(prev, thisOne) > 0) {
                    l[j-1]=thisOne;
                    l[j]=prev;
                } else {
                    break;
                }
                j--;
            }
        }
    }

	/**
	 * Does an insertion sort
	 * @param l A list of elements
	 * @param comparator A Comparator to define how the elements should be compared
	 * @Postconditions
	 * If the Comparable requests an ascending order
	 *     l.get(0) =< ... =< l.get(l.size()-1)
	 * else
	 *     l.get(0) >= ... >= l.get(l.size()-1)
	 */
    public static <T, S extends Comparator<T>> void insertionSort(List<T> l, S comparator) {
        for (int sz=l.size(), i=1; i<sz; i++) {
            int j = i;
            while (j > 0) {
                T prev = l.get(j-1);
                T thisOne = l.get(j);
                if (comparator.compare(prev, thisOne) > 0) {
                    l.set(j-1, thisOne);
                    l.set(j, prev);
                } else {
                    break;
                }
                j--;
            }
        }
    }

	/**
	 * Does an insertion sort for ascending order on an int array
	 * @param l An int array
	 * @Postconditions
	 * l[0] =< ... =< l[l.length-1]
     */
    public static void insertionSortAsec(int [] l) {
        for (int sz=l.length, i=1; i<sz; i++) {
            int j = i;
            while (j > 0) {
                int prev = l[j-1];
                int thisOne = l[j];
                if (prev - thisOne > 0) {
                    l[j-1]=thisOne;
                    l[j]=prev;
                } else {
                    break;
                }
                j--;
            }
        }
    }
}
