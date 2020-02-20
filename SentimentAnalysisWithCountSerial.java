/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package com.mycompany.twitter.sentiments;
// Uses Naive Bayes algorithm for sentiment classification  -Serial

import java.sql.*;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.doccat.DoccatFactory ;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters ;

import opennlp.tools.util.*;
import opennlp.tools.ml.naivebayes.*; // NaiveBayesTrainer class

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author milind
 */
public class SentimentAnalysisWithCountSerial {

    
Connection con;
PreparedStatement ps;
ResultSet rs;
            
    DoccatModel model;
    static int positive = 0;
    static int negative = 0;

       
     int totalLines = 0; 
     static int sadness =1 ,worry=2,hate=3,love=4,fun=5,surprise=6,enthusiasm=7,neutral=8,happiness=9,empty=10;

    static SentimentAnalysisWithCountSerial twitterCategorizer ;
    Vector v1 ; 
    long trainEndTime = 0;
    
    SentimentAnalysisWithCountSerial()
    {
        try
        { 
            Class.forName("org.gjt.mm.mysql.Driver");
        
            con = DriverManager.getConnection("jdbc:mysql://localhost/NaiveSentimentDB", "root", "");
            ps = con.prepareStatement("INSERT INTO  analysis(NoOfTweets ,RunningTime ,dDateOfRun ,OptimizationType) VALUES (?,  ?,  ?,  ?)");
     
        }
        catch(Exception e)
        {
            System.out.println(e) ;
        }
    }
    public static void main(String[] args) throws IOException, TwitterException {
        String line = "";
        twitterCategorizer = new SentimentAnalysisWithCountSerial();
        twitterCategorizer.trainModel();

        /*ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("9CH7xPBj3Mv5pYibs8Dfwcf1h")
                .setOAuthConsumerSecret("8cI8vRVDLm86qlQlo2tgbRjYrAFszp2BYL92AvJyDkkaSyKh8h")
                .setOAuthAccessToken("777167446688096256-1xzXN7N8Hb6JeFY1ZCffyUNL78uOnve")
                .setOAuthAccessTokenSecret("pHgk7Xfr8H4MMOzUT0WinXxkUckxk1dFxizzKeU9JPuWS");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
       // Query query = new Query("udta punjab");
Query query = new Query("demonetisation");

        QueryResult result = twitter.search(query);
        int result1 = 0;
        for (Status status : result.getTweets()) {
            result1 = twitterCategorizer.classifyNewTweet(status.getText());
            if (result1 == 1) {
                positive++;
            } else {
                negative++;
            }
        }
*/
        /*int result1 =0 ;
        FileReader fr = new FileReader("text_emotion.csv");
        BufferedReader br = new BufferedReader(fr);
//        String line = "";

        while((line = br.readLine()) != null)
        {
			 result1 = twitterCategorizer.classifyNewTweet(line);
			            if(result1 ==1)  sadness++ ;
			            else if(result1 == 2) worry++;
			            else if(result1 == 3) hate++;
			            else if(result1 == 4) love++;
			            else if(result1 == 5) fun++;
			            else if(result1 == 6) surprise++;
			            else if(result1 == 7) enthusiasm++;
			            else if(result1 == 8) neutral++;
			            else if(result1 == 9) happiness++;
			            else if(result1 == 10) empty++;
            }
        */
        
   
        
        
         long testprev = System.nanoTime();
        System.out.println("Testing started at : " + testprev)        ;
                
        twitterCategorizer.testModel();

         long endTime = (System.nanoTime() - testprev) ;
         System.out.println("Total execution time taken ,for testing ( in nano.seconds : "+endTime);
         
         twitterCategorizer.saveToDB(endTime) ;

        BufferedWriter bw = new BufferedWriter(new FileWriter("resultsSerial.csv"));
        bw.write("Empty Tweets," + empty);
        bw.newLine();

        bw.write("Sadness Tweets," + sadness);
        bw.newLine();

        bw.write("Worry Tweets," + worry);
        bw.newLine();
        bw.write("Love Tweets," + love);
        bw.newLine();
        bw.write("Fun Tweets," + fun);
        bw.newLine();
        bw.write("Hate Tweets," + hate);
        bw.newLine();
        bw.write("Enthusiasm Tweets," + enthusiasm);
        bw.newLine();
        bw.write("Surprise Tweets," + surprise);
        bw.newLine();
        bw.write("Neutral Tweets," + neutral);
        bw.newLine();
        bw.write("happiness Tweets," + happiness);
        bw.newLine();
        bw.close();
    }

    public void testModel( ) 
    {

        try
        {
                 // Load tweets from files in memory
        FileReader fr = new FileReader("text_emotion.csv");
        BufferedReader br = new BufferedReader(fr);
     
        int result1 =0 ;

        String line = "";

       while((line = br.readLine()) != null)
        {
	//result1 = twitterCategorizer.classifyNewTweet(line);
               totalLines++;
                result1 = classifyNewTweet(line);
	if(result1 ==1)  sadness++ ;
	else if(result1 == 2) worry++;
	else if(result1 == 3) hate++;
	else if(result1 == 4) love++;
	else if(result1 == 5) fun++;
	else if(result1 == 6) surprise++;
	else if(result1 == 7) enthusiasm++;
	else if(result1 == 8) neutral++;
	else if(result1 == 9) happiness++;
	else if(result1 == 10) empty++;

            }
        }
        catch(Exception e)
        {
            System.out.println("testing error :" +e) ;
        }

    }
    
    
    /**
     * Saves data to DB
     */
    public void saveToDB(long  totalTime)
    {
        try
        {
            //total testing tweets
            java.util.Date d = new java.util.Date();
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd") ;
            String dt = fmt.format(d) ;
            ps.setInt(1,totalLines) ;
            ps.setLong(2,(totalTime/1000000) +trainEndTime);
        
            ps.setDate(3,java.sql.Date.valueOf(dt));
            ps.setString(4,"Serial");
            ps.executeUpdate() ;
            JOptionPane.showMessageDialog(new JFrame() ,"Running time data has been saved to DB") ;
            
        }
        catch(Exception e)
        {
            System.out.println("Error while saving to db :" +e) ;
        }

        
    }
    public void trainModel() {
        InputStream dataIn = null;
        try {
            //dataIn = new FileInputStream("training_data1.txt");

            MarkableFileInputStreamFactory factory = new MarkableFileInputStreamFactory(new File("training_data1.txt")) ;

            //ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream lineStream = new PlainTextByLineStream(factory, "UTF-8");
            ObjectStream sampleStream = new DocumentSampleStream(lineStream);
            // Specifies the minimum number of times a feature must be seen
            int cutoff = 7;
            int trainingIterations = 30;

          TrainingParameters params = new TrainingParameters();
          params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(trainingIterations));
          params.put(TrainingParameters.CUTOFF_PARAM,Integer.toString(7));
          params.put(TrainingParameters.ALGORITHM_PARAM,NaiveBayesTrainer.NAIVE_BAYES_VALUE);
               // params.put(TrainingParameters.THREADS_PARAM,Integer.toString(5));

                long prev = System.currentTimeMillis();
                model = DocumentCategorizerME.train("en", sampleStream, params,new DoccatFactory());
                
                trainEndTime = (System.currentTimeMillis() - prev) ; 
                System.out.println("Total execution time taken ,for training : "+trainEndTime);
                
                
                //  model = DocumentCategorizerME.train("en", sampleStream, cutoff,  trainingIterations);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dataIn != null) {
                try {
                    dataIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int classifyNewTweet(String tweet) throws IOException {


        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
        double[] outcomes = myCategorizer.categorize(tweet);
        String category = myCategorizer.getBestCategory(outcomes);

        System.out.print("-----------------------------------------------------\nTWEET :" + tweet + " ===> ");
        if (category.equalsIgnoreCase("1")) {
            System.out.println(" Sadness ");
            return 1;
        }
        else  if(category.equalsIgnoreCase("2")){
            System.out.println(" Worry ");
            return 2;
        }
        else  if(category.equalsIgnoreCase("3")){
		            System.out.println(" Hate ");
		            return 3;
        }
        else  if(category.equalsIgnoreCase("4")){
		            System.out.println(" Love ");
		            return 4;
        }
        else  if(category.equalsIgnoreCase("5")){
		            System.out.println(" Fun ");
		            return 5;
        }
        else  if(category.equalsIgnoreCase("6")){
		            System.out.println(" Surprise ");
		            return 6;
        }
        else  if(category.equalsIgnoreCase("7")){
		            System.out.println(" Enthusiasm ");
		            return 7;
        }
        else  if(category.equalsIgnoreCase("8")){
		            System.out.println(" Neutral ");
		            return 8;
        }
        else  if(category.equalsIgnoreCase("9")){
		            System.out.println(" Happiness ");
		            return 9;
        }
        else  if(category.equalsIgnoreCase("10")){
		            System.out.println(" Empty ");
		            return 10;
        }
        return 0;
    }
}

