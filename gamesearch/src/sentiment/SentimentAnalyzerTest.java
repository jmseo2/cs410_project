package sentiment;

public class SentimentAnalyzerTest {
    public static void main (String []args) {
        SentimentAnalyzer analyzer 
            = new SentimentAnalyzer()
                .setTrainDataDirectory("../data/sentiment/txt_sentoken/")
                .setNGramLength(8);
        try {
            //analyzer.train();
            //analyzer.test();
            //analyzer.saveModel("../data/sentiment/models/sentiment.model");
            analyzer.loadModel("../data/sentiment/models/sentiment.model");
            analyzer.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}