package dataquery;


import net.sf.classifier4J.summariser.SimpleSummariser;
import org.apache.lucene.document.Document;
import util.Utility;
import java.util.*;
import java.io.*;

public class QueryData {
    public static Set<String> getTitles(String titlesDir) {
        Set<String> res = new HashSet<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(titlesDir));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                res.add(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public static String getClosestTitle(String query, Set<String> titles) {
        int bestDist = Integer.MAX_VALUE;
        String bestTitle = "";
        for (String title : titles) {
            int dist = Utility.editDistance(query, title);
            if (dist < bestDist) {
                bestDist = dist;
                bestTitle = title;
            }
        }
        return bestTitle;
    }
    
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Usage: java [query] [num-docs] [num-sentences] [index-directory] [titles-directory]");
            return;
        }
        String query = args[0];
        int numDocs = Integer.parseInt(args[1]);
        int numSentences = Integer.parseInt(args[2]);
        String indexDir = args[3];
        String titlesDir = args[4];
        SimpleSummariser summarizer = new SimpleSummariser();
        Set<String> titles = getTitles(titlesDir);
        
        // among the titles, find the one closest
        query = getClosestTitle(query + " review", titles);
        try {
            DataQuerier querier 
                = new DataQuerier()
                    .setIndexDirectory(indexDir)
                    .setFieldType("title")
                    .setQuery(query)
                    .setScoreThreshold(0.8f)
                    .setup();
            Document[] docs = querier.getRetrievedDocs(numDocs);
            for (Document doc : docs) {
                System.out.println(doc.get("title"));
                System.out.println(doc.get("url"));
                String review = Utility.getOnlyReviewText(doc.get("body"));
                String summarizedReview = summarizer.summarise(review, numSentences);
                System.out.println(summarizedReview);
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
