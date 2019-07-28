package twitterstreaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.Seconds
import twitter4j.conf.ConfigurationBuilder
import twitter4j.auth.OAuthAuthorization
import twitter4j.Status
import org.apache.spark.streaming.twitter.TwitterUtils

// to run with sbt cd in directory: sbt "run $TWITTER_CONSUMER_KEY $TWITTER_CONSUMER_SECRET $TWITTER_ACCESS_TOKEN $TWITTER_ACCESS_TOKEN_SECRET keyword"
// to run after sbt package cd in directory: spark-submit --class twitterstreaming target/scala-2.11/spark-streaming-twitter_2.11-1.0.jar $TWITTER_CONSUMER_KEY $TWITTER_CONSUMER_SECRET $TWITTER_ACCESS_TOKEN $TWITTER_ACCESS_TOKEN_SECRET keyword
object twitterstreaming extends App {
  /*def main(args: Array[String]) {*/
    if (args.length < 4) {
      System.err.println("Usage: TwitterData <ConsumerKey><ConsumerSecret><accessToken><accessTokenSecret>" +
        "[<filters>]")
      System.exit(1)
    }
    val appName = "TwitterData"
    val conf = new SparkConf()
    conf.setAppName(appName).setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(5))
    val Array(consumerKey, consumerSecret, accessToken, accessTokenSecret) = args.take(4)
    val filters = args.takeRight(args.length - 4)
    val cb = new ConfigurationBuilder
    cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerSecret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)
    val auth = new OAuthAuthorization(cb.build)
    val tweets = TwitterUtils.createStream(ssc, Some(auth), filters)
    
    val engTweets = tweets.filter(x => x.getLang() == "en")
	val statuses = engTweets.map(status => status.getText)
	val tweetwords = statuses.flatMap(tweetText => tweetText.split(" ")) 
	val hashtags = tweetwords.filter(word => word.startsWith("#"))
	val hashtagKeyValues = hashtags.map(hashtag => (hashtag, 1))
	val hashtagCounts = 
	hashtagKeyValues.reduceByKeyAndWindow((x:Int,y:Int)=>x+y, Seconds(5), Seconds(20))
	val sortedResults = hashtagCounts.transform(rdd => rdd.sortBy(x => x._2, false))
	sortedResults.saveAsTextFiles("/Users/danielhopp/dhopp1/spark-streaming-twitter/tweets/","txt")
	sortedResults.print
	ssc.checkpoint("/Users/danielhopp/dhopp1/spark-streaming-twitter/checkpoints/")
    //tweets.saveAsTextFiles("/Users/danielhopp/dhopp1/spark-streaming-twitter/tweets", "json")
    ssc.start()
    ssc.awaitTermination()
  /*}*/
}