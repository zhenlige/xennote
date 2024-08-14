package com.github.zhenlige.xennote;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XennoteMath {
	public static final int[] PRIMES = new int[] {
			 2, 3, 5, 7,11,13,17,19,23,29,
			31,37,41,43,47,53,59,61,67,71,
			73,79,83,89,97
	};
	public static final int MIN_PRIME_OUT = 101;
	
	public static int gcd(int a, int b) {
		if (b == 0) return Math.abs(a);
		return gcd(b, a % b);
	}

	public static HashMap<Integer, Integer> fact(int x) {
		if (x == 0) return null;
		HashMap<Integer, Integer> res = new HashMap<Integer, Integer>();
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
	
	public static HashMap<Integer, Integer> fact(Rational x) {
		x = x.simplify();
		HashMap<Integer, Integer> res = fact(x.q);
		Iterator<Map.Entry<Integer, Integer> > it = res.entrySet().iterator();
        Map.Entry<Integer, Integer> entry;
        while (it.hasNext()) {
            entry = it.next();
            entry.setValue(-entry.getValue());
        }
		res.putAll(fact(x.p));
		return res;
	}
	
}