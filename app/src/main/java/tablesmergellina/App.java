package tablesmergellina;

import lombok.extern.slf4j.Slf4j;
import tablesmergellina.json.JsonParser;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.exit;

@Slf4j
public class App {
	private static final String SEPARATOR = "_$_";

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Provide tables.json absolute path");
			exit(0);
		}
		List<String> columns = JsonParser.readCellsInColumnsInTables(args[0])
				.stream()
				.flatMap(Collection::stream)
				.map(s -> String.join(SEPARATOR, s))
				.collect(Collectors.toList());

		log.info("Columns extract: {}", columns.subList(0, 10));
	}
}

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
