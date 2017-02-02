package com.mode.cron;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mode.model.Tweet;
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
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Controller
public class AcquireTweetsCron {

    @Autowired
    Environment environment;

    @GetMapping("/")
    public @ResponseBody List<String> tweetTest() {
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(10);
        BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(10);

        Location.Coordinate swUSBoundingBox = new Location.Coordinate(-126.0,25.6);
        Location.Coordinate neUSBoundingBox = new Location.Coordinate(-66.3,49.7);
        Location usBoundingBox = new Location(swUSBoundingBox, neUSBoundingBox);

        Client twitterClient = getTwitterClient(msgQueue, eventQueue, usBoundingBox);

        int count = 0;
        List<String> output = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        while (!twitterClient.isDone() && count < 50) {
            String msg = null;
            try {
                msg = msgQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            Tweet tweet = gson.fromJson(msg, Tweet.class);
            output.add(tweet.getText());
        }
        return output;
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
