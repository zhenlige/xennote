package com.github.zhenlige.xennote;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class XennoteModMenuIntegration implements ModMenuApi {
	@Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
        	ConfigBuilder builder = ConfigBuilder.create()
			        .setParentScreen(parent)
			        .setTitle(Text.translatable("title.xennote.global_config"));
        	ConfigCategory cat = 
					builder.getOrCreateCategory(Text.of(""));
        	ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        	return builder.build();
        };
    }
}