package tablesmergellina.json;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import tablesmergellina.json.model.Cell;
import tablesmergellina.json.model.Table;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public final class JsonParser {
	private JsonParser() {}

	public static List<List<List<String>>> readCellsInColumnsInTables(String filePath) {
		final JsonMapper mapper = new JsonMapper();
		final File input = Paths.get(filePath).toFile();
		List<List<List<String>>> cellsInColumnsInTables = new ArrayList<>();

		try (MappingIterator<Table> it = mapper.readerFor(Table.class).readValues(input)) {
			while (it.hasNextValue()) {
				Table currentTable = it.nextValue();
				cellsInColumnsInTables.add(extractColumns(currentTable.getCells()));
			}
			return cellsInColumnsInTables;
		} catch (IOException e) {
			log.error("", e);
			return Collections.emptyList();
		}
	}

	public static List<List<String>> extractColumns(List<Cell> cells) {
		Map<Long, List<String>> columnNumber2Content = new HashMap<>();
		cells.stream()
				.filter(c -> !c.isHeader() && !c.getCleanedText().isBlank())
				.forEach(c -> columnNumber2Content.computeIfAbsent(c.getCoordinates().getColumnNumber(), k -> new ArrayList<>()).add(c.getCleanedText()));
		return new ArrayList<>(columnNumber2Content.values());
	}
}
