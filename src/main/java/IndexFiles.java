import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.unh.cs.treccar_v2.Data;
import edu.unh.cs.treccar_v2.read_data.DeserializeData;

//Reading the file dataset xml file from the brexit dataset and perform indexing on the same.

public class IndexFiles {

	public IndexFiles() {
	}

	// indexwriter is created, used to write to index in lucene
	private IndexWriter indexwriter = null;
	Scanner scan = new Scanner(System.in);

	public IndexWriter getindexwriter(boolean create) throws IOException {
		if (indexwriter == null) {
			Directory indexDir = FSDirectory.open(Paths.get("index-directory"));
			IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
			indexwriter = new IndexWriter(indexDir, config);
		}

		return indexwriter;
	}
	// Using the DeserializeData.iterableParagraphs,each paragraph from the corpus
	// is converted to Lucene document

	public void indexfile()throws IOException{
		IndexWriter writer = getindexwriter(false);
			 //final FileInputStream xmlFile = new FileInputStream(new File("C:\\Users\\Vivek\\Downloads\\dataset.xml"));
			Demo d = new Demo();
			        HashMap data =   new HashMap<String,String>();
			        data= d.XMLParser();
			        for(Map.Entry<String, String> entry : d.getData().entrySet())
			        {
			            System.out.println(entry);
						Document doc = new Document();
						doc.add(new StringField("id",entry.getKey(),Field.Store.YES));
						doc.add(new TextField("content",entry.getValue(),Field.Store.YES));
						writer.addDocument(doc);
			        }
				
			 
		
		//If indexwriter is not null, indexwriter is closed
		if(indexwriter != null) {
			indexwriter.close();
		}
		
	}

}
