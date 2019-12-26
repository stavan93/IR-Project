import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.w3c.dom.*;
import javax.xml.parsers.*;


//Reading the file train.pages.cbor-paragraphs.cbor from the TREC Complex Answer Retrieval data set that has been unpacked
public class Demo
{
	public String content;
	public String Item_id;
	public HashMap data = new HashMap<String,String>();

	
public String getContent() {
		return content;
	}



	public void setContent(String content) {
		this.content = content;
	}



	public String getItem_id() {
		return Item_id;
	}



	public void setItem_id(String item_id) {
		Item_id = item_id;
	}



   public HashMap<String,String> getData() {
		return data;
	}



	public void setData(HashMap data) {
		this.data = data;
	}



public HashMap<String,String> XMLParser()
{
		try{
			 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			 DocumentBuilder builder = factory.newDocumentBuilder();
			 Document document = builder.parse(new File("xlime_newsfeed_en_90001-92500.xml"));
			 document.getDocumentElement().normalize();
			 System.out.println("Root element :" + document.getDocumentElement().getNodeName());
			 NodeList nList = document.getElementsByTagName("item");
			 System.out.println("---------------------------------------------------------");
			 for (int temp = 0; temp < nList.getLength(); temp++) {

		Node nNode = nList.item(temp);
		String actualcontent = null;	
		//System.out.println("\nCurrent Element :" + nNode.getNodeName());
				
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
			
			Element eElement = (Element) nNode;
			String content = eElement.getElementsByTagName("content").item(0).getTextContent();
			if(content.contains("<p>"))
			{
				int Index= content.indexOf("<p>");
				actualcontent= content.substring(0, Index);
			}
			
			setItem_id(eElement.getAttribute("_id"));
			setContent(actualcontent);
			data.put(getItem_id(), getContent());
			
			}
		}
			//setData(data);
    }

    catch(Exception e){
		e.printStackTrace();		
	}
		return data;
}
}
	


