package tablesmergellina;

import lombok.extern.slf4j.Slf4j;
import tablesmergellina.analytic.AnalyticsUtils;
import tablesmergellina.exception.IndexingException;
import tablesmergellina.json.JsonParser;
import tablesmergellina.model.Column;
import tablesmergellina.model.Table;
import tablesmergellina.search.SearchController;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;

@Slf4j
public class App {
	private static final int K = 10;
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Provide tables.json absolute path");
			exit(0);
		}

		AnalyticsUtils.runAnalytics(args[0], K);

		SearchController searchController = new SearchController("index", K);
		if (!searchController.isIndexed()) {
			try {
				List<Column> columns = JsonParser.readCellsInColumnsInTables(args[0])
						.stream()
						.map(Table::getColumns)
						.flatMap(Collection::stream)
						.collect(Collectors.toList());
				searchController.indexDocs(columns);
			} catch (IndexingException | IOException e) {
				System.err.println(e.getMessage());
			}
		}

		if (args.length >= 2) {
			List<String> resultTablesIds = searchController.runUserQuery(String.join(" ", Arrays.asList(args).subList(1, args.length)));
			System.out.println("Top " + K + " matching tables ids:");
			resultTablesIds.forEach(System.out::println);
		}
	}
}

// gradle run --args="C:/Users/Gren/Desktop/tables.json -Xmx30g"

// CONTENUTO CELLE CHE SI RIPETE ALMENO UNA VOLTA (>2)
// 1264526

// CELLE TOTALI RAGGRUPPATE PER CONTENUTO
// 9357600

// NUMERO TABELLE
// 550271

// NUMERO COLONNE NO HEADER, NON VUOTE
// 2355380

// NUMERO COLONNE TOTALI
// 2408842

// TUTTE LE CELLE
// 43452478

// SOLO VUOTE
// 3967119

// SOLO NON VUOTE
// 39485359


// TUTTE, NO HEADER
// 40408049

// NON VUOTE, NO HEADER
// 36473921

// SOLO VUOTE, NO HEADER
// 3934128


// TUTTE, SOLO HEADER
// 3044429

// NON VUOTE, SOLO HEADER
// 3011438

// SOLO VUOTE, SOLO HEADER
// 32991
