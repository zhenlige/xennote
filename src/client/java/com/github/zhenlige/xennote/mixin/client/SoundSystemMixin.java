package com.github.zhenlige.xennote.mixin.client;

import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
	private static final float MIN_PITCH = 0.0F;
	private static final float MAX_PITCH = Float.POSITIVE_INFINITY;
	
	@ModifyConstant(method = "getAdjustedPitch", constant = @Constant(floatValue = 0.5F))
	private float minPitch(float value) {
		return MIN_PITCH;
	}
	
	@ModifyConstant(method = "getAdjustedPitch", constant = @Constant(floatValue = 2.0F))
	private float maxPitch(float value) {
		return MAX_PITCH;
	}
}