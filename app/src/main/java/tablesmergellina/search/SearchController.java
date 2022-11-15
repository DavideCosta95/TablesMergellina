package tablesmergellina.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tablesmergellina.model.Column;
import tablesmergellina.exception.IndexingException;
import tablesmergellina.exception.SearchException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SearchController {
	private static final String SEPARATOR = " ";
	private static final String INDEXED_FIELD_NAME = "column";
	private static final String TABLE_ID_FIELD_NAME = "tableId";
	private final String indexPath;
	private final int k;

	public SearchController(String indexPath, int k) {
		this.indexPath = indexPath;
		this.k = k;
	}

	public boolean isIndexed() {
		try (Directory directory = FSDirectory.open(Paths.get(indexPath))) {
			try (IndexReader reader = DirectoryReader.open(directory)) {
				return true;
			}
		} catch (IOException e) {
			return false;
		}
	}

	public void indexDocs(List<Column> columns) throws IndexingException {
		Path path = Paths.get(indexPath);
		IndexWriter writer;
		try (Directory indexDirectory = FSDirectory.open(path)) {

			Analyzer analyzer = new WhitespaceAnalyzer();
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			writer = new IndexWriter(indexDirectory, config);
			writer.deleteAll();
			columns.stream()
					.map(c -> {
						Document doc = new Document();
						StoredField tableId = new StoredField(TABLE_ID_FIELD_NAME, c.getTableId());
						TextField column = new TextField(INDEXED_FIELD_NAME, String.join(SEPARATOR, c.getCells()), Field.Store.YES);
						doc.add(column);
						doc.add(tableId);
						return doc;
					})
					.forEach(d -> {
						try {
							writer.addDocument(d);
						} catch (IOException e) {
							log.error("", e);
						}
					});
			writer.commit();
			writer.close();
		} catch (IOException e) {
			throw new IndexingException(e);
		}
	}

	private List<QueryResult> doSearch(String field, String queryString) throws SearchException {
		QueryParser parser = new QueryParser(field, new WhitespaceAnalyzer());
		try {
			Query query = parser.parse(queryString);
			try (Directory directory = FSDirectory.open(Paths.get(indexPath))) {
				try (IndexReader reader = DirectoryReader.open(directory)) {
					IndexSearcher searcher = new IndexSearcher(reader);
					return runQuery(searcher, query);
				}
			}
		} catch (IOException | ParseException e) {
			throw new SearchException(e);
		}
	}

	public List<String> runUserQuery(String query) {
		try {
			return doSearch(SearchController.INDEXED_FIELD_NAME, query)
					.stream()
					.map(QueryResult::getDocument)
					.map(Document::getFields)
					.flatMap(Collection::stream)
					.filter(f -> f.name().equals(SearchController.TABLE_ID_FIELD_NAME))
					.map(IndexableField::stringValue)
					.collect(Collectors.toList());
		} catch (SearchException e) {
			System.err.println(e.getMessage());
			return Collections.emptyList();
		}
	}

	private List<QueryResult> runQuery(IndexSearcher searcher, Query query) throws SearchException {
		return runQuery(searcher, query, false);
	}

	private List<QueryResult> runQuery(IndexSearcher searcher, Query query, boolean explain) throws SearchException {
		try {
			List<QueryResult> results = new ArrayList<>();
			TopDocs hits = searcher.search(query, k);
			for (int i = 0; i < hits.scoreDocs.length; i++) {
				ScoreDoc scoreDoc = hits.scoreDocs[i];
				Document doc = searcher.doc(scoreDoc.doc);
				Explanation explanation = explain ? searcher.explain(query, scoreDoc.doc) : null;
				results.add(new QueryResult(doc, scoreDoc.score, explanation));
			}
			return results;
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}
}
