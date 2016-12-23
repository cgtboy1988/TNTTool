package tntTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

public class SentenceSeparater
{
	public static void main(String[] args)
	{
		String theDir = "/home/developer/Desktop/project_corpora/test/untagged/";
		String outputDir = "/home/developer/Desktop/project_corpora/test/tagged/";
		String theFile = "brown.tagged";
		separateFile(theDir, theFile, outputDir);
	}
	
	
	public static void separateFile(String theDirectory, String theFile, String outputDirectory)
	{
		File curFile = new File(theDirectory + theFile);
		String finalOutput = "";
		try
		{
			BufferedReader fileReader = new BufferedReader(new FileReader(curFile));
			String curLine;
			String curSent = "";
			String curPOSSent = "";
			int sentNum = 0;
			while((curLine=fileReader.readLine()) != null)
			{
				//System.out.println(curLine);
				//if(curLine.equals(System.getProperty("line.separator")))
				//{
				//	System.out.println("Sentence: " + curSent);
				//	curSent = "";
				//	System.out.println("New Sent");
				//}
				//else
				{
					//System.out.println(curLine);
					Scanner myScanner = new Scanner(curLine);
					if(myScanner.hasNext())
					{
						String word = myScanner.next();
						String pos = myScanner.next();
						curSent += word + "\n";
						curPOSSent += word + "\t" + pos + "\n";
					}
					else
					{
						//System.out.println("Sentence: " + "\n" + curSent);
						System.out.println("POS Sentence: " + "\n" + curPOSSent);
						PrintWriter sentOutput = new PrintWriter(theDirectory  + "sentences" + "/" + theFile + ".raw." + sentNum);
						PrintWriter sentOutputPOS = new PrintWriter(theDirectory + "sentencesPOS" + "/" + theFile + ".raw." + sentNum + ".pos");
						curSent += "\n";
						curPOSSent += "\n";
						sentOutput.print(curSent);
						sentOutputPOS.print(curPOSSent);
						sentOutput.close();
						sentOutputPOS.close();
						
						finalOutput += curSent;
						curSent = "";
						curPOSSent = "";
						sentNum++;
					}
				}
			}
			PrintWriter finalOutputPrinter = new PrintWriter(theDirectory + theFile + ".raw");
			finalOutputPrinter.print(finalOutput);
			//System.out.println(finalOutput);
			finalOutputPrinter.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
