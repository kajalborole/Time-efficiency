/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package com.mycompany.twitter.sentiments;
// Uses Naive Bayes algorithm for sentiment classification  -Serial
import java.sql.*;
import java.io.BufferedWriter;
//import java.io.FileInputStream;
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
//import twitter4j.Query;
//import twitter4j.QueryResult;
//import twitter4j.Status;
//import twitter4j.Twitter;
import twitter4j.TwitterException;
//import twitter4j.TwitterFactory;
//import twitter4j.conf.ConfigurationBuilder;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
//import java.util.Date;
import java.util.Vector;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
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

       int posCat[] = {4,5,6,7,9} ;
     int negCat[] = { 1,2,3} ; // for testing leave 10 (empty) and 8 -Neutral categories 
     
    String categoryName = ""; // positive and negative 
 
    int tp =0 ,tn=0,fp=0,fn=0 ;
    double precision = 0.0,recall =0.0, accuracy = 0.0 ;
        
    static SentimentAnalysisWithCountSerial twitterCategorizer ;
    Vector v1,trainTweet,trainScore ; 
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
        
   
        
        
         long starttesttime = System.currentTimeMillis();
        System.out.println("Testing started at : " + starttesttime);
                
        twitterCategorizer.testModel();
        long endtesttime= System.currentTimeMillis();
        System.out.println("Testing end at:"+endtesttime);  
         long endTime = (endtesttime - starttesttime) ;
         System.out.println("Total execution time taken ,for testing ( in ms : "+endTime);
         
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
            
        String line = "";
   
        //load tweets from training file - trainingDataThou.txt
        
        FileReader fr = new FileReader("train.txt") ;
        BufferedReader br = new BufferedReader(fr) ;
        
        String lineT = ""; 
         trainTweet = new Vector(1000) ;
         trainScore = new Vector(1000) ;
         
        while((lineT=br.readLine()) != null)
        {
                 trainTweet.add(lineT.substring(lineT.indexOf("\t")+1)) ; 
                 trainScore.add(lineT.substring(0,lineT.indexOf("\t"))) ; 
                  
        }
        System.out.println("Total training tweets : " + trainTweet.size()) ; 
            
        int scoreActual=0; 
        String catActual = "" ,machineActual = "";  
                 // Load tweets from files in memory
      //  FileReader fr = new FileReader("text_emotion.csv");
         fr = new FileReader("test.txt");
         br = new BufferedReader(fr);
     
        int result1 =0 ;

       //String line = "";

       while((line = br.readLine()) != null)
        {
	//result1 = twitterCategorizer.classifyNewTweet(line);
               totalLines++;
                result1 = classifyNewTweet(line);
                
                 scoreActual = search(line) ; 
                System.out.println("ScoreActual :"  + scoreActual) ;
                //result1 - Tweet score given by machine i.e machineActual 
                
                //now find categories of both 
                //if(scoreActual != 10 && scoreActual != 8)
                if(scoreActual != 0)
                {
                catActual = findCategory(scoreActual) ;
                machineActual = findCategory(result1) ;
                
                System.out.println("cat actual " + catActual +" machineActual = " + machineActual) ;
                
                if(catActual.equals("yes") && machineActual.equals("yes"))
                    tp++ ;
                else  if(catActual.equals("yes") && machineActual.equals("no"))
                    fn++ ;
                else  if(catActual.equals("no") && machineActual.equals("yes"))
                    fp++ ;
                else  if(catActual.equals("no") && machineActual.equals("no"))
                    tn++ ;
                } 
                
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
        System.out.println("tp="+tp+"\ntn="+tn);
        System.out.println("fp="+fp+"\nfn="+fn);
                precision = (double)tp /(double) (tp+fp) ;
                precision = precision * 100 ;
                
                recall = (double)tp / (double)( tp + fn) ;
                recall = recall * 100 ;
                
                accuracy = (double)(tp + tn) /(double) ( tp + tn + fp+fn) ;
                accuracy = accuracy * 100; 
                
                double F1score = 2 * precision * recall / ( precision + recall) ;
                
                System.out.println("Precision is : " + precision) ;
                System.out.println("Recall is : " + recall) ;
                System.out.println("Accuracy is : " + accuracy) ;
                System.out.println("F1-score : " + F1score) ;
                  
        }
        catch(Exception e)
        {
            System.out.println("testing error :" +e) ;
        }

    }
    
     /**
     * Searches a tweet from training file and returns its actual score
     */
    public int search(String line)
    {
        String tweet = ""; 
        int scoreActual = 0; 
        for(int i=0;i<trainTweet.size();i++)
        {
            tweet = (String)trainTweet.elementAt(i) ;
            tweet = tweet.trim() ;
            line = line.trim() ;
            if(line.equalsIgnoreCase(tweet))        
            {
                scoreActual = Integer.parseInt((String)trainScore.elementAt(i)) ;
                break ;
            }
        }
        return scoreActual ; 
    }
    /**
      * Finds whether a tweet belongs to positive cat or negative cat 
      */
     public String findCategory(int score)
     {
        //  Arrays.sort(negCat);
          int indx = Arrays.binarySearch(negCat,score)  ; 
          if(indx > 0 )
              categoryName = "no"   ;
          else
              categoryName = "yes" ; 
          return categoryName ; 
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
            ps.setLong(2,(totalTime) +trainEndTime);
        
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

            MarkableFileInputStreamFactory factory = new MarkableFileInputStreamFactory(new File("train.txt")) ; // training_data1.txt

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

