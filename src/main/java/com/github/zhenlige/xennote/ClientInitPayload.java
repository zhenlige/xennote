package com.github.zhenlige.xennote;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientInitPayload(NbtCompound tuningList) implements CustomPayload {
	public static final Identifier PACKET_ID = Identifier.of(Xennote.MOD_ID, "init");
	public static final Id<ClientInitPayload> ID = new CustomPayload.Id<>(PACKET_ID);
	public static final PacketCodec<PacketByteBuf, ClientInitPayload> CODEC = PacketCodec.tuple(
		PacketCodecs.NBT_COMPOUND, ClientInitPayload::tuningList,
		ClientInitPayload::new
	);
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
