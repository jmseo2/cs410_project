package dataquery;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

public class DataQuerier {
    private String fieldType;
    private String queryStr;
    private String indexDir;
    private Term[] queryTerms;
    private IndexReader reader;
    private IndexSearcher searcher;

    public DataQuerier setIndexDirectory(String indexDirectory) {
        indexDir = indexDirectory;
        return this;
    }

    public DataQuerier setFieldType(String field) {
        fieldType = field;
        return this;
    }
    
    public DataQuerier setQuery(String queryString) {
        queryStr = queryString;
        String[] tokens = queryStr.split(" ");
        queryTerms = new Term[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            queryTerms[i] = new Term(fieldType, tokens[i]);
        }
        return this;
    }
    
    public DataQuerier setup() {
        try {
            // open index
            File path = new File(indexDir);
            Directory dir = FSDirectory.open(path.toPath());
            reader = DirectoryReader.open(dir);
            searcher = new IndexSearcher(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    private ScoreDoc [] getRetrievedScoreDocs(Term [] terms, int hitsPerPage) {
        ScoreDoc [] scoreDocs = null;
        try {
            BooleanQuery query = new BooleanQuery();
            for (int i = 0; i < terms.length; i++) {
                TermQuery tq = new TermQuery(terms[i]);
                query.add(tq, BooleanClause.Occur.SHOULD);
            }

            TopDocs tops = searcher.search(query, hitsPerPage);
            scoreDocs = tops.scoreDocs;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scoreDocs;
    }
    
    public Document [] convertScoreDocsToDocs(ScoreDoc [] scoreDocs) {
        Document [] resDocs = new Document[scoreDocs.length];
        try {
            for (int i = 0; i < resDocs.length; i++) {
                int docid = scoreDocs[i].doc;
                resDocs[i] = searcher.doc(docid);
            }  
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resDocs;
    }
    
    // User can search by game similarities or general search
    public Document [] getRetrievedDocs(int hitsPerPage) {
        if (fieldType.equals("title")) {
            ScoreDoc [] titleScoreDocs = getRetrievedScoreDocs(queryTerms, 1);
            if (titleScoreDocs.length == 0) {
                return new Document[0];
            }
            Document [] titleDocs = convertScoreDocsToDocs(titleScoreDocs);
            System.out.println("[INFO] Retrieved doc's title: " + titleDocs[0].get("title"));
            
            try {
                // open index
                File path = new File(indexDir);
                Directory dir = FSDirectory.open(path.toPath());
                IndexReader reader = DirectoryReader.open(dir);
                MoreLikeThis mlt = new MoreLikeThis(reader);
                mlt.setAnalyzer(new StandardAnalyzer());
                mlt.setFieldNames(new String [] {"title", "body"});
                int docid = titleScoreDocs[0].doc;
                Query query = mlt.like(docid);
                TopDocs similarDocs = searcher.search(query, hitsPerPage);
                ScoreDoc [] bodyScoreDocs = similarDocs.scoreDocs;
                Document [] bodyDocs = convertScoreDocsToDocs(bodyScoreDocs);
                return bodyDocs;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return convertScoreDocsToDocs(getRetrievedScoreDocs(queryTerms, hitsPerPage));
    }
}