package dataindex;

import org.apache.lucene.document.Document;

public class QueryData {
    public static void main(String[] args) {
        String query = "dota";
        DataQuerier querier 
            = new DataQuerier()
                .setIndexDirectory("../data/index/")
                .setQuery(query);
        Document[] docs = querier.getRetrievedDocs(10);
        for (Document doc : docs) {
            System.out.println(doc.get("title"));
            System.out.println(doc.get("url"));
        }
    }
}
