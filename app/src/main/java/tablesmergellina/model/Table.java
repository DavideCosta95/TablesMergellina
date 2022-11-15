package tablesmergellina.model;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class Table {
	@NonNull
	private String id;

	@NonNull
	private List<Column> columns;
}
