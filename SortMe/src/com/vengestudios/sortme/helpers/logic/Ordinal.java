package com.vengestudios.sortme.helpers.logic;

/**
 * A helper class to format ranking positions
 * using the English Main Ordinal Series (1st, 2nd, 3rd, 4th, ...)
 */
public class Ordinal {

	public String number; // A String that represents the position
	public String suffix; // The suffix of the position

	/**
	 * Constructor
	 *
	 * Creates an instance of Ordinal which gives a number and suffix
	 * according to the English Main Ordinal Series (1st, 2nd, 3rd, 4th, ...)
	 *
	 * @param i The position or ranking
	 * @param zeroIndexed Whether the ranking starts from 0.
	 * if true, (0 -> 1st, 1 -> 2nd, ...),
	 * else (1 -> 1st, 2 -> 2nd, ...)
	 */
	public Ordinal(int i, boolean zeroIndexed) {
		if (zeroIndexed) i++;
		number = ""+i;
		int r = i % 10;
		if      (r==1) suffix = "st";
		else if (r==2) suffix = "nd";
		else if (r==3) suffix = "rd";
		else           suffix = "th";
	}

}
