package tablesmergellina.model;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
public class Column {
	@NonNull
	private String tableId;

	@NonNull
	private List<String> cells;
}
