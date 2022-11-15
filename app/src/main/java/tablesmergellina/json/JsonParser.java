package tablesmergellina.json;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import tablesmergellina.model.Column;
import tablesmergellina.json.model.Cell;
import tablesmergellina.json.model.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public final class JsonParser {
	private JsonParser() {}

	public static List<tablesmergellina.model.Table> readCellsInColumnsInTables(String filePath) throws IOException {
		final JsonMapper mapper = new JsonMapper();
		final File input = Paths.get(filePath).toFile();
		List<tablesmergellina.model.Table> tables = new ArrayList<>();

		try (MappingIterator<Table> it = mapper.readerFor(Table.class).readValues(input)) {
			while (it.hasNextValue()) {
				Table currentTable = it.nextValue();
				tables.add(new tablesmergellina.model.Table(currentTable.getKeyId(), extractColumns(currentTable.getKeyId(), currentTable.getCells())));
			}
			return tables;
		}
	}

	private static List<Column> extractColumns(String tableId, List<Cell> cells) {
		Map<Long, List<String>> columnNumber2Content = new HashMap<>();
		cells.stream()
				.filter(c -> !c.isHeader() && !c.getCleanedText().isBlank())
				.forEach(c -> columnNumber2Content.computeIfAbsent(c.getCoordinates().getColumnNumber(), k -> new ArrayList<>()).add(c.getCleanedText()));
		return columnNumber2Content
				.values()
				.stream()
				.map(c -> new Column(tableId, c))
				.collect(Collectors.toList());
	}
}
