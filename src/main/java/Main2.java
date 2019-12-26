import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.apache.lucene.queryparser.classic.ParseException;
import net.didion.jwnl.JWNLException;
public class Main2 {

	public static void main(String[] args) throws IOException, ParseException, JWNLException {


			Scanner scan = new Scanner(new File("Queries.txt"));
			Path path = FileSystems.getDefault().getPath("index-directory");
			CreateQrelsFile cq = new CreateQrelsFile();
			cq.XMLParser();
			DeleteDir.main(args);
			if (!Files.exists(path)) {
				System.out.println("Indexing File...");
				IndexFiles indexer = new IndexFiles();
				indexer.indexfile();
				System.out.println("Indexing Done.");
			
			}//if
	
			
			System.out.println("Performing Search on Query Expansion...");
			SearchEngineD search = new SearchEngineD();
			SearchEngineD search1 = new SearchEngineD();
			SearchEngineD search2 = new SearchEngineD();
			SearchEngineD search3 = new SearchEngineD();
			SearchEngineD search4 = new SearchEngineD();
			SearchEngineD search5 = new SearchEngineD();
			SearchEngineD search6 = new SearchEngineD();
			SearchEngineD search7 = new SearchEngineD();
			
			search.displayresults("qe_thesaurus" , "unilap");
			search1.displayresults("qe_thesaurus", "unijm");
			search2.displayresults("qe_thesaurus", "unidir");
			search3.displayresults("qe_thesaurus", "bm25");
			search4.displayresults("qe_relfeed", "unilap");
			search5.displayresults("qe_relfeed", "unijm");
			search6.displayresults("qe_relfeed", "unidir");
			search7.displayresults("qe_relfeed", "bm25");
			
			System.out.println("Search done");
			scan.close(); 
			
		
			}//main
	}//main