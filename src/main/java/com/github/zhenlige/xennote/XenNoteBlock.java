package com.github.zhenlige.xennote;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class XenNoteBlock extends NoteBlock implements BlockEntityProvider {
	public XenNoteBlock(Settings settings) {
		super(settings);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new XenNoteBlockEntity(pos, state);
	}
	
	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.SUCCESS;
		XenNoteBlockEntity be = (XenNoteBlockEntity) world.getBlockEntity(pos);
		ServerPlayNetworking.send((ServerPlayerEntity) player,
				new XennotePayload(GlobalPos.create(world.getRegistryKey(), pos),
				be.p, be.q, be.edo));
		return ActionResult.CONSUME;
	}

	@Override
	protected boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
		NoteBlockInstrument noteBlockInstrument = (NoteBlockInstrument) state.get(INSTRUMENT);
		float f;
		if (noteBlockInstrument.canBePitched()) {
			// int i = (Integer) state.get(NOTE);
			// f = getNotePitch(i);
			if (world.isClient) world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
			XenNoteBlockEntity be = (XenNoteBlockEntity) world.getBlockEntity(pos);
			Temp temp = be.edo==0 ? Temp.JI : EqualTemp.ofOctave(be.edo);
			f = temp.tune(new Rational(be.p, be.q));
			world.addParticle(ParticleTypes.NOTE, (double) pos.getX() + 0.5, (double) pos.getY() + 1.2,
					(double) pos.getZ() + 0.5, 0.5 + Math.log((double) f) / Math.log(4.0), 0.0, 0.0);
		} else {
			f = 1.0F;
		}

		RegistryEntry<SoundEvent> registryEntry;
		if (noteBlockInstrument.hasCustomSound()) {
			Identifier identifier = this.getCustomSound(world, pos);
			if (identifier == null) {
				return false;
			}

			registryEntry = RegistryEntry.of(SoundEvent.of(identifier));
		} else {
			registryEntry = noteBlockInstrument.getSound();
		}
		world.playSound((PlayerEntity) null, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5,
				(double) pos.getZ() + 0.5, registryEntry, SoundCategory.RECORDS, 3.0F, f, world.random.nextLong());
		return true;
	}

	private Identifier getCustomSound(World world, BlockPos pos) {
		BlockEntity var4 = world.getBlockEntity(pos.up());
		if (var4 instanceof SkullBlockEntity skullBlockEntity) {
			return skullBlockEntity.getNoteBlockSound();
		} else {
			return null;
		}
	}
}