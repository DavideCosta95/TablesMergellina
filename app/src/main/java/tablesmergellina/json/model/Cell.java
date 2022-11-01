package tablesmergellina.json.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class Cell {
	@JsonProperty
	private String className;

	@JsonProperty
	private String innerHTML;

	@JsonProperty
	private boolean isHeader;

	@JsonProperty
	private String type;

	@JsonProperty("Coordinates")
	private Position coordinates;

	@JsonProperty
	private String cleanedText;

	@JsonProperty("Rows")
	private List<Row> rows;


	@NoArgsConstructor
	@Data
	public static class Position {
		@JsonProperty("row")
		private long rowNumber;

		@JsonProperty("column")
		private long columnNumber;
	}


	@NoArgsConstructor
	@Data
	static class Row {
		@JsonProperty("LinkBlue")
		private boolean blueLink;

		@JsonProperty("Title")
		private String title;

		@JsonProperty("Href")
		private String href;
	}
}
