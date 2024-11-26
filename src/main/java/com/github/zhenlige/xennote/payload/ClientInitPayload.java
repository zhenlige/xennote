package com.github.zhenlige.xennote.payload;

import com.github.zhenlige.xennote.Tuning;
import com.github.zhenlige.xennote.Xennote;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Map;

public record ClientInitPayload(Map<String, Tuning> tuningMap) implements CustomPayload {
	public static final Identifier PACKET_ID = Identifier.of(Xennote.MOD_ID, "init");
	public static final Id<ClientInitPayload> ID = new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<PacketByteBuf, ClientInitPayload> CODEC = PacketCodec.tuple(
		Tuning.MAP_PACKET_CODEC, ClientInitPayload::tuningMap,
		ClientInitPayload::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
