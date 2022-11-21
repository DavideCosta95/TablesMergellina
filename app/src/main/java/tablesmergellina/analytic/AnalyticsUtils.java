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

	public static long countColumns(List<Table> tables) {
		return tables
				.stream()
				.map(Table::getColumns)
				.mapToLong(Collection::size)
				.sum();
	}

	public static long countRows(List<Table> tables) {
		return tables
				.stream()
				.map(Table::getColumns)
				.filter(Predicate.not(List::isEmpty))
				.map(l -> {
					Column max = l.get(0);
					for (Column c : l) {
						if (c.getCells().size() > max.getCells().size()) {
							max = c;
						}
					}
					return max;
				})
				.map(Column::getCells)
				.map(List::size)
				.count();
	}

	public static int countSingleTableRows(Table table) {
		Column max = table.getColumns().get(0);
		for (Column c : table.getColumns()) {
			if (c.getCells().size() > max.getCells().size()) {
				max = c;
			}
		}
		return max.getCells().size();
	}

	public static long countDistinctCellsValues(List<Table> tables) {
		return tables
				.stream()
				.map(Table::getColumns)
				.flatMap(Collection::stream)
				.map(Column::getCells)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet())
				.size();
	}

	public static Map<Integer, Long> groupTablesByColumnsCount(List<Table> tables) {
		Map<Integer, Long> count = new HashMap<>();
		tables.forEach(t -> {
			count.putIfAbsent(t.getColumns().size(), 0L);
			count.compute(t.getColumns().size(), (k, v) -> v + 1);
		});
		return count;
	}

	public static Map<Integer, Long> groupTablesByRowsCount(List<Table> tables) {
		Map<Integer, Long> count = new HashMap<>();
		tables.forEach(t -> {
			int currentCount;
			if (t.getColumns().isEmpty()) {
				currentCount = 0;
			} else {
				currentCount = countSingleTableRows(t);
			}
			count.putIfAbsent(currentCount, 0L);
			count.compute(currentCount, (k, v) -> v + 1);
		});
		return count;
	}

	private static Map<Integer, Long> groupByK(Map<Integer, Long> map, int k) {
		Map<Integer, Long> groupedMap = new HashMap<>();
		map.forEach((key, value) -> {
			int currentKey = ((key / k) + 1) * k;
			groupedMap.putIfAbsent(currentKey, 0L);
			groupedMap.compute(currentKey, (__, v) -> v + value);
		});
		return sortByKey(groupedMap);
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

	public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByKey());

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	public static void runAnalytics(String tablesPathString, int k) {
		List<Table> tables;
		try {
			tables = JsonParser.readTables(tablesPathString);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		long tablesNumber = tables.size();
		long columnsNumber = countColumns(tables);
		long rowsNumber = countRows(tables);
		System.out.println("Total tables: " + tablesNumber);
		System.out.println("Not empty cells: " + countNotEmptyCells(tables));
		System.out.println("Columns count " + columnsNumber);
		System.out.println("Rows count " + rowsNumber);
		System.out.println("Columns count distribution: " + groupByK(groupTablesByColumnsCount(tables), 20));
		System.out.println("Rows count distribution: " + groupByK(groupTablesByRowsCount(tables), 200));
		System.out.printf("Average rows count %.4f%n", rowsNumber / (float) tablesNumber);
		System.out.printf("Average columns count %.4f%n", columnsNumber / (float) tablesNumber);
		System.out.println("Cells distinct values: " + countDistinctCellsValues(tables));
		System.out.println("Top " + k + " commons cells values:");
		extractTopCommonCellsContent(tables, k).forEach(c -> System.out.println(c.getKey() + ": " + c.getValue()));
	}
}
