package com.github.zhenlige.xennote;

import com.github.zhenlige.xennote.annotation.NeedWorldTunings;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;

public class XenNoteBlockEntity extends BlockEntity {
	public static final NbtString JI = NbtString.of("ji");

	public int p = 1, q = 1; // The frequency is multiplied by p/q
	public double edo = 0; // 0 for infinity
	@NotNull
	public NbtElement tuningNbt = JI;
	
	public XenNoteBlockEntity(BlockPos pos, BlockState state) {
		super(Xennote.NOTE_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
		nbt.putInt("p", p);
		nbt.putInt("q", q);
		//nbt.putDouble("edo", edo);
		// update to post-v0.3.0 form
		//nbt.remove("edo");
		nbt.put("tuning", tuningNbt);
		super.writeNbt(nbt, registryLookup);
	}
	@Override
	public void readNbt(NbtCompound nbt, WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		p = nbt.getInt("p");
		q = nbt.getInt("q");
		//edo = nbt.getDouble("edo");
		if (nbt.contains("tuning")) {
			tuningNbt = nbt.get("tuning");
		} else if (nbt.contains("edo")) {
			// compatibility for v0.2.0 and earlier
			double edo = nbt.getDouble("edo");
			tuningNbt = edo == 0 ? JI : NbtDouble.of(edo / Math.log(2.));
		}
		else {
			tuningNbt = JI;
		}
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(WrapperLookup registryLookup) {
		return createNbt(registryLookup);
	}

	@NeedWorldTunings
	public Tuning getTuning() {
		if (tuningNbt instanceof NbtDouble k)
			return new EqualTuning(k.doubleValue());
		return Tuning.fromNbt(tuningNbt);
	}

	@NeedWorldTunings
	public float getPitch() {
		return this.getTuning().tune(Fraction.getFraction(p, q));
	}

	@NeedWorldTunings
	public double getLogPitch() {
		return this.getTuning().logTune(Fraction.getFraction(p, q));
	}
}