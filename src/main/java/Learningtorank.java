import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

public class Learningtorank {
	ArrayList<String> list = null;
	DecimalFormat dec = null;
	Map<String,ArrayList<String>>mp = null;
	IndexReader reader = null;
	
	public Learningtorank() throws IOException {
		Directory indexDir = FSDirectory.open(Paths.get("Candidate Set/index-directory"));
		reader = DirectoryReader.open(indexDir);
		list = new ArrayList<String>(); 
		dec = new DecimalFormat("#0.0000");
		mp = new HashMap<String,ArrayList<String>>();
		
	}
	
	public void createMap() {
		
		
			try {
				for(int i = 0;i < reader.maxDoc();i++) {
					Document doc = reader.document(i);
					String text = doc.getField("id").stringValue();
					list.add(text);
				}
			}
			catch(Exception e) {
				
			}
		try {
			Scanner scan = new Scanner(new File("Queries.txt"));
			while(scan.hasNextLine()) {
				String query = scan.nextLine();
				String[] queryy = query.split(",");
				mp.put(queryy[0], list);
			}
		}
		catch(Exception e) {
			
		}
		/*for (HashMap.Entry<String, ArrayList<String>> entry : mp.entrySet()) {
			ArrayList<String> value = entry.getValue();
		    for(String tweet : value) {
		    	System.out.println(entry.getKey()+": "+tweet);
		    }
		}*/
	}
	
	public String readqrelfile(String s,String d,File file) throws FileNotFoundException {
		String ans = "";
		String temp = s + " "+"0"+" " + d;
    	Scanner scan = new Scanner(file);
    	while(scan.hasNextLine()) {
    		String line = scan.nextLine();
    		if(line.contains(temp)) {
    			ans = "1" + " " + "qid:" + s;
    		}
    	}
    	if(ans.equals("")) {
    		ans = "0" + " " + "qid:" + s;
    	}
		return ans;
	}
	
	public String readrunfile(String s,String d,File file,int j) throws FileNotFoundException {
		String ans = "";
		Scanner scan = new Scanner(file);
    	String temp = s + " "+"Q0"+" " + d;
    	while(scan.hasNextLine()) {
    		String line = scan.nextLine();
    		if(line.contains(temp)) {
    			String[] temp1 = line.split(" ");
    			double i = Double.parseDouble(temp1[3]);
    			//System.out.println(i);
    			double rank = 1.0/i;
    			dec.format(rank);
    			//System.out.println(rank);
    			ans = Integer.toString(j)+":"+Double.toString(rank);
    		}
    	}
    	if(!ans.contains(Integer.toString(j)+":")) {
    		//System.out.println("0");
    		ans = ans + Integer.toString(j)+":0";
    	}
		return ans;
	}
	
	public void createfeaturevector() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("Feature-Vector.txt"));
		createMap();
		for (Map.Entry< String,ArrayList<String>> me:mp.entrySet()) {
			String ans = "";
			String qid = me.getKey();
			ArrayList<String> did = me.getValue();
			for(String str : did) {
				ans = readqrelfile(qid,str,new File("qrels_file.qrels"));
				ans = ans +" "+ readrunfile(qid,str,new File("Runfile-BM25.run"),1); 
				ans = ans +" "+ readrunfile(qid,str,new File("Runfile-LNCLTN.run"),2);
				ans = ans +" "+ readrunfile(qid,str,new File("Runfile-Champion.run"),3);
				ans = ans +" "+ readrunfile(qid,str,new File("Runfile-Index-Elimination.run"),4);
				ans = ans +" "+ readrunfile(qid,str,new File("Runfile-Bigram.run"),5);
				ans = ans + " #docid=" + str;
				
				System.out.println(ans);
				writer.write(ans+"\n");
			}
			
		}
		writer.close();
	}
	

}
