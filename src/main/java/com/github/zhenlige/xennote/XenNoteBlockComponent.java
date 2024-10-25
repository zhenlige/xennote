package com.github.zhenlige.xennote;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class XenNoteBlockComponent implements ComponentMap {

	@Nullable
	@Override
	public <T> T get(ComponentType<? extends T> type) {
		return null;
	}

	@Override
	public Set<ComponentType<?>> getTypes() {
		return Set.of();
	}
}
