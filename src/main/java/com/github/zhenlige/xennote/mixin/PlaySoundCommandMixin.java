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
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.PlaySoundCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import static net.minecraft.command.argument.EntityArgumentType.getPlayers;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.Vec3ArgumentType.getVec3;
import static net.minecraft.server.command.CommandManager.*;

@Mixin(PlaySoundCommand.class)
public abstract class PlaySoundCommandMixin {
	@Inject(method="makeArgumentsForCategory",at=@At("HEAD"),cancellable=true)
	private static void makeArgumentsForCategoryInject(SoundCategory category,
		CallbackInfoReturnable<LiteralArgumentBuilder<ServerCommandSource> > cir) {
		cir.setReturnValue(
			literal(category.getName()).executes((context) ->
				execute(context.getSource(),
					toList(context.getSource().getPlayer()),
					getIdentifier(context, "sound"),
					category,
					(context.getSource()).getPosition(),
					1.0F, 1.0F, 0.0F)
			).then(argument("targets", EntityArgumentType.players()).executes((context) ->
				execute(context.getSource(),
					getPlayers(context, "targets"),
					getIdentifier(context, "sound"),
					category,
					context.getSource().getPosition(),
					1.0F, 1.0F, 0.0F)
			).then(argument("pos", Vec3ArgumentType.vec3()).executes((context) ->
				execute(context.getSource(),
					getPlayers(context, "targets"),
					getIdentifier(context, "sound"),
					category,
					getVec3(context, "pos"),
					1.0F, 1.0F, 0.0F)
			).then(argument("volume", FloatArgumentType.floatArg(0.0F)).executes((context) ->
				execute(context.getSource(),
					getPlayers(context, "targets"),
					getIdentifier(context, "sound"),
					category,
					getVec3(context, "pos"),
					context.getArgument("volume", Float.class),
					1.0F, 0.0F)
			).then(argument("pitch", FloatArgumentType.floatArg(0.0F)).executes((context) ->
				execute(context.getSource(),
					getPlayers(context, "targets"),
					getIdentifier(context, "sound"),
					category,
					getVec3(context, "pos"),
					context.getArgument("volume", Float.class),
					context.getArgument("pitch", Float.class),
					0.0F)
			).then(argument("minVolume", FloatArgumentType.floatArg(0.0F, 1.0F)).executes((context) ->
				execute(context.getSource(),
					getPlayers(context, "targets"),
					getIdentifier(context, "sound"),
					category,
					getVec3(context, "pos"),
					context.getArgument("volume", Float.class),
					context.getArgument("pitch", Float.class),
					context.getArgument("minVolume", Float.class))
			))))))
		);
	}

	@Shadow private static Collection<ServerPlayerEntity>
	toList(@Nullable ServerPlayerEntity player) {return List.of();}

	@Shadow private static int execute(
		ServerCommandSource source,
		Collection<ServerPlayerEntity> targets,
		Identifier sound, SoundCategory category,
		Vec3d pos, float volume, float pitch, float minVolume) throws CommandSyntaxException {return 0;}
}