package com.fumitti.vfdlib.Reader;

import com.fumitti.vfdlib.VFDNewsPanel;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by fumitti on 2015/05/21.
 */
public class twitterUN_NERV implements ReaderInterface{

    private final Twitter twitter;

    public twitterUN_NERV(){
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("")
                .setOAuthConsumerSecret("")
                .setOAuthAccessToken("")
                .setOAuthAccessTokenSecret("");
        Configuration build = cb.build();
        TwitterFactory tf = new TwitterFactory(build);
        twitter = tf.getInstance();
        // TwitterStreamを生成
        TwitterStreamFactory factory = new TwitterStreamFactory(build);
        TwitterStream twitterStream = factory.getInstance();
        twitterStream.addListener(new UserStreamAdapter(){
            @Override
            public void onStatus(Status tweet) {
                synchronized (this) {
                    if(tweet.getUser().getId()!=116548789L){
                    }else if(tweet.getText().contains("中の人")){
                    }else if(!tweet.isRetweet()) {
                        System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                        if(tweet.getText().contains("緊急地震速報"))
                            VFDNewsPanel.prioQue.add(tweet.getText().replace('\n', ' ').split("http")[0]);
                        else
                            VFDNewsPanel.newsQue.add(tweet.getText().replace('\n', ' ').split("http")[0]);
                    }
                }
            }
            @Override
            public void onException(Exception ex)
            {
            }
        });

        // 取得をスタート
        long[] list = {116548789L};
		FilterQuery query = new FilterQuery(list);
        String[] track = {"from:un_nerv","exclude:retweets"};
        //FilterQuery query = new FilterQuery();
        query.track(track);
        query.follow(list);
        twitterStream.filter(query);
        //		twitterStream.user();
        //		twitterStream.sample();
    }

    public void getinfo(){
        try {
            Query query = new Query("from:un_nerv");
            QueryResult result;
                result = twitter.search(query);
                List<Status> tweets = result.getTweets();
                Collections.reverse(tweets);
                for (Status tweet : tweets) {
                    System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
                    VFDNewsPanel.newsQue.add(tweet.getText().replace('\n', ' ').split("http")[0]);
                }
        } catch (Exception te) {
            te.printStackTrace();
            System.out.println("Failed to search tweets: " + te.getMessage());
        }
    }
}
