package com.github.zhenlige.xennote;

import com.github.zhenlige.xennote.annotation.NeedWorldTunings;
import com.google.common.collect.EnumHashBiMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.lang3.math.Fraction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Specify a tuning.
 * In most calculations, interval sizes are stored as the natural logarithm of the frequency ratios.
 */
public class Tuning implements Cloneable {
	/** The ordinal is not guaranteed to be stable. */
	public enum TuningType {
		JI, EQUAL, PRIME_MAP
	}
	public static final EnumHashBiMap<TuningType, String> STRING_MAP = EnumHashBiMap.create(
		Map.of(
			TuningType.JI, "ji",
			TuningType.EQUAL, "equal",
			TuningType.PRIME_MAP, "primeMap"
		)
	);
	public static final Map<TuningType, Function<NbtCompound, ? extends Tuning> > FROM_NBT_MAP = Map.of(
		TuningType.JI, Tuning::new,
		TuningType.EQUAL, EqualTuning::new,
		TuningType.PRIME_MAP, PrimeMapTuning::new
	);
	public static final Map<TuningType, Function<ByteBuf, ? extends Tuning> > DECODE_MAP = Map.of(
		TuningType.JI, Tuning::new,
		TuningType.EQUAL, EqualTuning::new,
		TuningType.PRIME_MAP, PrimeMapTuning::new
	);
	public TuningType getType() {
		return TuningType.JI;
	}

	public double stretch = 1.;

	public Tuning() {}

	protected Tuning(NbtCompound nbt) {
		if (nbt.contains("stretch"))
			stretch = nbt.getDouble("stretch");
	}

	public static Tuning fromNbt(NbtCompound nbt) {
		String type = nbt.getString("type");
		try {
			return FROM_NBT_MAP.get(STRING_MAP.inverse().get(type)).apply(nbt);
		} catch (NullPointerException e) {
			Xennote.GLOBAL_LOGGER.error("Unknown tuning type \"{}\", using JI instead", type);
			return ji();
		}
	}

	@NeedWorldTunings
	public static Tuning fromNbt(NbtElement nbte) {
		return TuningRef.fromNbt(nbte).getTuning();
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = new NbtCompound();
		nbt.putString("type", STRING_MAP.get(getType()));
		if (stretch != 1.) nbt.putDouble("stretch", stretch);
		return nbt;
	}

	protected Tuning(ByteBuf buf) {
		stretch = PacketCodecs.DOUBLE.decode(buf);
	}

	private static Tuning decode(ByteBuf buf) {
		byte i = PacketCodecs.BYTE.decode(buf);
		try {
			return DECODE_MAP.get(TuningType.values()[i]).apply(buf);
		} catch (ArrayIndexOutOfBoundsException e) {
			Xennote.GLOBAL_LOGGER.error("Unknown tuning type #{}, using JI instead", i);
			return ji();
		}
	}

	protected void encode(ByteBuf buf) {
		PacketCodecs.BYTE.encode(buf, (byte) getType().ordinal());
		PacketCodecs.DOUBLE.encode(buf, stretch);
	}

	public Tuning clone() {
		try {
			Tuning tuning = getClass().getConstructor().newInstance();
			tuning.stretch = stretch;
			return tuning;
		} catch (Exception e) {
			throw new RuntimeException("The subclass " + getClass().getName() + " is not clonable due to: " + e.getMessage());
		}
	}

	public float tune(Fraction x) {
		return (float) Math.exp(logTune(x));
	}

	public double logTune(Fraction x) {
		return Math.log(x.doubleValue()) * stretch;
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

	public Tuning setStretch(double stretch) {
		this.stretch = stretch;
		return this;
	}

	public static Tuning ji() {
		return WorldTunings.JI;
	}

	public static final PacketCodec<ByteBuf, Tuning> PACKET_CODEC = new PacketCodec<>() {
		@Override
		public Tuning decode(ByteBuf buf) {
			return Tuning.decode(buf);
		}

		@Override
		public void encode(ByteBuf buf, Tuning value) {
			value.encode(buf);
		}
	};
	public static final PacketCodec<ByteBuf, Optional<Tuning> > OPTIONAL_PACKET_CODEC = new PacketCodec<>() {
		@Override
		public Optional<Tuning> decode(ByteBuf buf) {
			return PacketCodecs.BOOL.decode(buf)
				? Optional.of(PACKET_CODEC.decode(buf))
				: Optional.empty();
		}

		@Override
		public void encode(ByteBuf buf, Optional<Tuning> value) {
			if (value.isEmpty()) {
				PacketCodecs.BOOL.encode(buf, false);
			} else {
				PacketCodecs.BOOL.encode(buf, true);
				PACKET_CODEC.encode(buf, value.get());
			}
		}
	};
	public static final PacketCodec<ByteBuf, Map<String, Tuning> > MAP_PACKET_CODEC = new PacketCodec<>() {
		@Override
		public Map<String, Tuning> decode(ByteBuf buf) {
			Map<String, Tuning> map = new HashMap<>();
			String str;
			Tuning tuning;
			while (!(str = PacketCodecs.STRING.decode(buf)).isEmpty()) {
				tuning = PACKET_CODEC.decode(buf);
				map.put(str, tuning);
			}
			return map;
		}

		@Override
		public void encode(ByteBuf buf, Map<String, Tuning> value) {
			for (Map.Entry<String, Tuning> i : value.entrySet()) {
				PacketCodecs.STRING.encode(buf, i.getKey());
				PACKET_CODEC.encode(buf, i.getValue());
			}
			PacketCodecs.STRING.encode(buf, "");
		}
	};
}