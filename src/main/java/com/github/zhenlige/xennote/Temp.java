package com.github.zhenlige.xennote;

public class Temp {
	public float tune(Rational x) {
		return (float) x.p / (float) x.q;
	}
	
	public double logTune(Rational x) {
		return Math.log((double) x.p / (double) x.q);
	}
	
	public static final Temp JI = new Temp();
}