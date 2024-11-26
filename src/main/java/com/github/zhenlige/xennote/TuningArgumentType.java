package com.github.zhenlige.xennote;

import com.github.zhenlige.xennote.annotation.NeedWorldTunings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class TuningArgumentType implements ArgumentType<TuningRef> {
	private static final Logger LOGGER = LoggerFactory.getLogger(TuningArgumentType.class);

	public static TuningArgumentType tuning() {
		return new TuningArgumentType();
	}

	@NeedWorldTunings
	public static <S> Tuning getTuning(CommandContext<S> context, String name) {
		return getTuningRef(context, name).getTuning();
	}

	public static <S> TuningRef getTuningRef(CommandContext<S> context, String name) {
		return context.getArgument(name, TuningRef.class);
	}

	private static final Collection<String> EXAMPLES = List.of(
		// ET
		"17.3123",
		"12edo",
		"12.0232edo",
		"12/1edo",
		"19ed3",
		"19.056ed3",
		"19/1ed3",
		"19ed3.0",
		"19.056ed3.0",
		"19/1ed3.0",
		"19ed3/1",
		"19.056ed3/1",
		"19/1ed3/1",
		// tuning ID
		"ji",
		// tuning NBT
		"{type:\"primeMap\",primeMap:[{prime:2,mapTo:0.69},{prime:3,mapTo:1.10}]}"
	);
	public static final DynamicCommandExceptionType INVALID_TUNING = new DynamicCommandExceptionType(
		o -> Text.translatable("argument.tuning.invalid",  o)
	);

	@Override
	public TuningRef parse(StringReader reader) throws CommandSyntaxException {
		// read argument string
		int argBeginning = reader.getCursor();
		if (!reader.canRead()) reader.skip();
		while (reader.canRead() && reader.peek() != ' ') reader.skip();
		String str = reader.getString().substring(argBeginning, reader.getCursor());
		LOGGER.debug("Argument string: {}", str);
		try {
			Tuning tuning;
			// try NBT representation
			try {
				LOGGER.debug("Try NBT compound representation");
				NbtCompound nbt = new StringNbtReader(new StringReader(str)).parseCompound();
				tuning = Tuning.fromNbt(nbt);
			} catch (CommandSyntaxException ex) {
				try {
					LOGGER.debug("Try ET");
					// try ET
					int p = str.lastIndexOf("ed");
					double ed, period;
					if (p != -1) {
						ed = XennoteMath.parseDouble(str.substring(0, p));
						String periodStr = str.substring(p + 2);
						period = switch (periodStr) {
							case "o", "O" -> 2.;
							case "t", "T" -> 3.;
							case "f", "F" -> 1.5;
							case "e", "E" -> Math.E;
							case "phi", "Phi" -> XennoteMath.PHI;
							default -> XennoteMath.parseDouble(periodStr);
						};
						if (ed > 0.) {
							if (period > 1.) {
								LOGGER.debug("Parsed {}ed{} (aka {})", ed, periodStr, period);
								tuning = EqualTuning.of(ed, period);
							} else throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().create(period, 1.);
						} else throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().create(ed, 0.);
					} else {
						ed = XennoteMath.parseDouble(str);
						if (ed > 0.) {
							LOGGER.debug("Parsed {}ede", ed);
							tuning = new EqualTuning(ed);
						} else throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().create(ed, 0.);
					}
				} catch (NumberFormatException ex1) {
					LOGGER.debug("Parsed tuning ID: {}", str);
					// try world tunings
					if (str.matches("[A-Za-z_]\\w*"))
						return TuningRef.ofVar(str);
					else throw new Exception(str);
				}
			}
			return TuningRef.ofConst(tuning);
		} catch (Exception ex) {
			reader.setCursor(argBeginning);
			throw INVALID_TUNING.createWithContext(reader, ex.getMessage());
		}
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}
