package sentiment;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.LMClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;

public class SentimentAnalyzer {
    private File trainDataDir;
    private DynamicLMClassifier<NGramProcessLM> classifier;
    private LMClassifier classifierSaved;
    private String [] labels;
    private int nGramLength;
    public SentimentAnalyzer() {
        nGramLength = 6;
    }
    
    public SentimentAnalyzer setTrainDataDirectory(String dir) {
        trainDataDir = new File(dir);
        labels = trainDataDir.list();
        return this;
    }
    
    public SentimentAnalyzer setNGramLength(int len) {
        nGramLength = len;
        return this;
    }
    
    public void saveModel(String modelDir) throws IOException {
        System.out.println("[INFO] Saving model...");
        File file = new File(modelDir);
        AbstractExternalizable.compileTo(classifier, file);
        System.out.println("[INFO] Saving model done...");
    }
    
    public void loadModel(String modelDir) throws IOException, ClassNotFoundException{
        System.out.println("[INFO] Loading model...");
        File file = new File(modelDir);
        classifierSaved = (LMClassifier)AbstractExternalizable.readObject(file);
        System.out.println("[INFO] Loading model done...");
    }
    
    public void train() throws IOException {
        System.out.println("[INFO] Training classifier...");
        classifier = DynamicLMClassifier.createNGramProcess(labels, nGramLength);
        for (String label : labels) {
            Classification classification = new Classification(label);
            File labelDir = new File(trainDataDir, label);
            File [] trainDataFiles = labelDir.listFiles();
            int numTrainFiles = 0;
            for (File trainDataFile : trainDataFiles) {
                if (!isTrainingFile(trainDataFile))
                    continue;
                String review = Files.readFromFile(trainDataFile, "ISO-8859-1");
                Classified<CharSequence> classified = new Classified<CharSequence>(review, classification);
                classifier.handle(classified);
                numTrainFiles++;
            }
            System.out.println("[INFO] Number of " + label + " training data: " + numTrainFiles);
        }
        System.out.println("[INFO] Training classifier done...");
    } 
    
    public void test() throws IOException {
        int numCorrect = 0;
        int numTestFiles = 0;
        for (String label : labels) {
            File file = new File(trainDataDir, label);
            File [] testDataFiles = file.listFiles();
            for (File testDataFile : testDataFiles) {
                if (isTrainingFile(testDataFile))
                    continue;
                String review = Files.readFromFile(testDataFile, "ISO-8859-1");
                ConditionalClassification classification;
                if (classifier != null) {
                    classification = classifier.classify(review);
                } else {
                    classification = classifierSaved.classify(review);
                }
                String resultLabel = classification.bestCategory();
                if (resultLabel.equals(label))
                    numCorrect++;
                numTestFiles++;
            }
        }
        System.out.println("[INFO] Total number of tests: " + numTestFiles);
        System.out.println("[INFO] Number of instances labeled correctly: " + numCorrect);
        System.out.println("[INFO] Accuracy: " + (double)numCorrect / numTestFiles);
    }
    
    public boolean isTrainingFile(File file) {
        return file.getName().charAt(2) != '9';
    }
    
    // conditional probability of the label given the input
    public double probability(String input, String label) {
        ConditionalClassification classification;
        if (classifier != null) {
            classification = classifier.classify(input);
        } else {
            classification = classifierSaved.classify(input);
        }
        return classification.conditionalProbability(label);
    }
}