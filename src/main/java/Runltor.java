import java.io.*;
public class Runltor {
	
	public void executeCommands() throws IOException, InterruptedException {

	    File tempScript = createTempScript();

	    try {
	        ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
	        pb.inheritIO();
	        Process process = pb.start();
	        process.waitFor();
	    } finally {
	        tempScript.delete();
	    }
	}

	public File createTempScript() throws IOException {
	    File tempScript = File.createTempFile("script", null);

	    Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
	            tempScript));
	    PrintWriter printWriter = new PrintWriter(streamWriter);

	    printWriter.println("#!/bin/bash");
	    printWriter.println("java -jar RankLib.jar");
	    printWriter.println("java -jar RankLib.jar -train Feature-Vector.txt -save mymodel.txt");
	    printWriter.println("java -jar RankLib.jar -train Feature-Vector.txt -save mymodel.txt");
	    printWriter.println("java -jar RankLib.jar -train Feature-Vector.txt -ranker 4 -kcv 5 -kcvmd models/ -kcvmn ca -metric2t MAP");
	    printWriter.println("java -jar RankLib.jar -load mymodel.txt -rank Feature-Vector.txt -score myScoreFile.txt");
	    printWriter.println("java -jar RankLib.jar -rank Feature-Vector.txt -load mymodel.txt -indri myNewRankedLists.txt");
	    printWriter.close();

	    return tempScript;
	}

}
