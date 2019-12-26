import java.io.File;
import java.io.IOException;
 


 public class DeleteDir {
    	private static final String src_folder = "index-directory";
    	
        public static void main(String[] args)
        {	
        	
        	File directory = new File(src_folder);
        	if(!directory.exists()){
                    System.out.println("Directory does not exist.");
     
            }//if
        	else{
               try{ 
                   deletee(directory);
               }catch(IOException e){
                   e.printStackTrace();
                   System.exit(0);
               }
            }//else
     
        	System.out.println("Done");
        }//main
     
        public static void deletee(File file)
        	throws IOException{
     
        	if(file.isDirectory()){
        		if(file.list().length==0){     			
        		   file.delete();
        		   System.out.println("Directory is deleted : " 
                                                     + file.getAbsolutePath());
        			
        		}//if
        		else{
            	   String files[] = file.list();
         
            	   for (String temp : files) {
            	      File fileDelete = new File(file, temp);
            	     deletee(fileDelete);
            	   }//for
            	   if(file.list().length==0){
               	     file.delete();
            	     System.out.println("Directory is deleted : " 
                                                      + file.getAbsolutePath());
            	   }//if
        		}//else
        		
        	}else{
        		file.delete();
        		System.out.println("File is deleted : " + file.getAbsolutePath());
        	}//else
        }//deletee
    }//DeleteDir

