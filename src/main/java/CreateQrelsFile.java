import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreateQrelsFile {
	
	public void XMLParser() throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter("qrels_file.qrels"));
			try{
				
				 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				 DocumentBuilder builder = factory.newDocumentBuilder();
				 Document document = builder.parse(new File("/data/brexit/xlime_newsfeed_en_90001-92500.xml"));
				 document.getDocumentElement().normalize();
				 //System.out.println("Root element :" + document.getDocumentElement().getNodeName());
				 NodeList nodeList = document.getElementsByTagName("*");
				 String _id = "";
				    for (int i = 0; i < nodeList.getLength(); i++) {
				        Node node = nodeList.item(i);
				        if (node.getNodeName() == "item") {
				            // do something with the current element
				        	Element element = (Element) node;
				            //System.out.println(element.getAttribute("_id")+" "+element.getAttribute("displayName"));
				            _id = element.getAttribute("_id");
				        }
				        if (node.getNodeName() == "Category") {
				            // do something with the current element
				        	Element element = (Element) node;
				        	if(element.getAttribute("weight").equals("1")) {
				        		System.out.println(element.getAttribute("id")+" "+"0 "+_id+" "+element.getAttribute("weight"));
				        		writer.write(element.getAttribute("id")+" "+"0 "+_id+" "+element.getAttribute("weight")+"\n");
				        	}
				            
				        }
				    }
				    writer.close();
			}
			

	    catch(Exception e){
			e.printStackTrace();		
		}
	}
}
