package com.github.zhenlige.xennote;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

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
			BlockEntity be = context.server().getWorld(payload.pos().dimension()).getBlockEntity(payload.pos().pos());
			if(be instanceof XenNoteBlockEntity) {
				((XenNoteBlockEntity) be).p = payload.p();
				((XenNoteBlockEntity) be).q = payload.q();
				((XenNoteBlockEntity) be).edo = payload.edo();
				be.markDirty();
			}
		}); // */
	}
}