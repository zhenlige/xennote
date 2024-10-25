package com.github.zhenlige.xennote;

import com.github.zhenlige.xennote.annotation.NeedWorldTunings;
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

		map.put("ji", Tuning.JI);
		map.put("qcomMeantone", PrimeMapTuning.QCOM_MEANTONE);
		map.put("septQcomMeantone", PrimeMapTuning.SEPT_QCOM_MEANTONE);

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
			if (payload.tuningNbt() instanceof NbtCompound nbt && nbt.isEmpty()) {
				current.tunings.remove(payload.tuningId());
			} else {
				current.tunings.put(payload.tuningId(), Tuning.fromNbt(payload.tuningNbt()));
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(ClientInitPayload.ID, (payload, context) -> {
			current = createFromNbt(payload.tuningList(), null);
			log();
		});
	}

	@NeedWorldTunings
	public static WorldTunings getCurrent() {
		return current;
	}
}
