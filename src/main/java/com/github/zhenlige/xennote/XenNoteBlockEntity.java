package com.github.zhenlige.xennote;

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

	public int p = 1, q = 1; // The frequency is multiplied by p/q
	public double edo = 0; // 0 for infinity
	
	public XenNoteBlockEntity(BlockPos pos, BlockState state) {
		super(XennoteMain.NOTE_BLOCK_ENTITY, pos, state);
	}

	@Override
	public void writeNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        nbt.putInt("p", p);
        nbt.putInt("q", q);
        nbt.putDouble("edo", edo);
        super.writeNbt(nbt, registryLookup);
    }
	@Override
	public void readNbt(NbtCompound nbt, WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        p = nbt.getInt("p");
        q = nbt.getInt("q");
        edo = nbt.getDouble("edo");
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
	
	public Temp getTemp() {
		return edo==0 ? Temp.JI : EqualTemp.ofOctave(edo);
	}
	
	public float getPitch() {
		return this.getTemp().tune(new Rational(p, q));
	}
	
	public double getLogPitch() {
		return this.getTemp().logTune(new Rational(p, q));
	}
}