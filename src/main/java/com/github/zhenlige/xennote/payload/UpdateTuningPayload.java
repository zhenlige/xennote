package com.github.zhenlige.xennote.payload;

import com.github.zhenlige.xennote.Tuning;
import com.github.zhenlige.xennote.Xennote;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record UpdateTuningPayload(String tuningId, Optional<Tuning> tuning) implements CustomPayload {

	public static final Identifier PACKET_ID = Identifier.of(Xennote.MOD_ID, "update_tuning");
	public static final Id<UpdateTuningPayload> ID = new Id<>(PACKET_ID);

	public static final PacketCodec<PacketByteBuf, UpdateTuningPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.STRING, UpdateTuningPayload::tuningId,
		Tuning.OPTIONAL_PACKET_CODEC, UpdateTuningPayload::tuning,
		UpdateTuningPayload::new);

	@Override
	public Id<? extends CustomPayload> getId(){
		return ID;
	}
}