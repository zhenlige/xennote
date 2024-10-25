package com.github.zhenlige.xennote;

import com.github.zhenlige.xennote.annotation.NeedWorldTunings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import org.apache.commons.lang3.math.Fraction;

import java.util.Objects;

/**
 * Specify a tuning.
 * In most calculations, interval sizes are stored as the natural logarithm of the frequency ratios.
 */
public class Tuning implements Cloneable {
	public static final String JI_TYPE = "ji";
	public static final String EQUAL_TYPE = "equal";
	public static final String PRIME_MAP_TYPE = "primeMap";
	public double stretch = 1.;

	public Tuning clone() {
		try {
			Tuning tuning = getClass().getConstructor().newInstance();
			tuning.stretch = stretch;
			return tuning;
		} catch (Exception e) {
			throw new RuntimeException("xennote.Tuning: The subclass " + getClass().getName() + " has a problem: " + e.getMessage());
		}
	}

	public float tune(Fraction x) {
		return (float) Math.exp(logTune(x));
	}

	public double logTune(Fraction x) {
		return Math.log(x.doubleValue()) * stretch;
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("type", JI_TYPE);
		if (stretch != 1.) nbt.putDouble("stretch", stretch);
		return nbt;
	}

	/**
	 * Caution: ALWAYS WRITE IT FROM THE BEGINNING IN SUBCLASSES! DO NOT USE {@code super.fromNbt}!
	 */
	public static Tuning fromNbt(NbtCompound nbt) {
		switch (nbt.getString("type")) {
			case JI_TYPE:
				if (nbt.contains("stretch"))
					return JI.restretch(nbt.getDouble("stretch"));
				else return JI;
			case EQUAL_TYPE:
				return EqualTuning.fromNbt(nbt);
			case PRIME_MAP_TYPE:
				return PrimeMapTuning.fromNbt(nbt);
			default:
				return JI;
		}
	}

	@NeedWorldTunings
	public static Tuning fromNbt(NbtElement nbte) {
		return switch (nbte) {
			case NbtCompound nbt -> fromNbt(nbt);
			case NbtString str -> Objects.requireNonNullElse(
				WorldTunings.getCurrent().tunings.get(str.asString()), JI);
			default -> JI;
		};
	}

	public Tuning restretch(double k) {
		Tuning tuning = clone();
		tuning.stretch *= k;
		return tuning;
	}

	/**
	 * Streching a tuning so that a specific interval is mapped to a specific size.
	 * Warning: streching a comma that is tempered out may cause unexpected behaviors.
	 *
	 * @param x The interval.
	 * @param newTune The logarithmic representation of the new size.
	 * @return The stretched tuning.
	 */
	public Tuning restretch(Fraction x, double newTune) {
		return restretch(newTune / tune(x));
	}

	/**
	 * Just intonation.
	 */
	public static final Tuning JI = new Tuning();
}