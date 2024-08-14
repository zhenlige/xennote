package com.github.zhenlige.xennote;

public class SimplestRational extends Rational {

	public SimplestRational(Rational b) {
		super(b);
		preSimplify();
	}
	
	public SimplestRational(SimplestRational b) {
		super(b);
	}
	
	public SimplestRational(int p, int q) {
		super(p, q);
		preSimplify();
	}
	
	private void preSimplify() {
		if (p == 0 && q == 0) return;
		if (p != 0 && q == 0) {
			p = 1;
			return;
		}
		int g = XennoteMath.gcd(p, q);
		p /= g;
		q /= g;
		if (q < 0 && q != Integer.MIN_VALUE) {
			p = -p;
			q = -q;
		}
	}
	
	public SimplestRational simplify() {
		return this;
	}
}