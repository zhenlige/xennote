package com.github.zhenlige.xennote;

import org.apache.commons.lang3.math.Fraction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class XennoteMath {
	// short form
	public static int pOf(Fraction x) {
		return x.getNumerator();
	}

	public static int qOf(Fraction x) {
		return x.getDenominator();
	}

	/**
	 * The closest <code>double</code> value to phi.
	 */
	public static final double PHI = 1.618033988749895;

	public static final double IHP = 0.6180339887498949;

	public static final int[] PRIMES = new int[] {
		 2, 3, 5, 7,11,13,17,19,23,29,
		31,37,41,43,47,53,59,61,67,71,
		73,79,83,89,97
	};

	public static final int MIN_PRIME_OUT = 101;

	public static HashMap<Integer, Integer> fact(int x) {
		if (x == 0) return null;
		HashMap<Integer, Integer> res = new HashMap<>();
		if (x == Integer.MIN_VALUE) {
			res.put(2, Integer.SIZE - 1);
			return res;
		}
		x = Math.abs(x);
		for (int p : PRIMES) {
			int n = 0;
			while (x % p == 0) {
				++n;
				x /= p;
			}
			if (n != 0) res.put(p, n);
			if (x == 1) return res;
		}
		if (x < MIN_PRIME_OUT * MIN_PRIME_OUT) {
			res.put(x, 1);
			return res;
		}
		for (int p = MIN_PRIME_OUT; x > 1; ++p) {
			int n = 0;
			while (x % p == 0) {
				++n;
				x /= p;
			}
			if (n != 0) res.put(p, n);
		}
		return res;
	}

	public static HashMap<Integer, Integer> fact(int x, Set<Integer> primes) {
		if (x == 0) return null;
		HashMap<Integer, Integer> res = new HashMap<>();
		if (x == Integer.MIN_VALUE) {
			if (primes.contains(2))
				res.put(2, Integer.SIZE - 1);
			else
				res.put(x, 1);
			return res;
		}
		x = Math.abs(x);
		for (int p : primes) {
			int n = 0;
			while (x % p == 0) {
				++n;
				x /= p;
			}
			if (n != 0) res.put(p, n);
			if (x == 1) return res;
		}
		res.put(x, 1);
		return res;
	}

	public static HashMap<Integer, Integer> fact(Fraction x) {
		HashMap<Integer, Integer> res = fact(qOf(x));
		if (res != null) {
			Iterator<Map.Entry<Integer, Integer>> it = res.entrySet().iterator();
			Map.Entry<Integer, Integer> entry;
			while (it.hasNext()) {
				entry = it.next();
				entry.setValue(-entry.getValue());
			}
			res.putAll(fact(pOf(x)));
		}
		return res;
	}

	public static HashMap<Integer, Integer> fact(Fraction x, Set<Integer> primes) {
		HashMap<Integer, Integer> res = fact(qOf(x), primes);
		if (res != null) {
			Iterator<Map.Entry<Integer, Integer> > it = res.entrySet().iterator();
			Map.Entry<Integer, Integer> entry;
			while (it.hasNext()) {
				entry = it.next();
				entry.setValue(-entry.getValue());
			}
			res.putAll(fact(pOf(x), primes));
		}
		return res;
	}

	/**
	 * Allow input in fraction form.
	 * @param str represented in standard or fraction form.
	 */
	public static double parseDouble(String str) {
		int p = str.lastIndexOf('/');
		if (p == -1) return Double.parseDouble(str);
		else return Double.parseDouble(str.substring(0, p)) / Double.parseDouble(str.substring(p+1));
	}
}