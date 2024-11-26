package com.github.zhenlige.xennote;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.lang3.math.Fraction;

import java.util.Map;

public class EqualTuning extends Tuning {
	@Override
	public TuningType getType() {
		return TuningType.EQUAL;
	}

	/** The inverse of the step size. The temperment is <code>ede</code> ed <code>Math.E</code>. */
	public double ede;

	public EqualTuning(double ede) {
		this.ede = ede;
	}

	public static EqualTuning of(double ed, double period) {
		return new EqualTuning(ed / Math.log(period));
	}

	protected EqualTuning(NbtCompound nbt) {
		super(nbt);
		if (nbt.contains("ede"))
			ede = nbt.getDouble("ede");
		else ede = 12 / Math.log(2.);
	}

	public NbtCompound toNbt() {
		NbtCompound nbt = super.toNbt();
		nbt.putDouble("ede", ede);
		return nbt;
	}

	protected EqualTuning(ByteBuf buf) {
		super(buf);
		ede = PacketCodecs.DOUBLE.decode(buf);
	}

	@Override
	protected void encode(ByteBuf buf) {
		super.encode(buf);
		PacketCodecs.DOUBLE.encode(buf, ede);
	}

	@Override
	public double logTune(Fraction x) {
		Map<Integer, Integer> fact = XennoteMath.fact(x);
		long n = 0;
        for (Map.Entry<Integer, Integer> i : fact.entrySet()) {
            n += Math.round(Math.log(i.getKey()) * ede) * i.getValue();
        }
		return n / ede * stretch;
	}
}