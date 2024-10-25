package com.github.zhenlige.xennote;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.apache.commons.lang3.math.Fraction;

import java.util.HashMap;
import java.util.Map;

public class PrimeMapTuning extends Tuning {
	public Map<Integer, Double> map = new HashMap<>();

	public PrimeMapTuning() {}

	public PrimeMapTuning(Map<Integer, Double> map) {
		this.map.putAll(map);
	}

	public PrimeMapTuning(double[] map) {
		for(int i = 0; i < Math.min(map.length, XennoteMath.PRIMES.length); ++i) {
			this.map.put(XennoteMath.PRIMES[i], map[i]);
		}
	}


	public <T> PrimeMapTuning(T map, double stretch) {
		this(map);
		this.stretch = stretch;
	}

	public <T> PrimeMapTuning(T map) {
		throw new RuntimeException("xennote.PrimeMapTuning: Invalid map representation");
		// for passing compiler
	}

	public PrimeMapTuning clone() {
		PrimeMapTuning tuning = (PrimeMapTuning) super.clone();
		tuning.map.putAll(map);
		return tuning;
	}
	public double logTune(Fraction x) {
		Map<Integer, Integer> fact = XennoteMath.fact(x, map.keySet());
		double res = 0;
		for (Map.Entry<Integer, Integer> i : fact.entrySet()) {
			if (map.containsKey(i.getKey()))
				res += map.get(i.getKey()) * i.getValue();
			else
				res += Math.log(i.getKey()) * i.getValue() * stretch;
		}
		return res;
	}

	public static PrimeMapTuning fromNbt(NbtCompound nbt) {
		PrimeMapTuning tuning = new PrimeMapTuning();
		NbtList maps = nbt.getList("primeMap", NbtElement.COMPOUND_TYPE);
		for (NbtElement i : maps) {
			if (i instanceof NbtCompound map) {
				tuning.map.put(map.getInt("prime"), map.getDouble("mapTo"));
			}
		}
		if (nbt.contains("stretch")) tuning.stretch = nbt.getDouble("stretch");
		return tuning;
	}

	@Override
	public NbtCompound toNbt() {
		NbtCompound nbt = super.toNbt();
		nbt.putString("type", PRIME_MAP_TYPE);
		NbtList mapNbt = new NbtList();
		for (Map.Entry<Integer, Double> i : map.entrySet()) {
			NbtCompound singleMap = new NbtCompound();
			singleMap.putInt("prime", i.getKey());
			singleMap.putDouble("mapTo", i.getValue());
			mapNbt.add(singleMap);
		}
		nbt.put("primeMap", mapNbt);
		return nbt;
	}

	public PrimeMapTuning restretch(double k) {
		PrimeMapTuning tuning = (PrimeMapTuning) super.restretch(k);
		for (Map.Entry<Integer, Double> i : tuning.map.entrySet()) {
			i.setValue(i.getValue() * k);
		}
		return tuning;
	}

	/**
	 * Quarter-comma meantone.
	 */
	public static final PrimeMapTuning QCOM_MEANTONE = new PrimeMapTuning
		(new double[]{Math.log(2), Math.log(5)/4 + Math.log(2), Math.log(5)});

	/**
	 * Quarter-comma meantone with septimal mappings.
	 */
	public static final PrimeMapTuning SEPT_QCOM_MEANTONE = new PrimeMapTuning
		(new double[]{Math.log(2), Math.log(5)/4 + Math.log(2), Math.log(5), Math.log(5)*2.5 - Math.log(8)});
}
