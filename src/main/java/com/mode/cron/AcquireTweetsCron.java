package com.mode.cron;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mode.model.*;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Controller
public class AcquireTweetsCron {

    @Autowired
    Environment environment;

    @Autowired
    SampleRepository sampleRepository;

//    @Scheduled(fixedDelay=1000 * 60)
    public
    @ResponseBody
    void tweetTest() {
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(50);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(10);

        Location.Coordinate swUSBoundingBox = new Location.Coordinate(-126.0, 25.6);
        Location.Coordinate neUSBoundingBox = new Location.Coordinate(-66.3, 49.7);
        Location usBoundingBox = new Location(swUSBoundingBox, neUSBoundingBox);

        Client twitterClient = getTwitterClient(msgQueue, eventQueue, usBoundingBox);

        int count = 0;
        List<Tweet> tweets = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        while (!twitterClient.isDone() && count < 50) {
            String msg = null;
            try {
                msg = msgQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            tweets.add(gson.fromJson(msg, Tweet.class));
        }

        SentimentRequest sentimentRequest = new SentimentRequest(
                tweets.toArray(new Tweet[]{}));

        RestTemplate restTemplate = new RestTemplateBuilder().build();
        ResponseEntity<String> raw = restTemplate.exchange(
                "http://www.sentiment140.com/api/bulkClassifyJson?appid=" +
                        environment.getProperty("SENTIMENT_EMAIL"),
                HttpMethod.POST,
                new HttpEntity<>(gson.toJson(sentimentRequest)),
                String.class);
        SentimentResponse sentimentResponse = gson.fromJson(raw.getBody(),SentimentResponse.class);
        Sample toSave = Sample.builder()
                .addedOn(new Date())
                .polarityAverage(sentimentResponse.getAdjustedPolarityAverage())
                .build();
        sampleRepository.save(toSave);
        twitterClient.stop();
        System.out.println("Sentiment sample saved successfully sir.");
    }



    private Client getTwitterClient(BlockingQueue<String> msgQueue, BlockingQueue<Event> eventQueue, Location boundingBox) {
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        hosebirdEndpoint.locations(Collections.singletonList(boundingBox));
        Authentication hosebirdAuth = new OAuth1(
                environment.getProperty("TWITTER_CONSUMER_KEY"),
                environment.getProperty("TWITTER_CONSUMER_SECRET"),
                environment.getProperty("TWITTER_TOKEN"),
                environment.getProperty("TWITTER_TOKEN_SECRET"));

        ClientBuilder builder = new ClientBuilder()
                .name("Mode-L")
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue))
                .eventMessageQueue(eventQueue);

        Client twitterClient = builder.build();
        twitterClient.connect();
        return twitterClient;
    }
}
