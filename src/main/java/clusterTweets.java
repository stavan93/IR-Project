/**
*
* @author  Gayathri Venkatasrinivasan
*/
import java.util.ArrayList;
import java.util.List;

public class clusterTweets {
	public List<TweetData> tweetList;
	public TweetData centroidTweet;
	public int id;
	public clusterTweets(int id) 
	{
		this.id = id;
		this.tweetList = new ArrayList();
		this.centroidTweet = null;
	}
	/*
	The getter and setter for the centroid tweets
	*/
	public void setCentroidTweet(TweetData t)
	{
		this.centroidTweet = t;
	}
	public TweetData getCentroidTweet()
	{
		return this.centroidTweet;
	}
	//Printing the points of the cluster of tweets
	public void printCluster()
	{
		System.out.println("[clusterTweets: " + this.id+"]");
		System.out.println("[Centroid: " + this.centroidTweet.getText()+ "]");
		System.out.println("[Points:");
		for(TweetData t : tweetList) {
			System.out.print(t.getId() +",");
		}
		System.out.println("]");
	}
	public void addPoint(TweetData t)
	{
		this.tweetList.add(t);
	}
	public List<TweetData> getPoints()
	{
		return this.tweetList;
	}
	//Clearing the tweet data
	public void clear()
	{
		tweetList.clear();
	}	
}