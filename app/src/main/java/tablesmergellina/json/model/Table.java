package tablesmergellina.json.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Table {
	@JsonProperty("_id")
	private TableId keyId;

	@JsonProperty
	private String className;

	@JsonProperty
	private String id;

	@JsonProperty
	private List<Cell> cells;

	@JsonProperty
	private String beginIndex;

	@JsonProperty
	private String endIndex;

	@JsonProperty
	private String referenceContext;

	@JsonProperty
	private String type;

	@JsonProperty
	private String classe;

	@JsonProperty
	private Dimension maxDimensions;

	@JsonProperty
	private List<String> headersCleaned;

	@JsonProperty
	private long keyColumn;

	public String getKeyId() {
		return keyId.getId();
	}

	@NoArgsConstructor
	@Data
	static class TableId {
		@JsonProperty("$oid")
		private String id;
	}


	@NoArgsConstructor
	@Data
	static class Dimension {
		@JsonProperty("row")
		private long rowNumber;

		@JsonProperty("column")
		private long columnNumber;
	}
}
