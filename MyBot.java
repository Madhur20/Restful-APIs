
//importing all the required files
import org.jibble.pircbot.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MyBot extends PircBot {
    
    public MyBot() {
        this.setName("MadBot");
    }
    
    //
    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?units=imperial&";
    private static final String APIkey = "64c9267beffbf96ca0f9d7472729a361";
    private static final String YOUTUBE_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String youtubeAPIkey = "AIzaSyBKwnlCUXjkdAoEg_jKxsVWLUosmxw4Xhc";
    
   //when any new bot or user joins
    protected void onJoin(String channel, String sender, String login, String hostname) {

    	sendMessage(channel, sender + " Welcome!");
    	sendMessage(channel, sender + " Enter the keyword \"weather\" and city name or zipcode to get current temperature of that place.");
    	sendMessage(channel, sender + " Enter the keyword \"time\" to get current time.");
    	sendMessage(channel, sender + " Enter the keyword \"twitter\" to get current top 10 worldwide tags.");
    	sendMessage(channel, sender + " Enter the keyword \"youtube\" and query to get first video found on youtube using title.");
    	sendMessage(channel, sender + " Enter the keyword \"disconnect\" to disconnect the BOT.");

    	
    }

    boolean FLAG = true;
    
    //method to call functions relative to the input
    public void onMessage(String channel, String sender,
                       String login, String hostname, String message) {
    	String[] msgWords = message.split("\\W+");
    	FLAG = true;
		for(int i = 0; i < msgWords.length; i++)
    	{
	    	if (msgWords[i].equalsIgnoreCase("time")) {
	            String time = new java.util.Date().toString();
	            sendMessage(channel, sender + ": The current time is " + time);
	        }
	    	else if (msgWords[i].equalsIgnoreCase("twitter")) {
				try {
					TwitterTrendingTags(channel, sender);
				} catch (TwitterException e) {
					e.printStackTrace();
				}
	    	}
	    	else if (msgWords[i].equalsIgnoreCase("youtube")) {
				try {
					String name = message.replace(" ", "%20");
					getYoutube(channel, sender, name);
				} catch (Exception e) {
					e.printStackTrace();
				}
	    	}
	    	if(msgWords[i].equalsIgnoreCase("weather")){
        		for(int m = 0;  m < msgWords.length-1; m++ ) {
        				String name = msgWords[m]+ "%20" +msgWords[m+1];
        			try {
						getWeather(channel, sender, name);
					} catch (Exception e) {
						e.printStackTrace();
					}
        			
        			}
  
	        }
	        if (msgWords[i].equalsIgnoreCase("weather") && FLAG){
	        	for(int j = 0;  j < msgWords.length; j++ ) {
	        		if(isNumeric(msgWords[j])) {
	        			try {
	        				int zip = Integer.parseInt(msgWords[j]);
	    					getWeather(channel, sender, zip);
	    				} catch (Exception e) {
	    					e.printStackTrace();
	    				}
	        		}
	        		else {
				        try {
							getWeather(channel, sender, msgWords[j]);
						} catch (Exception e) {
							e.printStackTrace();
						}
				     }
	        		
	        		}
	        }
	        if(message.equalsIgnoreCase("disconnect")){
	        	sendMessage(channel, sender + " The bot has now disconnected. Thank You.");
	        	dispose();
	        }
	        
        }
    }
    
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    
    //get weather method through zipcode
    void getWeather(String channel, String sender, int k) throws Exception {
    	String POSTS_URL = WEATHER_URL + "zip=" + k + "&appid=" + APIkey;
    	HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(POSTS_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

//                sendMessage(channel, sender + " " + response.body());
        
        Object obj = new JSONParser().parse(response.body()); 
        
//        typecasting obj to JSONObject 
        JSONObject jo = (JSONObject) obj; 
        JSONObject main = (JSONObject) jo.get("main");
        double temp = (double) main.get("temp");
        double feel = (double) main.get("feels_like");
        String city = (String) jo.get("name");
        double min_temp_d = 786, max_temp_d = 786;
        long min_temp_l = 786, max_temp_l = 786;
        String min_temp, max_temp;
        try {
        	min_temp_l = (long) main.get("temp_min");
        	min_temp = Long.toString(min_temp_l);
        } catch (Exception e) {
        	min_temp_d = (double) main.get("temp_min");
        	min_temp = Double.toString(min_temp_d);
        }
        try {
        	max_temp_l = (long) main.get("temp_max");
        	max_temp = Long.toString(max_temp_l);
        } catch (Exception e) {
        	max_temp_d = (double) main.get("temp_max");
        	max_temp = Double.toString(max_temp_d);
        }
      //printing out the weather
        sendMessage(channel, sender + ": Current temperature in " + city + " is " + temp + " ºF, and feels like is " + feel + " ºF with "
        		+ "Maximum temp of " + max_temp + "ºF and Minimum temp of " + min_temp + "ºF.\n");
    }
    
    //get weather method through city name
    void getWeather(String channel, String sender, String name) throws Exception {
    	String POSTS_URL = WEATHER_URL + "q=" + name + "&appid=" + APIkey;
    	HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("accept", "application/json")
                .uri(URI.create(POSTS_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        
        Object obj = new JSONParser().parse(response.body()); 
        
//        typecasting obj to JSONObject
        JSONObject jo = (JSONObject) obj; 
        JSONObject main = (JSONObject) jo.get("main");
        double temp = (double) main.get("temp");
        double feel = (double) main.get("feels_like");
        String city = (String) jo.get("name");
        double min_temp_d = 786, max_temp_d = 786;
        long min_temp_l = 786, max_temp_l = 786;
        String min_temp, max_temp;
        try {
        	min_temp_l = (long) main.get("temp_min");
        	min_temp = Long.toString(min_temp_l);
        } catch (Exception e) {
        	min_temp_d = (double) main.get("temp_min");
        	min_temp = Double.toString(min_temp_d);
        }
        try {
        	max_temp_l = (long) main.get("temp_max");
        	max_temp = Long.toString(max_temp_l);
        } catch (Exception e) {
        	max_temp_d = (double) main.get("temp_max");
        	max_temp = Double.toString(max_temp_d);
        }
     //printing out the weather 
        sendMessage(channel, sender + ": Current temperature in " + city + " is " + temp + " ºF, and feels like is " + feel + " ºF with "
        		+ "Maximum temp of " + max_temp + "ºF and Minimum temp of " + min_temp + "ºF.\n");

        FLAG = false;
    }
    
    //method to display current top 10 twitter tags
    void TwitterTrendingTags(String channel, String sender) throws TwitterException {
        ConfigurationBuilder c_b = new ConfigurationBuilder();
        c_b.setDebugEnabled(true)
                .setOAuthConsumerKey("WR8aOd20pNmRv4JfHLOVP27MF")
                .setOAuthConsumerSecret("St372xHEk3JPqGOdHyIr0y3N2q0xsSRU4tduiw8jubh27kdnrP")
                .setOAuthAccessToken("1312377650057371648-cwynHBJn5pfJ4DVhcgGOqrhTaOA1s3")
                .setOAuthAccessTokenSecret("qEYJEpscx1GNpUAzobx7fArm6Szt83xbKXiOrYUkWuc5U");
        TwitterFactory t_f = new TwitterFactory(c_b.build());
        Twitter twitter = t_f.getInstance();
        Trends trends = twitter.getPlaceTrends(1);
        sendMessage(channel, sender + ": Top 10 worldwide current trending tags are: \n");
        int count = 0;
        for (Trend trend : trends.getTrends()) {
            if (count < 10) {
            	sendMessage(channel,trend.getName() + "\n");
                count++;
            }
        }
    }
    
    //method to get first youtube video found as per the user message
    void getYoutube(String channel, String sender, String name) throws Exception{
    	
    	String YoutubeURL = YOUTUBE_URL + "?part=snippet&maxResults=1&key=" + youtubeAPIkey + "&type=video&q=" + name;
    	HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .header("accept", "application/json")
                .uri(URI.create(YoutubeURL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        Object obj = new JSONParser().parse(response.body()); 
        
//      typecasting obj to JSONObject
        JSONObject jo = (JSONObject) obj;

        JSONArray items= (JSONArray)jo.get("items");
        JSONObject index = (JSONObject) items.get(0);
        String videoID = (String) ((JSONObject)index.get("id")).get("videoId");
      
       String url = "https://www.youtube.com/watch?v=" + videoID;
       System.out.println(response);
       sendMessage(channel, sender + " " + url);
      
      
    }
    
}