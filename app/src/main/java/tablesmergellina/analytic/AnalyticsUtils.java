package tablesmergellina.analytic;

import tablesmergellina.model.Column;
import tablesmergellina.json.JsonParser;
import tablesmergellina.model.Table;
import tablesmergellina.model.Pair;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class AnalyticsUtils {

	private AnalyticsUtils() {
	}

	public static long countNotEmptyCells(List<Table> tables) {
		return extractColumns(tables)
				.stream()
				.map(Column::getCells)
				.flatMap(Collection::stream)
				.filter(Predicate.not(String::isBlank))
				.count();
	}

	public static Map<String, Long> extractTopCommonCellsContent(List<Table> tables) {
		Map<String, Long> valuesMap = new HashMap<>();

		tables.stream()
				.map(Table::getColumns)
				.flatMap(Collection::stream)
				.map(Column::getCells)
				.flatMap(Collection::stream)
				.forEach(s -> {
					valuesMap.putIfAbsent(s, 0L);
					valuesMap.put(s, valuesMap.get(s) + 1);
				});

		Map<String, Long> commonestValues = valuesMap
				.entrySet()
				.stream()
				.filter(e -> e.getValue() > 5000)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return sortByValue(commonestValues);
	}

	public static List<Pair<String, Long>> extractTopCommonCellsContent(List<Table> tables, int k) {
		List<Map.Entry<String, Long>> cellsContent2Frequency = new ArrayList<>(extractTopCommonCellsContent(tables).entrySet());
		Collections.reverse(cellsContent2Frequency);
		return cellsContent2Frequency.stream()
				.limit(k)
				.map(e -> new Pair<>(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	private static List<Column> extractColumns(List<Table> tables) {
		return tables
				.stream()
				.map(Table::getColumns)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public static void runAnalytics(String tablesPathString, int k) {
		List<Table> tables;
		try {
			tables = JsonParser.readCellsInColumnsInTables(tablesPathString);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
		System.out.println("Total tables: " + tables.size());
		System.out.println("Not empty cells: " + countNotEmptyCells(tables));
		System.out.println("Top " + k + " commons cells values:");
		extractTopCommonCellsContent(tables, k).forEach(c -> System.out.println(c.getKey() + ": " + c.getValue()));
	}
}
