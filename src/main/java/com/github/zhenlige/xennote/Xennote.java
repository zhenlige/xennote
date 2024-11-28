package com.github.zhenlige.xennote;

import com.github.zhenlige.xennote.payload.ClientInitPayload;
import com.github.zhenlige.xennote.payload.UpdateTuningPayload;
import com.github.zhenlige.xennote.payload.BlockTuningPayload;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;


public class Xennote implements ModInitializer {
	public static final String MOD_ID = "xennote";
	public static final Logger GLOBAL_LOGGER = LoggerFactory.getLogger(MOD_ID + " global");
	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final XenNoteBlock NOTE_BLOCK = new XenNoteBlock(Block.Settings.copy(Blocks.NOTE_BLOCK));
	public static final BlockItem NOTE_BLOCK_ITEM = new BlockItem(NOTE_BLOCK, new Item.Settings());
	public static final BlockEntityType<XenNoteBlockEntity> NOTE_BLOCK_ENTITY = Registry.register(
		Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "note_block_entity"),
		BlockEntityType.Builder.create(XenNoteBlockEntity::new, NOTE_BLOCK).build()
	);
	public static final DynamicCommandExceptionType INVALID_TUNING_ID = new DynamicCommandExceptionType(
		o -> Text.translatable("argument.tuning.invalid_id",  o)
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Initializing...");
		Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "note_block"), NOTE_BLOCK);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "note_block"), NOTE_BLOCK_ITEM);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content ->
			content.addAfter(Items.NOTE_BLOCK, NOTE_BLOCK_ITEM));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content ->
			content.addAfter(Items.NOTE_BLOCK, NOTE_BLOCK_ITEM));
		registerNetwork();
		registerCommand();
		LOGGER.info("Finished initializing");
	}

	private static void registerNetwork() {
		PayloadTypeRegistry.playS2C().register(BlockTuningPayload.ID, BlockTuningPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(BlockTuningPayload.ID, BlockTuningPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UpdateTuningPayload.ID, UpdateTuningPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ClientInitPayload.ID, ClientInitPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
			ServerPlayNetworking.send(
				handler.player,
				new ClientInitPayload(WorldTunings.getServerState(server).tunings)
			)
		);
		ServerPlayNetworking.registerGlobalReceiver(BlockTuningPayload.ID, (payload, context) -> {
			World world = context.server().getWorld(payload.pos().dimension());
			BlockPos pos = payload.pos().pos();
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof XenNoteBlockEntity xbe) {
				xbe.p = payload.p();
				xbe.q = payload.q();
				xbe.tuningRef = payload.tuningRef();
				be.markDirty();
				XenNoteBlock.refreshNote(world, pos);
			}
		});
	}

	private static void registerCommand() {
		ArgumentTypeRegistry.registerArgumentType(
			Identifier.of(MOD_ID, "tuning"),
			TuningArgumentType.class,
			ConstantArgumentSerializer.of(TuningArgumentType::tuning)
		);
		// /tuning set <from> [to] <tuning>
		// /tuning create <id> <tuning>
		// /tuning remove <tuning>
		// /tuning show <tuning>
		// /tuning showall
		// TODO /tuning gui
		CommandRegistrationCallback.EVENT.register(
			(dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("tuning")
				.then(CommandManager.literal("set")
					.then(CommandManager.argument("from", BlockPosArgumentType.blockPos())
						.then(CommandManager.argument("to", BlockPosArgumentType.blockPos())
							.then(CommandManager.argument("tuning", TuningArgumentType.tuning())
								.executes(context -> {
									ServerCommandSource source = context.getSource();
									ServerWorld world = source.getWorld();
									WorldTunings.getServerState(source.getServer());
									BlockPos pos1 = BlockPosArgumentType.getBlockPos(context, "from");
									BlockPos pos2 = BlockPosArgumentType.getBlockPos(context, "to");
									TuningRef ref = TuningArgumentType.getTuningRef(context, "tuning");
									//BlockBox box = BlockBox.create(pos1, pos2);
									for (BlockPos pos : BlockPos.iterate(pos1, pos2)) {
										BlockEntity be = world.getBlockEntity(pos);
										if (be instanceof XenNoteBlockEntity xbe) {
											xbe.tuningRef = ref;
											XenNoteBlock.refreshNote(world, pos);
										}
									}
									return 1;
								})
							)
						).then(CommandManager.argument("tuning", TuningArgumentType.tuning())
							.executes(context -> {
								ServerCommandSource source = context.getSource();
								ServerWorld world = source.getWorld();
								WorldTunings.getServerState(source.getServer());
								BlockPos pos = BlockPosArgumentType.getBlockPos(context, "from");
								TuningRef ref = TuningArgumentType.getTuningRef(context, "tuning");
								BlockEntity be = world.getBlockEntity(pos);
								if (be instanceof XenNoteBlockEntity xbe) {
									xbe.tuningRef = ref;
									XenNoteBlock.refreshNote(world, pos);
								}
								return 1;
							})
						)
					)
				).then(CommandManager.literal("create")
					.then(CommandManager.argument("id", StringArgumentType.word())
						.then(CommandManager.argument("tuning", TuningArgumentType.tuning())
							.executes(context -> {
								MinecraftServer server = context.getSource().getServer();
								WorldTunings tunings = WorldTunings.getServerState(server);
								String id = StringArgumentType.getString(context, "id");
								if (id.matches("[A-Za-z_]\\w*")) {
									Tuning tuning = TuningArgumentType.getTuning(context, "tuning");
									tunings.tunings.put(id, tuning);
									for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
										ServerPlayNetworking.send(player, new UpdateTuningPayload(id, Optional.of(tuning)));
									return 1;
								} else {
									throw INVALID_TUNING_ID.create(id);
								}
							})
						)
					)
				).then(CommandManager.literal("remove")
					.then(CommandManager.argument("id", StringArgumentType.word())
						.executes(context -> {
							MinecraftServer server = context.getSource().getServer();
							WorldTunings tunings = WorldTunings.getServerState(server);
							String id = StringArgumentType.getString(context, "id");
							tunings.tunings.remove(id);
							if (WorldTunings.BUILT_IN_TUNINGS.containsKey(id))
								tunings.tunings.put(id, WorldTunings.BUILT_IN_TUNINGS.get(id));
							for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList())
								ServerPlayNetworking.send(player, new UpdateTuningPayload(id, Optional.empty()));
							return 1;
						})
					)
				).then(CommandManager.literal("show")
					.then(CommandManager.argument("tuning", TuningArgumentType.tuning())
						.executes(context -> {
							MinecraftServer server = context.getSource().getServer();
							WorldTunings.getServerState(server);
							context.getSource().sendFeedback(
								() -> Text.of(TuningArgumentType.getTuning(context, "tuning").toNbt().toString()),
								false);
							return 1;
						})
					)
				).then(CommandManager.literal("showall")
					.executes(context -> {
						MinecraftServer server = context.getSource().getServer();
						var set = WorldTunings.getServerState(server).tunings.entrySet();
						if (set.isEmpty()){
							context.getSource().sendFeedback(() -> Text.of("empty"), false);
						}
						for (Map.Entry<String, Tuning> i : set)
							context.getSource().sendFeedback(
								() -> Text.of(
									"id: " + i.getKey()
									+ " tuning: " + i.getValue().toNbt().toString()),
								false);
						return 1;
					})
				)
			)
		);
	}
}