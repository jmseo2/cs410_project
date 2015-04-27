package dataquery;

import java.io.IOException;

import org.apache.lucene.document.Document;
import util.Utility;

public class QueryData {
    public static void main(String[] args) {
        try {
            String query = "smite";
            DataQuerier querier 
                = new DataQuerier()
                    .setIndexDirectory("../data/indexboosted/")
                    .setFieldType("title")
                    .setQuery(query)
                    .setup();
            Document[] docs = querier.getRetrievedDocs(10);
            for (Document doc : docs) {
                System.out.println(doc.get("docid"));
                System.out.println(doc.get("title"));
                System.out.println(doc.get("url"));
                System.out.println();
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
