package com.github.zhenlige.xennote;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.lang3.math.Fraction;
import org.apache.commons.numbers.primes.Primes;

import java.util.HashMap;
import java.util.Map;

public class PrimeMapTuning extends Tuning {
	@Override
	public TuningType getType() {
		return TuningType.PRIME_MAP;
	}
	public Map<Integer, Double> map = new HashMap<>();

	public PrimeMapTuning() {}

	public PrimeMapTuning(Map<Integer, Double> map) {
		this.map.putAll(map);
	}

	public PrimeMapTuning(double... map) {
		int p = 2;
		for (double v : map) {
			this.map.put(p, v);
			p = Primes.nextPrime(p + 1);
		}
	}

	protected PrimeMapTuning(NbtCompound nbt) {
		super(nbt);
		NbtList mapNbt = nbt.getList("primeMap", NbtElement.COMPOUND_TYPE);
		for (NbtElement i : mapNbt) {
			if (i instanceof NbtCompound singleMap) {
				int prime = singleMap.getInt("prime");
				if (Primes.isPrime(prime))
					map.put(prime, singleMap.getDouble("mapTo"));
			}
		}
	}

	@Override
	public NbtCompound toNbt() {
		NbtCompound nbt = super.toNbt();
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

	protected PrimeMapTuning(ByteBuf buf) {
		super(buf);
		int prime;
		double mapTo;
		while ((prime = PacketCodecs.INTEGER.decode(buf)) != 0) {
			mapTo = PacketCodecs.DOUBLE.decode(buf);
			map.put(prime, mapTo);
		}
	}

	@Override
	protected void encode(ByteBuf buf) {
		super.encode(buf);
		for (Map.Entry<Integer, Double> i : map.entrySet()) {
			PacketCodecs.INTEGER.encode(buf, i.getKey());
			PacketCodecs.DOUBLE.encode(buf, i.getValue());
		}
		PacketCodecs.INTEGER.encode(buf, 0);
	}

	public PrimeMapTuning clone() {
		PrimeMapTuning tuning = (PrimeMapTuning) super.clone();
		tuning.map.putAll(map);
		return tuning;
	}

	@Override
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

	public PrimeMapTuning restretch(double k) {
		PrimeMapTuning tuning = (PrimeMapTuning) super.restretch(k);
		for (Map.Entry<Integer, Double> i : tuning.map.entrySet()) {
			i.setValue(i.getValue() * k);
		}
		return tuning;
	}
}
