package com.github.zhenlige.xennote.mixin;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.PlaySoundCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@Mixin(PlaySoundCommand.class)
public abstract class PlaySoundCommandMixin {
	@Inject(method="makeArgumentsForCategory",at=@At("HEAD"),cancellable=true)
	private static void makeArgumentsForCategoryInject(SoundCategory category,
		CallbackInfoReturnable<LiteralArgumentBuilder<ServerCommandSource> > cir) {
		cir.setReturnValue((LiteralArgumentBuilder<ServerCommandSource>)((LiteralArgumentBuilder<ServerCommandSource>)CommandManager.literal(category.getName()).executes((context) -> {
			return execute((ServerCommandSource)context.getSource(), toList(((ServerCommandSource)context.getSource()).getPlayer()), IdentifierArgumentType.getIdentifier(context, "sound"), category, ((ServerCommandSource)context.getSource()).getPosition(), 1.0F, 1.0F, 0.0F);
		})).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes((context) -> {
			return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, ((ServerCommandSource)context.getSource()).getPosition(), 1.0F, 1.0F, 0.0F);
		})).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes((context) -> {
			return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), 1.0F, 1.0F, 0.0F);
		})).then(((RequiredArgumentBuilder)CommandManager.argument("volume", FloatArgumentType.floatArg(0.0F)).executes((context) -> {
			return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), (Float)context.getArgument("volume", Float.class), 1.0F, 0.0F);
		})).then(((RequiredArgumentBuilder)CommandManager.argument("pitch", FloatArgumentType.floatArg(0.0F)).executes((context) -> {
			return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), (Float)context.getArgument("volume", Float.class), (Float)context.getArgument("pitch", Float.class), 0.0F);
		})).then(CommandManager.argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((context) -> {
			return execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers(context, "targets"), IdentifierArgumentType.getIdentifier(context, "sound"), category, Vec3ArgumentType.getVec3(context, "pos"), (Float)context.getArgument("volume", Float.class), (Float)context.getArgument("pitch", Float.class), (Float)context.getArgument("minVolume", Float.class));
		})))))));
	}

	@Shadow private static Collection<ServerPlayerEntity> toList(@Nullable ServerPlayerEntity player){return List.of();};

	@Shadow private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Identifier sound, SoundCategory category, Vec3d pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {return 0;};
}