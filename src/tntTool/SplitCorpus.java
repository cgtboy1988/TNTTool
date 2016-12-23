package tntTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

public class SplitCorpus
{
	public static void main(String[] args)
	{
		double trainPercent = .9;
		String corpusDir = "/home/developer/Desktop/project_corpora/complete/";
		String simpleTrainDir = "/home/developer/Desktop/project_corpora/simple/";
		String trainDir = "/home/developer/Desktop/project_corpora/train/";
		String testDir = "/home/developer/Desktop/project_corpora/test/";
		File myDir = new File(corpusDir);
		String[] fileNames = myDir.list();
		Arrays.sort(fileNames);
		ArrayList allList = new ArrayList();
		ArrayList nameList = new ArrayList();
		for(int x=0; x<fileNames.length; x++)
		{
			String root = fileNames[x].split(Pattern.quote("."))[0];
			ArrayList[] output = splitFile(corpusDir, fileNames[x], trainPercent);
			ArrayList trainList = output[0];
			ArrayList testList = output[1];
			//System.out.println("(" + trainList.get(0) + ")");
			//System.out.println("(" + testList.get(0) + ")");
			//System.out.println("(" + testList.get(1) + ")");
			try
			{
				//String root = fileNames[x].split(Pattern.quote("."))[0];x
				//System.out.println(trainDir + root +".train");
				PrintWriter trainOutputPrinter = new PrintWriter(trainDir + root +".train");
				PrintWriter simpleTrainOutputPrinter = new PrintWriter(simpleTrainDir + root +".train");
				for(int y=0; y<trainList.size(); y++)
				{
					trainOutputPrinter.print(trainList.get(y) + "\n");
					simpleTrainOutputPrinter.print(trainList.get(y) + "\n");
				}
				trainOutputPrinter.close();
				simpleTrainOutputPrinter.close();
				
				int curListSize = allList.size();
				for(int y=0; y<curListSize; y++)
				{
					ArrayList[] nextOutput = (ArrayList[]) allList.get(y);
					String nextRoot = (String) nameList.get(y);
					ArrayList nextTrainList = nextOutput[0];
					ArrayList toAdd = new ArrayList();
					System.out.println("Adding " + trainList.size() +" to  " + nextTrainList.size());
					toAdd.addAll(nextTrainList);
					toAdd.addAll(trainList);
					String nameToAdd = nextRoot + root;
					System.out.println(nameToAdd);
					ArrayList[] arrayToAdd = new ArrayList[1];
					arrayToAdd[0] = toAdd;
					allList.add(arrayToAdd);
					nameList.add(nameToAdd);
					PrintWriter nextTrainOutputPrinter = new PrintWriter(trainDir + nameToAdd +".train");
					for(int z=0; z<toAdd.size(); z++)
					{
						nextTrainOutputPrinter.print(toAdd.get(z) + "\n");
					}
					nextTrainOutputPrinter.close();
				}
				
				//String root = fileNames[x].split(Pattern.quote("."))[0];
				//System.out.println(trainDir + root +".train");
				PrintWriter testOutputPrinter = new PrintWriter(testDir + "tagged/" + root +".tagged");
				for(int y=0; y<testList.size(); y++)
				{
					testOutputPrinter.print(testList.get(y) + "\n");
				}
				testOutputPrinter.close();
				
				PrintWriter testOutputUntaggedPrinter = new PrintWriter(testDir + "untagged/" + root +".raw");
				for(int y=0; y<testList.size(); y++)
				{
					String tmpString = (String) testList.get(y);
					String outputString = "";
					Scanner tmpScanner = new Scanner(tmpString);
					while(tmpScanner.hasNextLine())
					{
						String nestedString = tmpScanner.nextLine();
						String[] posSeparated = nestedString.split(Pattern.quote("\t"));
						testOutputUntaggedPrinter.print(posSeparated[0] + "\n");
					}
					testOutputUntaggedPrinter.print("\n");
				}
				testOutputUntaggedPrinter.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			allList.add(output);
			nameList.add(root);
		}
		
	}
	
	private static ArrayList[] splitFile(String dirName, String fileName, double percent)
	{
		System.out.println(dirName + fileName);
		ArrayList[] myReturn = new ArrayList[2];
		ArrayList sentList = new ArrayList();
		
		File curFile = new File(dirName + fileName);
		try
		{
			BufferedReader fileReader = new BufferedReader(new FileReader(curFile));
			String curLine;
			String curSent = "";
			int lineNum = 0;
			while((curLine=fileReader.readLine()) != null)
			{
				if(curLine.isEmpty())
				{
					if(!curSent.isEmpty())
					{
						sentList.add(curSent);
					}
					curSent = "";
				}
				else
				{
					curSent += curLine + "\n";
				}
				lineNum++;
				//System.out.println(lineNum);
			}
			ArrayList trainList = new ArrayList();
			ArrayList testList = new ArrayList();
			for(int x=0; x<sentList.size(); x++)
			{
				//System.out.println(x);
				if(x < (sentList.size() * percent))
				{
					trainList.add(sentList.get(x));
				}
				else
				{
					testList.add(sentList.get(x));
				}
			}
			myReturn[0] = trainList;
			myReturn[1] = testList;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return myReturn;
	}
}
