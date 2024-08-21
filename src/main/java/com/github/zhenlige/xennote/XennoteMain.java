package com.github.zhenlige.xennote;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XennoteMain implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("xennote");
	public static final XenNoteBlock NOTE_BLOCK = new XenNoteBlock(Block.Settings.copy(Blocks.NOTE_BLOCK));
	public static final BlockItem NOTE_BLOCK_ITEM = new BlockItem(NOTE_BLOCK, new Item.Settings());
	public static final BlockEntityType<XenNoteBlockEntity> NOTE_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE, Identifier.of("xennote", "note_block_entity"),
			BlockEntityType.Builder.create(XenNoteBlockEntity::new, NOTE_BLOCK).build());
	
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Loading");
		Registry.register(Registries.BLOCK, Identifier.of("xennote", "note_block"), NOTE_BLOCK);
		Registry.register(Registries.ITEM, Identifier.of("xennote", "note_block"), NOTE_BLOCK_ITEM);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(content -> {
			content.addAfter(Items.NOTE_BLOCK, NOTE_BLOCK_ITEM);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.NOTE_BLOCK, NOTE_BLOCK_ITEM);
		});
		PayloadTypeRegistry.playS2C().register(XennotePayload.ID, XennotePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(XennotePayload.ID, XennotePayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(XennotePayload.ID, (payload, context) -> {
			World world = context.server().getWorld(payload.pos().dimension());
			BlockPos pos = payload.pos().pos();
			BlockState state = world.getBlockState(pos);
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof XenNoteBlockEntity xbe) {
				xbe.p = payload.p();
				xbe.q = payload.q();
				xbe.edo = payload.edo();
				be.markDirty();
				int approx = Math.floorMod((int) Math.round(xbe.getLogPitch() * 12.0 / Math.log(2.0)) + 12, 24);
				if(approx != state.get(NoteBlock.NOTE)) {
					world.setBlockState(pos, state.with(NoteBlock.NOTE, approx));
					world.markDirty(pos);
				}
				world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
			}
		});
		/* CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("xenupdate").executes((context) -> {
				MinecraftServer server = context.getSource().getServer();
				for (ServerWorld world : server.getWorlds()){
					world.blo
				}
				return 1;
			}));
		}); // */
	}
}