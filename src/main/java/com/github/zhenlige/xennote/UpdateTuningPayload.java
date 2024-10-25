package com.github.zhenlige.xennote;

import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record UpdateTuningPayload(String tuningId, NbtElement tuningNbt) implements CustomPayload {

	public static final Identifier PACKET_ID = Identifier.of(Xennote.MOD_ID, "update_tuning");
	public static final Id<UpdateTuningPayload> ID = new Id<>(PACKET_ID);

	public static final PacketCodec<PacketByteBuf, UpdateTuningPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.STRING, UpdateTuningPayload::tuningId,
		PacketCodecs.NBT_ELEMENT, UpdateTuningPayload::tuningNbt,
		UpdateTuningPayload::new);

	@Override
	public Id<? extends CustomPayload> getId(){
		return ID;
	}
}
