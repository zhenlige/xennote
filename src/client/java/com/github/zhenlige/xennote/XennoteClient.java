package com.github.zhenlige.xennote;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.DoubleListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class XennoteClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientPlayNetworking.registerGlobalReceiver(XennotePayload.ID, (payload, context) -> {
			MinecraftClient client = context.client();
			client.execute(() -> {
				ConfigBuilder builder = ConfigBuilder.create()
				        .setParentScreen(client.currentScreen)
				        .setTitle(Text.translatable("title.xennote.config"));
				ConfigCategory cat = 
						builder.getOrCreateCategory(Text.of(""));
				ConfigEntryBuilder entryBuilder = builder.entryBuilder();
				IntegerListEntry entryP = entryBuilder.startIntField(Text.translatable("option.xennote.p"), payload.p()).setMin(1).build();
				cat.addEntry(entryP);
				IntegerListEntry entryQ = entryBuilder.startIntField(Text.translatable("option.xennote.q"), payload.q()).setMin(1).build();
				cat.addEntry(entryQ);
				DoubleListEntry entryEdo = entryBuilder.startDoubleField(Text.translatable("option.xennote.edo"), payload.edo()).setMin(0).build();
				cat.addEntry(entryEdo);
				builder.setSavingRunnable(() -> {
					Rational f = new SimplestRational(entryP.getValue(), entryQ.getValue());
					int p = f.p, q = f.q;
					ClientPlayNetworking.send(new XennotePayload(payload.pos(), p, q, entryEdo.getValue()));
				});
				Screen screen = builder.build();
				client.setScreen(screen);
			});
		});
	}
}