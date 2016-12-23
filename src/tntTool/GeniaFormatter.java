package tntTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GeniaFormatter
{
	
	public static void main(String[] args)
	{
		String mappingFile = "/home/developer/Desktop/project_corpora/mappings/en-genia.map";
		String theDir = "/home/developer/Desktop/project_corpora/original/genia/";
		String theHeadFile = "GENIAcorpus3.02.pos.txt";
		String outputDir = "/home/developer/Desktop/project_corpora/complete/";
		String outputFile = "genia.tagged";
		
		HashMap translationMap = loadMappings(mappingFile);
		System.out.println(translationMap);
		//if(true)
		//{
		//	return;
		//}
		
		//int size = 200;
		//String outputString = "";
		//File folder = new File(theDir);
		//String[] fileNames = folder.list();
		//for(int x=0; x<fileNames.length; x++)
		//{
		//	System.out.println(fileNames[x]);
		//}
		//for(char y = 'a'; y <= 'z'; y++)
		//{
		//	for(int x=1; x<=size; x++)
		//	{
		//		String paddedInt = ""+x;
		//		if(x<10)
		//		{
		//			paddedInt = "000" + paddedInt;
		//		}
		//		else if(x<100)
		//		{
		//			paddedInt = "00" + paddedInt;
		//		}
		//		else if(x<1000)
		//		{
		//			paddedInt = "0" + paddedInt;
		//		}
		//		String theFile = theHeadFile + paddedInt + ".pos";
				//System.out.println(theDir + theFile);
				//System.out.println(theFile);
		//		if(Arrays.asList(fileNames).contains(theFile))
		//		{
		//			System.out.println(theFile);
		//			//if(true)
					//{
					//	continue;
					//}
		//			outputString += separateFile(theDir, theFile, translationMap);
		//		}
		//	}
		//}
		String theFile = theHeadFile;
		String outputString = "";
		outputString = separateFile(theDir, theFile, translationMap);
		try
		{
			PrintWriter finalOutputPrinter = new PrintWriter(outputDir + outputFile);
			finalOutputPrinter.print(outputString);
			finalOutputPrinter.close();
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		//System.out.println(outputString);
	}
	
	public static HashMap loadMappings(String theFile)
	{
		HashMap myMap = new HashMap();
		File curFile = new File(theFile);
		
		try
		{
			BufferedReader fileReader = new BufferedReader(new FileReader(curFile));
			String curLine;
			while((curLine=fileReader.readLine()) != null)
			{
				Scanner tmpScanner = new Scanner(curLine);
				String from = tmpScanner.next();
				String to = tmpScanner.next();
				myMap.put(from, to);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		return myMap;
	}
	
	
	public static String separateFile(String theDirectory, String theFile, HashMap translationMap)
	{
		//System.out.println(theFile);
		File curFile = new File(theDirectory + theFile);
		StringBuilder finalOutput = new StringBuilder((int)curFile.length());
		String curSent = "";
		try
		{
			BufferedReader fileReader = new BufferedReader(new FileReader(curFile));
			String curLine;
			int x = 0;
			while((curLine=fileReader.readLine()) != null)
			{
				x++;
				//System.out.println(curSent);
				//if(theFile.equals("wsj_0002.pos"))
				//{
				//	System.out.println(curLine);
				//}
				if(curLine.isEmpty() || curLine.equals("===================="))
				{
					//if(theFile.equals("wsj_0002.pos"))
					//{
					//	System.out.println("Got here 1");
					//}
					if(curSent.equals("") || curSent.equals("===================="))
					{
						//if(theFile.equals("wsj_0002.pos"))
						//{
						//	System.out.println("Got here 2");
						//}
						continue;
					}
					//System.out.println("Sent at "+x);
					//System.out.println(curSent);
					Scanner myScanner = new Scanner(curSent);
					while(myScanner.hasNext())
					{
						String nextWord = myScanner.next();
						Scanner nestedScanner = new Scanner(nextWord).useDelimiter("/");
						String first = nestedScanner.next();
						if(!nestedScanner.hasNext())
						{
							continue;
						}
						String second = nestedScanner.next();
						while(nestedScanner.hasNext())
						{
							first = first + "/" + second;
							second = nestedScanner.next();
						}
						if(first.isEmpty())
						{
							first = "/";
						}
						//System.out.println(first);
						//System.out.println(second);
						//Scanner nestedNestedScanner = new Scanner(second);
						//if(!nestedNestedScanner.hasNext(Pattern.compile(".*[A-Za-z0-9]+.*")))
						//{
							//System.out.println(theFile);
							//System.out.println(second);
							//second = "PUNCT";
						//}
						//System.out.println(second);
						String tmp = second;
						second = second.toUpperCase();
						if(translationMap.containsKey(second))
						{
							second = (String) translationMap.get(second);
						}
						else
						{
							System.out.println(theFile + " : " + first + " : " + tmp);
							System.out.println();
						}
						//System.out.println(second);
						//System.out.println();
						second = second.toUpperCase();
						//System.out.println(second);
						nextWord = first + "\t" + second;
						finalOutput.append(nextWord + "\n");
					}
					//finalOutput += curSent;
					finalOutput.append("\n");
					curSent = "";
				}
				else
				{
					curSent += " " + curLine;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println(finalOutput);
		return finalOutput.toString();
	}
}
