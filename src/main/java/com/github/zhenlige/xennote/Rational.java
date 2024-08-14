package com.github.zhenlige.xennote;

public class Rational {
	public int p, q;

	public Rational(int p, int q) {
		this.p = p;
		this.q = q;
	}
	
	public Rational(int p) {
		this.p = p;
		this.q = 1;
	}
	
	public Rational(Rational b) {
		this.p = b.p;
		this.q = b.q;
	}
	
	public Rational clone() {
		return new Rational(this);
	}

	public SimplestRational simplify() {
		return new SimplestRational(this);
	}
	
	public boolean equals(Rational b) {
		if (q == 0) return b.q == 0 && ((p == 0 && b.p == 0) || (p != 0 && b.p != 0));
		return p * b.q == q * b.p;
	}
}