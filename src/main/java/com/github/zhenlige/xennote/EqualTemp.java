package com.github.zhenlige.xennote;

import java.util.Iterator;
import java.util.Map;

public class EqualTemp extends Temp {
	public double k;
	
	public EqualTemp(double k) {
		this.k = k;
	}
	
	public static EqualTemp ofOctave(double edo) {
		return new EqualTemp(edo / Math.log(2.0));
	}
	
	public static EqualTemp of(double ed, double period) {
		return new EqualTemp(ed / Math.log(period));
	}
	
	public float tune(Rational x) {
		Map<Integer, Integer> f = XennoteMath.fact(x);
		int n = 0;
		Iterator<Map.Entry<Integer, Integer> > it = f.entrySet().iterator();
        Map.Entry<Integer, Integer> entry;
        while (it.hasNext()) {
            entry = it.next();
            n += Math.round(Math.log(entry.getKey()) * k) * entry.getValue();
        }
		return (float) Math.exp(n / k);
	}
}