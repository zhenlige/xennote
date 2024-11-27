package com.github.zhenlige.xennote;

import com.google.common.collect.Lists;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.text.Text;
import org.apache.commons.numbers.primes.Primes;

import java.util.*;

public final class EntryManager {
	/** To avoid accidentally create instances of this class. */
	private EntryManager() {}

	private static class SinglePrimeMap {
		public int prime;
		public double mapTo;

		public SinglePrimeMap(int prime, double mapTo) {
			this.prime = prime;
			this.mapTo = mapTo;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			return prime == ((SinglePrimeMap) o).prime && mapTo == ((SinglePrimeMap) o).mapTo;
		}

		public MultiElementListEntry<SinglePrimeMap> createEntry(ConfigEntryBuilder entryBuilder) {
			return new MultiElementListEntry<>(
				Text.translatable("option.xennote.prime_map"),
				this,
				Lists.newArrayList(
					entryBuilder.startIntField(Text.translatable("option.xennote.prime"), prime)
						.setMin(2)
						.setSaveConsumer((newPrime) -> prime = newPrime)
						.build(),
					entryBuilder.startDoubleField(Text.translatable("option.xennote.map_to"), mapTo)
						.setSaveConsumer((newMapTo) -> mapTo = newMapTo)
						.build()
				),
				true
			);
		}
	}

	private static class MultiPrimeMap {
		private List<SinglePrimeMap> maps;

		public MultiPrimeMap() {
			maps = new ArrayList<>();
		}

		public MultiPrimeMap(Map<Integer, Double> map) {
			maps = new ArrayList<>();
			for (Map.Entry<Integer, Double> i : map.entrySet()) {
				maps.add(new SinglePrimeMap(i.getKey(), i.getValue()));
			}
			maps.sort(Comparator.comparingInt(i -> i.prime));
		}

		public NestedListListEntry<SinglePrimeMap, MultiElementListEntry<SinglePrimeMap> >
		createEntry(ConfigEntryBuilder entryBuilder) {
			return new NestedListListEntry<>(
				Text.translatable("option.xennote.prime_map_list"),
				maps,
				true,
				Optional::empty,
				list -> maps = list,
				() -> maps,
				entryBuilder.getResetButtonKey(),
				true,
				false,
				(elem, entry) -> {
					if (elem == null) {
						int p = 2;
						for (SinglePrimeMap i : entry.getValue()) {
							if (i.prime >= p) p = Primes.nextPrime(i.prime + 1);
						}
						return new SinglePrimeMap(p, Math.log(p)).createEntry(entryBuilder);
					} else {
						return elem.createEntry(entryBuilder);
					}
				}
			);
		}

		public Map<Integer, Double> getMap() {
			Map<Integer, Double> map = new HashMap<>();
			for (SinglePrimeMap i : maps) {
				map.put(i.prime, i.mapTo);
			}
			return map;
		}
	}

	public static class SingleTuning {
		private Tuning.TuningType type = Tuning.TuningType.JI;
		private double stretch = 1.;
		private double ede = 12 / Math.log(2.);
		private MultiPrimeMap primeMap = new MultiPrimeMap();

		public SingleTuning() {}

		public SingleTuning(Tuning tuning) {
			type = tuning.getType();
			switch (type) {
				case JI:
					break;
				case EQUAL:
					ede = ((EqualTuning) tuning).ede;
					break;
				case PRIME_MAP:
					primeMap = new MultiPrimeMap(((PrimeMapTuning) tuning).map);
			}
		}

		public Tuning getTuning() {
			return switch (type) {
				case JI -> Tuning.ji().setStretch(stretch);
				case EQUAL -> new EqualTuning(ede).setStretch(stretch);
				case PRIME_MAP -> new PrimeMapTuning(primeMap.getMap()).setStretch(stretch);
			};
		}

		public SubCategoryListEntry createEntry(ConfigEntryBuilder entryBuilder) {
			SubCategoryBuilder builder = entryBuilder.startSubCategory(Text.translatable("option.xennote.tuning"))
				.setExpanded(true);
			var typeEntry = entryBuilder.startEnumSelector(
				Text.translatable("option.xennote.type"),
				Tuning.TuningType.class,
				type
			).setSaveConsumer(newType -> type = newType)
				.build();
			builder.add(typeEntry);
			builder.add(entryBuilder.startDoubleField(Text.translatable("option.xennote.stretch"), stretch)
				.setSaveConsumer(newStretch -> stretch = newStretch)
				.build());
			builder.add(entryBuilder.startDoubleField(
				Text.translatable("option.xennote.ede"),
				ede
			).setSaveConsumer(newEde -> ede = newEde)
				.setRequirement(Requirement.isValue(typeEntry, Tuning.TuningType.EQUAL))
				.build());
			var primeMapEntry = primeMap.createEntry(entryBuilder);
			primeMapEntry.setRequirement(Requirement.isValue(typeEntry, Tuning.TuningType.PRIME_MAP));
			builder.add(primeMapEntry);
			return builder.build();
		}
	}

	public static class SingleTuningRef {
		private TuningRef.TuningRefType type = TuningRef.TuningRefType.VAR;
		private SingleTuning tuning;
		private String id = "ji";

		public SingleTuningRef() {
			tuning = new SingleTuning();
		}

		public SingleTuningRef(TuningRef ref) {
			tuning = new SingleTuning(ref.getTuning());
			type = ref.getType();
			id = switch (type) {
				case VAR -> ((TuningRef.VarTuningRef) ref).id;
				case CONST -> "ji";
			};
		}

		public SubCategoryListEntry createEntry(ConfigEntryBuilder entryBuilder) {
			SubCategoryBuilder builder = entryBuilder.startSubCategory(Text.translatable("option.xennote.tuning_ref"))
				.setExpanded(true);
			var typeEntry = entryBuilder.startEnumSelector(
				Text.translatable("option.xennote.type"),
				TuningRef.TuningRefType.class,
				type
			).setSaveConsumer(newType -> type = newType)
				.build();
			builder.add(typeEntry);
			builder.add(entryBuilder.startStrField(Text.translatable("option.xennote.tuning_id"), id)
				.setSaveConsumer(newId -> id = newId)
				.setRequirement(Requirement.isValue(typeEntry, TuningRef.TuningRefType.VAR))
				.build());
			var tuningEntry = tuning.createEntry(entryBuilder);
			tuningEntry.setRequirement(Requirement.isValue(typeEntry, TuningRef.TuningRefType.CONST));
			builder.add(tuningEntry);
			return builder.build();
		}

		public TuningRef getTuningRef() {
			return switch (type) {
				case VAR -> TuningRef.ofVar(id);
				case CONST -> TuningRef.ofConst(tuning.getTuning());
			};
		}
	}
}
