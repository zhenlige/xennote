package com.github.zhenlige.xennote;

import net.minecraft.nbt.NbtCompound;
import org.apache.commons.lang3.math.Fraction;

import java.util.Map;

public class EqualTuning extends Tuning {

	/**
	 * The inverse of the step size. The temperment is <code>k</code> ed <code>Math.E</code>.
	 */
	public double k;

	public EqualTuning(double k) {
		this.k = k;
	}
	
	public static EqualTuning ofOctave(double edo) {
		return new EqualTuning(edo / Math.log(2.0));
	}
	
	public static EqualTuning of(double ed, double period) {
		return new EqualTuning(ed / Math.log(period));
	}

	@Override
	public double logTune(Fraction x) {
		Map<Integer, Integer> fact = XennoteMath.fact(x);
		long n = 0;
        for (Map.Entry<Integer, Integer> i : fact.entrySet()) {
            n += Math.round(Math.log(i.getKey()) * k) * i.getValue();
        }
		return n / k * stretch;
	}

	public static EqualTuning fromNbt(NbtCompound nbt) {
		EqualTuning tuning;
		if (nbt.contains("ede"))
			tuning = new EqualTuning(nbt.getDouble("ede"));
		else tuning = ofOctave(12);
		if (nbt.contains("stretch"))
			tuning = (EqualTuning) tuning.restretch(nbt.getDouble("stretch"));
		return tuning;
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = super.toNbt();
		nbt.putString("type", EQUAL_TYPE);
		nbt.putDouble("ede", k);
		return nbt;
	}
}