package com.github.zhenlige.xennote;

import org.apache.commons.lang3.math.Fraction;
import org.apache.commons.numbers.primes.Primes;

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

	public static HashMap<Integer, Integer> fact(int x) {
		if (x == 0) return null;
		if (x == 1) return new HashMap<>();
		HashMap<Integer, Integer> ans = new HashMap<>();
		if (x == Integer.MIN_VALUE) {
			ans.put(2, Integer.SIZE - 1);
			return ans;
		}
		x = Math.abs(x);
		var list = Primes.primeFactors(x);
		for (int i : list) {
			if (ans.containsKey(i)) ans.put(i, ans.get(i) + 1);
			else ans.put(i, 1);
		}
		return ans;
	}

	public static HashMap<Integer, Integer> fact(int x, Set<Integer> primes) {
		if (x == 0) return null;
		HashMap<Integer, Integer> ans = new HashMap<>();
		if (x == Integer.MIN_VALUE) {
			if (primes.contains(2))
				ans.put(2, Integer.SIZE - 1);
			else
				ans.put(x, 1);
			return ans;
		}
		x = Math.abs(x);
		for (int p : primes) {
			int n = 0;
			while (x % p == 0) {
				++n;
				x /= p;
			}
			if (n != 0) ans.put(p, n);
			if (x == 1) return ans;
		}
		ans.put(x, 1);
		return ans;
	}

	public static HashMap<Integer, Integer> fact(Fraction x) {
		HashMap<Integer, Integer> ans = fact(qOf(x));
		if (ans != null) {
			Iterator<Map.Entry<Integer, Integer>> ite = ans.entrySet().iterator();
			Map.Entry<Integer, Integer> entry;
			while (ite.hasNext()) {
				entry = ite.next();
				entry.setValue(-entry.getValue());
			}
			ans.putAll(fact(pOf(x)));
		}
		return ans;
	}

	public static HashMap<Integer, Integer> fact(Fraction x, Set<Integer> primes) {
		HashMap<Integer, Integer> ans = fact(qOf(x), primes);
		if (ans != null) {
			Iterator<Map.Entry<Integer, Integer> > ite = ans.entrySet().iterator();
			Map.Entry<Integer, Integer> entry;
			while (ite.hasNext()) {
				entry = ite.next();
				entry.setValue(-entry.getValue());
			}
			ans.putAll(fact(pOf(x), primes));
		}
		return ans;
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