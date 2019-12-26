/**
* @author Gayathri Venkatasrinivasan
*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import org.apache.lucene.document.Document;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class KMeans {

	private List<TweetData> lastcentroidlist;
	private List<clusterTweets> clusterDatalist;
	private int kData = 3;

	/**
	 * Process the initial seed data given and add the centroids 463064318
	 * 
	 * @param path
	 * @param tweetDataList
	 * @throws IOException
	 */
	private void getSeedData(String path, List<TweetData> tweetDataList) throws IOException {
		FileInputStream fin = new FileInputStream(path);
		BufferedReader seedData = new BufferedReader(new InputStreamReader(fin));
		String line;
		List<String> s = new ArrayList<>();
		while ((line = seedData.readLine()) != null) {
			s.add(line.replace(",", ""));
		}
		seedData.close();
		clusterDatalist = new ArrayList<>();
		for (int i = 0; i < kData; i++) {
			for (int j = 0; j < tweetDataList.size(); j++) {
				if (s.get(i).equals(tweetDataList.get(j).id)) {
					clusterTweets cL = new clusterTweets(i);
					cL.centroidTweet = tweetDataList.get(j);
					clusterDatalist.add(cL);
				}
			}
		}

	}

	/**
	 * Calculates JaccardDistance for the centroid and tweet data
	 * 
	 * @param centroidTweets
	 * @param tweet
	 * @return
	 */
	private double calculateJaccardDistance(String centroidTweets, String tweet) {
		List<String> a = Arrays.asList(centroidTweets.toLowerCase().split(" "));
		List<String> b = Arrays.asList(tweet.toLowerCase().split(" "));

		Set<String> union = new HashSet<String>(a);
		union.addAll(b);

		Set<String> intersection = new HashSet<String>(a);
		intersection.retainAll(b);

		return (double) (1 - (intersection.size() / (double) union.size()));

	}

	/**
	 * Method to calculate the euclidean distance
	 * 
	 * @param centroidTweet
	 * @param tweet
	 * @return
	 * @throws IOException
	 */
	private double calculateEuclideanDistance(String centroidTweet, String tweet) throws IOException {
		// List<String> centroidTerms =
		// Arrays.asList(centroidTweet.toLowerCase().split(" "));
		// List<String> documentTerms = Arrays.asList(tweet.toLowerCase().split(" "));
		Map<String, Double> centroidMap = new HashMap<>();
		List<String> centroidTerms = tokenizeTweet(centroidTweet);
		List<String> documentTerms = tokenizeTweet(tweet);
		Map<String, Double> tweetTextMap = new HashMap<>();
		Set<String> keySet = new HashSet<String>(centroidTerms);
		keySet.addAll(documentTerms);
		double sum = 0;
		// Map<String, Double> centroidTweetDistanceMap = new HashMap();
		for (String centroidTerm : centroidTerms) {
			if (!centroidMap.containsKey(centroidTerm)) {
				centroidMap.put(centroidTerm, 1.0);
			} else {
				centroidMap.put(centroidTerm, centroidMap.get(centroidTerm) + 1);
			}
		}
		for (String documentTerm : documentTerms) {
			if (!tweetTextMap.containsKey(documentTerm)) {
				tweetTextMap.put(documentTerm, 1.0);
			} else {
				tweetTextMap.put(documentTerm, tweetTextMap.get(documentTerm) + 1);
			}
		}

		for (String key : keySet) {
			try {
				Double cDistance = (centroidMap.get(key) != null ? centroidMap.get(key) : 0.0);
				Double docDistance = (tweetTextMap.get(key) != null ? tweetTextMap.get(key) : 0.0);
				if (cDistance != null && docDistance != null) {
					sum += Math.pow(cDistance - docDistance, 2);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Math.sqrt(sum);

	}

	/**
	 * Tokenizes the tweet given to have only unique terms
	 * 
	 * @param tweet
	 * @throws IOException
	 */
	private List<String> tokenizeTweet(String tweet) throws IOException {
		Analyzer analyzer = new StandardAnalyzer();
		List<String> documentTokens = new ArrayList<String>();
		TokenStream stream = analyzer.tokenStream("content", tweet);
		stream.reset();
		while (stream.incrementToken()) {
			String termString = stream.getAttribute(CharTermAttribute.class).toString();
			documentTokens.add(termString);
		}
		return documentTokens;
	}

	/**
	 * Clusters the tweetlist for the given seed information
	 * 
	 * @param tweetDataList
	 * @throws IOException
	 */
	private void populateCLusters(List<TweetData> tweetDataList) throws IOException {

		deleteClusterData();
		List<Double[]> distancelist = new ArrayList<>();
		for (int i = 0; i < kData; i++) {
			Double[] distance = new Double[tweetDataList.size()];
			for (int j = 0; j < tweetDataList.size(); j++) {
				distance[j] = calculateEuclideanDistance(clusterDatalist.get(i).centroidTweet.text,
						tweetDataList.get(j).text);
			}
			distancelist.add(distance);
		}

		for (int i = 0; i < tweetDataList.size(); i++) {
			Double min = Double.MAX_VALUE;
			int index = 0;
			for (int k = 0; k < kData; k++) {
				if (distancelist.get(k)[i] < min) {
					min = distancelist.get(k)[i];
					index = k;
				}
			}
			clusterDatalist.get(index).tweetList.add(tweetDataList.get(i));
		}
	}

	/**
	 * Recalculates the centroid as per the data
	 * 
	 * @throws Exception
	 */
	private void recalculateCentroids() throws Exception {

		for (int i = 0; i < kData; i++) {
			int index = 0;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < clusterDatalist.get(i).tweetList.size(); j++) {

				double distance = 0;

				for (int k = 0; k < clusterDatalist.get(i).tweetList.size(); k++) {
					distance += calculateEuclideanDistance(clusterDatalist.get(i).tweetList.get(j).getText(),
							clusterDatalist.get(i).tweetList.get(k).getText());
				}

				if (distance < min) {
					min = distance;
					index = j;
				}
			}
			clusterDatalist.get(i).centroidTweet = clusterDatalist.get(i).tweetList.get(index);
		}

	}

	/**
	 * Flushes the centroid list of the previous run and adds them to the
	 * lastCentroidList
	 */
	private void deleteClusterData() {
		lastcentroidlist = new ArrayList<>();
		for (int i = 0; i < clusterDatalist.size(); i++) {
			clusterDatalist.get(i).tweetList.clear();
			lastcentroidlist.add(clusterDatalist.get(i).centroidTweet);// preserve older centroids
		}
	}

	/**
	 * Prints all the relevant data for the data set provided
	 * 
	 * @param outputfilepath
	 * @param sseValidation
	 * @throws IOException
	 */
	private void printClusters(String outputfilepath, double sseValidation) throws IOException {
		try (PrintWriter out = new PrintWriter(new FileWriter(outputfilepath, true))) {
			out.println("ClusterNo" + "\t" + "TweetId");
			for (int i = 0; i < clusterDatalist.size(); i++) {

				out.print(i + 1 + "\t\t");
				String s = "";
				for (int j = 0; j < clusterDatalist.get(i).tweetList.size(); j++) {
					s += clusterDatalist.get(i).tweetList.get(j).id.toString() + ",";
				}
				out.println(s);

			}
			out.println("The Squared Error is: " + sseValidation);
		}

	}

	/**
	 * Calculates the sum of squared errors
	 * 
	 * @return
	 * @throws Exception
	 */
	private double findSSEValidation() throws Exception {
		double sseValidation = 0;
		for (clusterTweets cL : clusterDatalist) {
			double distance = 0;
			double tempDist = 0;
			for (int k = 0; k < cL.tweetList.size(); k++) {
				tempDist = calculateEuclideanDistance(cL.centroidTweet.text, cL.tweetList.get(k).text);
				distance += (tempDist * tempDist);
			}
			sseValidation += distance;
		}
		return sseValidation;
	}

	/**
	 * Processing for KMeans for the given tweetlist starts here
	 * 
	 * @param tweetDataList
	 * @throws Exception
	 */
	public void start() throws Exception {
		// kData = Integer.parseInt(args[0]);
		// String initialSeedData = args[1];
		// String inputJSONData = args[2];
		// String outputFileData = args[3];
		List<TweetData> tweetDataList = TweetDataList();
		kData = 3;
		String initialSeedData = "InitialSeeds.txt";
		String outputFileData = "output.txt";
		getSeedData(initialSeedData, tweetDataList);
		for (int j = 0; j < 3; j++) {
			populateCLusters(tweetDataList);
			recalculateCentroids();
			List<TweetData> currentCentroidList = new ArrayList<>();
			double valueChange = 0;

			for (int i = 0; i < clusterDatalist.size(); i++) {

				currentCentroidList.add(clusterDatalist.get(i).centroidTweet);// new centroids
				valueChange += calculateEuclideanDistance(lastcentroidlist.get(i).text,
						currentCentroidList.get(i).text);
			}

			if (valueChange == 0) {
				break;
			}
		}
		double sseValidation = findSSEValidation();
		printClusters(outputFileData, sseValidation);
		System.out.println("Squared Error is: " + sseValidation);
	}

	public List<TweetData> TweetDataList() throws IOException  {
		Scanner scan = new Scanner(System.in);
		System.out.println("Choose Runfile:");
		String runfilepath = scan.next();
	Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
		IndexReader reader = DirectoryReader.open(indexDir);
		// Path filePath = FileSystems.getDefault().getPath(qrelsPath);
		Scanner scanner;
		List<TweetData> tweetDataList = new ArrayList<>();
		Set<String> tweetIdSet = new HashSet<>();
		try {
			scanner = new Scanner(new File(runfilepath));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] lineValues = line.split(" ");
				String tweetId = lineValues[2];
				if (!tweetIdSet.contains(tweetId)) {
					tweetIdSet.add(tweetId);
					//TweetData tweetData = new TweetData();
					// String queryId = lineValues[0];
					//tweetData.setId(tweetId);
					//tweetData.setText(lineValues[6]);
					//tweetDataList.add(tweetData);
				}
			}
			
			for(String tweetId : tweetIdSet)
			{
				for(int i = 0;i < reader.maxDoc();i++) {
			        Document doc = reader.document(i);
			        String doc_id = doc.getField("id").stringValue();
			        if(tweetId.equals(doc_id)) {
			        String text = doc.getField("content").stringValue();
			        String[] textt = text.split(" ");
			        TweetData tweetData = new TweetData();
					// String queryId = lineValues[0];
					tweetData.setId(tweetId);
					tweetData.setText(text);
					tweetDataList.add(tweetData);
		}
		} 
			}
		}
		
			catch (Exception e) {
			System.out.println(e);
		}
		return tweetDataList;
	}
}
