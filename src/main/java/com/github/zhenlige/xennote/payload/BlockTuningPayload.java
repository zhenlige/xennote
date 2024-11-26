package com.github.zhenlige.xennote.payload;

import com.github.zhenlige.xennote.TuningRef;
import com.github.zhenlige.xennote.Xennote;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;

/**
 * Always make sure p/q is the simplest form.
 */

public record BlockTuningPayload(GlobalPos pos, int p, int q, TuningRef tuningRef) implements CustomPayload {
	public static final Identifier PACKET_ID = Identifier.of(Xennote.MOD_ID, "config");
	public static final Id<BlockTuningPayload> ID = new Id<>(PACKET_ID);
	public static final PacketCodec<PacketByteBuf, BlockTuningPayload> CODEC = PacketCodec.tuple(
		GlobalPos.PACKET_CODEC, BlockTuningPayload::pos,
		PacketCodecs.INTEGER, BlockTuningPayload::p,
		PacketCodecs.INTEGER, BlockTuningPayload::q,
		TuningRef.PACKET_CODEC, BlockTuningPayload::tuningRef,
		BlockTuningPayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}