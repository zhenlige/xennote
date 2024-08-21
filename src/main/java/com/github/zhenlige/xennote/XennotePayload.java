package com.github.zhenlige.xennote;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.GlobalPos;

/**
 * Always make sure p/q is the simlest form.
 */

public record XennotePayload(GlobalPos pos, int p, int q,double edo) implements CustomPayload {
	public static final Identifier PACKET_ID = Identifier.of("xennote", "config");
	public static final Id<XennotePayload> ID = new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<PacketByteBuf, XennotePayload> CODEC = PacketCodec
			.tuple(GlobalPos.PACKET_CODEC, XennotePayload::pos,
					PacketCodecs.INTEGER, XennotePayload::p,
					PacketCodecs.INTEGER, XennotePayload::q,
					PacketCodecs.DOUBLE, XennotePayload::edo,
					XennotePayload::new);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}