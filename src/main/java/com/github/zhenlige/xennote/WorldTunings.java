package com.github.zhenlige.xennote;

import com.github.zhenlige.xennote.annotation.NeedWorldTunings;
import com.github.zhenlige.xennote.payload.ClientInitPayload;
import com.github.zhenlige.xennote.payload.UpdateTuningPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WorldTunings extends PersistentState {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorldTunings.class);
	public static final Map<String, Tuning> BUILT_IN_TUNINGS;
	private static WorldTunings current;

	static {
		Map<String, Tuning> map = new HashMap<>();

		map.put("ji", JI = new Tuning());
		map.put("qcomMeantone", QCOM_MEANTONE = new PrimeMapTuning(
			Math.log(2.),
			Math.log(80.) / 4.,
			Math.log(5.)
		));
		map.put("septQcomMeantone", SEPT_QCOM_MEANTONE = new PrimeMapTuning(
			Math.log(2.),
			Math.log(80.) / 4.,
			Math.log(5.),
			Math.log(5.) * 2.5 - Math.log(8.)
		));
		map.put("argent", ARGENT = new PrimeMapTuning(
			Math.log(2.),
			Math.log(2.) * (3. - Math.sqrt(2.))
		));
		map.put("argentHemi5ths", ARGENT_HEMI5THS = new PrimeMapTuning(
			Math.log(2.),
			Math.log(2.) * (3. - Math.sqrt(2.)),
			Math.log(2.) * (20 - 12.5 * Math.sqrt(2.)),
			Math.log(2.) * (12 - 6.5 * Math.sqrt(2.))
		));

		BUILT_IN_TUNINGS = map;
	}

	public Map<String, Tuning> tunings;

	public WorldTunings() {
		tunings = new HashMap<>(BUILT_IN_TUNINGS);
	}

	public WorldTunings(Map<String, Tuning> map) {
		tunings = map;
	}

	@Override
	@NotNull
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) { // !
		NbtList tuningsNbt = new NbtList();
		tunings.forEach((id, tuning) -> {
			if (!BUILT_IN_TUNINGS.containsValue(tuning)) {
				NbtCompound tuningNbt = tuning.toNbt();
				tuningNbt.putString("id", id);
				tuningsNbt.add(tuningNbt);
			}
		});
		nbt.put("tunings", tuningsNbt);
		return nbt;
	}

	public static WorldTunings createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		Map<String, Tuning> tunings = new HashMap<>(BUILT_IN_TUNINGS);
		if (nbt.contains("tunings", NbtElement.LIST_TYPE))
			for (NbtElement i : nbt.getList("tunings", NbtElement.COMPOUND_TYPE)) {
				if (i instanceof NbtCompound tuningNbt) {
					tunings.put(tuningNbt.getString("id"), Tuning.fromNbt(tuningNbt));
				}
			}
		return new WorldTunings(tunings);
	}

	private static final Type<WorldTunings> type = new Type<>(
		WorldTunings::new,
		WorldTunings::createFromNbt,
		null
	);

	private static void log() {
		for (var i : current.tunings.entrySet())
			LOGGER.debug("id: {} tuning: {}", i.getKey(), i.getValue().toNbt().toString());
	}

	public static WorldTunings getServerState(@NotNull MinecraftServer server) {
		LOGGER.debug("start getServerState()");
		PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
		WorldTunings state = manager.getOrCreate(type, Xennote.MOD_ID);
		state.markDirty();
		current = state;
		log();
		return state;
	}

	@Environment(EnvType.CLIENT)
	public static void initializeClient() {
		LOGGER.debug("start client initializing");
		ClientPlayNetworking.registerGlobalReceiver(UpdateTuningPayload.ID, (payload, context) -> {
			if (payload.tuning().isEmpty()) {
				current.tunings.remove(payload.tuningId());
			} else {
				current.tunings.put(payload.tuningId(), payload.tuning().orElse(JI));
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(ClientInitPayload.ID, (payload, context) -> {
			current = new WorldTunings(payload.tuningMap());
			log();
		});
	}

	@NeedWorldTunings
	public static WorldTunings getCurrent() {
		return current;
	}

	/** Just intonation. */
	public static final Tuning JI;

	/** Quarter-comma meantone. */
	public static final PrimeMapTuning QCOM_MEANTONE;

	/** Quarter-comma meantone with septimal mappings. */
	public static final PrimeMapTuning SEPT_QCOM_MEANTONE;

	/** Argent tuning. */
	public static final PrimeMapTuning ARGENT;

	/** Argent tuning with hemififths mappings. */
	public static final PrimeMapTuning ARGENT_HEMI5THS;
}
