package com.github.zhenlige.xennote;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public interface TuningRef {
	enum TuningRefType {
		VAR, CONST
	}
	VarTuningRef JI = ofVar("ji");
	TuningRefType getType();
	Tuning getTuning();
	NbtElement toNbt();

	class ConstTuningRef implements TuningRef {
		public final Tuning tuning;

		public TuningRefType getType() {
			return TuningRefType.CONST;
		}

		ConstTuningRef(Tuning tuning) {
			this.tuning = tuning;
		}

		public Tuning getTuning() {
			return tuning;
		}

		public NbtCompound toNbt() {
			return tuning.toNbt();
		}
	}

	class VarTuningRef implements TuningRef {
		public final String id;

		public TuningRefType getType() {
			return TuningRefType.VAR;
		}

		VarTuningRef(String id) {
			this.id = id;
		}

		public Tuning getTuning() {
			Tuning tuning = WorldTunings.getCurrent().tunings.get(id);
			if (tuning == null) {
				Xennote.GLOBAL_LOGGER.error("Tuning not found, using JI instead");
				return Tuning.ji();
			} else return tuning;
		}

		public NbtString toNbt() {
			return NbtString.of(id);
		}
	}

	static ConstTuningRef ofConst(Tuning tuning) {
		return new ConstTuningRef(tuning);
	}

	static VarTuningRef ofVar(String id) {
		return new VarTuningRef(id);
	}

	static TuningRef fromNbt(NbtElement nbte) {
		return switch (nbte) {
			case NbtCompound nbt -> ofConst(Tuning.fromNbt(nbt));
			case NbtString nStr -> ofVar(nStr.asString());
			case NbtDouble nd -> ofConst(new EqualTuning(nd.doubleValue()));
			case null, default -> JI;
		};
	}

	PacketCodec<ByteBuf, TuningRef> PACKET_CODEC = new PacketCodec<>() {
		@Override
		public TuningRef decode(ByteBuf buf) {
			return switch (TuningRefType.values()[PacketCodecs.BYTE.decode(buf)]) {
				case VAR -> ofVar(PacketCodecs.STRING.decode(buf));
				case CONST -> ofConst(Tuning.PACKET_CODEC.decode(buf));
			};
		}

		@Override
		public void encode(ByteBuf buf, TuningRef value) {
			switch (value) {
				case VarTuningRef v -> {
					PacketCodecs.BYTE.encode(buf, (byte) TuningRefType.VAR.ordinal());
					PacketCodecs.STRING.encode(buf, v.id);
				}
				case ConstTuningRef c -> {
					PacketCodecs.BYTE.encode(buf, (byte) TuningRefType.CONST.ordinal());
					Tuning.PACKET_CODEC.encode(buf, c.tuning);
				}
				default -> {
					PacketCodecs.BYTE.encode(buf, (byte) -1);
					Xennote.GLOBAL_LOGGER.error("Unknown tuning reference type");
				}
			}
		}
	};
}