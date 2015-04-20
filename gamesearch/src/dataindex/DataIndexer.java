package dataindex;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.*;

/*
 * Core class that indexes text files located at directory directory
 */

public class DataIndexer {
	private String dataDirectory;
	private List<String> fieldTypes;
	private List<String> fieldNames;
	private Directory index;
	private StandardAnalyzer analyzer;
	private IndexWriterConfig config;
	private IndexWriter writer;
	
	public DataIndexer() {
		fieldTypes = new ArrayList<String>();
		fieldNames = new ArrayList<String>();
	}
	
	public DataIndexer createIndex(String directory) {
		try {
			File file = new File(directory);
			index = new SimpleFSDirectory(file.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public DataIndexer setAnalyzer(StandardAnalyzer s) {
		analyzer = s;
		return this;
	}
	
	public DataIndexer createIndexWriter() {
		config = new IndexWriterConfig(analyzer);
		try {
			writer = new IndexWriter(index, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public DataIndexer setDataDirectory(String directory) {
		dataDirectory = directory;
		return this;
	}
	
	public DataIndexer addField(String fieldType, String name) {
		fieldTypes.add(fieldType);
		fieldNames.add(name);
		return this;
	}
	
	public void indexData() {
		try {
			File folder = new File(dataDirectory);
			for (File fileEntry : folder.listFiles()) {
				if (!fileEntry.isDirectory()) {
					indexFile(fileEntry);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void indexFile(File file) throws IOException {
		String [] data = Utility.readWebpageTextData(file.getAbsolutePath());
		Document doc = new Document();
		if (data.length == fieldTypes.size()) {
			for (int i = 0; i < fieldTypes.size(); i++) {
				String fieldType = fieldTypes.get(i);
				String fieldName = fieldNames.get(i);
				if (fieldType.equals("STRING_FIELD")) {
					doc.add(new StringField(fieldName, data[i], Field.Store.YES));
				} else if (fieldType.equalsIgnoreCase("TEXT_FIELD")) {
					doc.add(new TextField(fieldName, data[i], Field.Store.YES));
				}
			}
		} else {
			System.out.println("Data in wrong format... ignoring");
		}
		writer.addDocument(doc);
	}
}
