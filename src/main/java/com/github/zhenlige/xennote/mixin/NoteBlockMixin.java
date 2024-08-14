package com.github.zhenlige.xennote.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(NoteBlock.class)
public abstract class NoteBlockMixin extends Block {
	public static final Logger LOGGER = LoggerFactory.getLogger("xennote.NoteBlockMixin");
	public NoteBlockMixin(Settings settings) {
		super(settings);
	}
/*
	@ModifyVariable(method = "onSyncedBlockEvent",
			at=@At(value="INVOKE",target="Lnnet.minecraft.block.NoteBlock;getNotePitch(I)F"),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	private void getNotePitchInjected(int note, 
		CallbackInfo ci) {
		ci.
	}//*/
}