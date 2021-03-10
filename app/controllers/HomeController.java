package controllers;

import play.mvc.*;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.*;
import akka.pattern.Patterns;
import akka.util.Timeout;
import actors.FileAnalysisActor;
import messages.FileAnalysisMessage;
import messages.FileProcessedMessage;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import scala.concurrent.Await;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

import models.TweetApi;
import models.Tweet;
import models.Features;


/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    public static class StaticPath {

        public static List<String> tweets = new ArrayList<>();
        public static String path = "data/test-tweets/smol";
        public static String output_file = "new_with_offset";
    }

    // Start timer for tracking efficiency
    static long startTime = System.currentTimeMillis();


    // Entry point for /tweets
    public Result index()  throws Exception{
        return ok(akkaActorApi());
    }

    public String akkaActorApi()  throws Exception{

        // Get jsonl files
        try (Stream<Path> paths = Files.walk(Paths.get(StaticPath.path),2)) {
            paths.map(Path::toString).filter(f -> f.endsWith(".jsonl"))
                    .forEach(t -> {
                        try {
                            parseEvent(t);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) { e.printStackTrace(); }

        StringBuilder resultString = new StringBuilder();

        appendStringResult(StaticPath.tweets, resultString);

        return resultString.toString();

    }


	private static String parseEvent(String s) throws Exception {
	    StringBuilder resultString = new StringBuilder();

        System.out.println("Parsing " + s);

        // Create actorSystem
        ActorSystem akkaSystem = ActorSystem.create("akkaSystem");

        // Create the first actor based on the specified class
        Props props = Props.create(FileAnalysisActor.class);
        ActorRef coordinator = akkaSystem.actorOf(props);

        // Create a message including the file path
        FileAnalysisMessage msg = new FileAnalysisMessage(s);

        // Process the results
        final ExecutionContext ec = akkaSystem.dispatcher();

        // Send a message to start processing the file.
        // This is a synchronous call using 'ask' with a timeout.
        Timeout timeout = new Timeout(300, TimeUnit.SECONDS); // 50 times out with embeddings
        Future<Object> future = Patterns.ask(coordinator, msg, timeout);

        FileProcessedMessage result =  (FileProcessedMessage) Await.result(future, timeout.duration());

        printResults(result);

        appendStringResult(StaticPath.tweets, resultString);

        printTimer(startTime);

        return resultString.toString();

    }

    private static void appendStringResult(List<String> intList, StringBuilder resultString) {
    		intList.forEach(ele->{
            	resultString.append(ele + "\n");
            });
    }


	private static void printResults(FileProcessedMessage result) {
		result.getHMap().forEach(outputs -> {
			outputs.getTweets().forEach(output -> {
				String tweet = "";
				try {
					tweet = adapterTweet(output);
				} catch (JsonProcessingException e1) {
					e1.printStackTrace();
				}

				StaticPath.tweets.add(tweet);
			});

		});
	}


	private static String adapterTweet(Tweet output) throws JsonProcessingException {
		 TweetApi tweet = new TweetApi();
		 if(output != null) {
			 if(output.getId() != null) {
				 tweet.setTweet_id(output.getId());
			 }

			 if(output.getText() != null) {
				 tweet.setTweet_text(output.getText());
			 }
			 Features feature = new Features();

			 if(output.getFeatures() != null) {
				 if(output.getFeatures().get("numb_of_urls") != null ) {
					 feature.setNumb_of_hashtags(output.getFeatures().get("numb_of_urls"));
				 }
				 if(output.getFeatures().get("numb_of_hashtags") != null) {
					 feature.setNumb_of_hashtags(output.getFeatures().get("numb_of_hashtags"));
				 }
                 if(output.getFeatures().get("numb_of_personal_pronouns") != null) {
        			 feature.setNumb_of_personal_pronouns(output.getFeatures().get("numb_of_personal_pronouns"));
				 }
                 if(output.getFeatures().get("numb_of_present_tenses") != null) {
        			 feature.setNumb_of_present_tenses(output.getFeatures().get("numb_of_present_tenses"));
                 }
			 }

			 tweet.setFeatures(feature);

             if(output.getDimensions() != null && output.getDimensions().length !=0) {
				 tweet.setEmbeddings(convertTableto(output.getDimensions()));
			 }

	    	 ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
	    	 return ow.writeValueAsString(tweet);

		 }else {
			 return null;
		 }

	}

	private static String convertTableto(float[] dimensions) {
		StringBuilder convertDim = new StringBuilder();
		convertDim.append("{");

		int a = 0;
		for(float dimension : dimensions) {
			convertDim.append(dimension);
			convertDim.append(",");
		}
		convertDim.append("}");
		return convertDim.toString();
	}

        public Result explore() {
                return ok(views.html.explore.render());
        }

        public Result tutorial() {
                return ok(views.html.tutorial.render());
        }

        private static void printTimer(long startTime) {
            // Outputs the elapsed time to console
            long elapsedTime = System.currentTimeMillis() - startTime;
            long elapsedSeconds = elapsedTime / 1000;
            long elapsedMinutes = elapsedSeconds / 60;
            System.out.println("Time elapsed: " + elapsedMinutes + " minutes");
            System.out.println(elapsedSeconds + " seconds");
        }

}
