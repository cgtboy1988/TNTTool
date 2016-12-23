package tntTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CorpusEncoder
{

	public static void main(String[] args)
	{
		TestingConnectionSource connectionSource = new TestingConnectionSource();
		Connection myConnection = connectionSource.getDatabaseConnection();
		String trainDir = "/home/developer/Desktop/project_corpora/simple/";
		File myDir = new File(trainDir);
		String[] fileNames = myDir.list();
		for(int x=0; x<fileNames.length; x++)
		{
			//System.out.println(fileNames[x]);
			Scanner nameSplitter = new Scanner(fileNames[x]).useDelimiter(Pattern.quote("."));
			String rootName = nameSplitter.next();
			System.out.println(rootName);
			File curFile = new File(trainDir + fileNames[x]);
			long lineNum = 0;
			try
			{
				myConnection.setAutoCommit(false);
				//Needs corpus, number
				String sentSQL = "INSERT IGNORE INTO `corpusDB`.`sentence` (`corpus`, `number`) VALUES (?, ?);";
				//Needs corpus, number, position, word, pos, line, vector weight
				String wordSQL = "INSERT IGNORE INTO `corpusDB`.`word` (`corpus`, `number`, `position`, `word`, `pos`, `line`, `vectorWeight`) VALUES (?, ?, ?, ?, ?, ?, ?)";
				String wordSQLValue = ",(?, ?, ?, ?, ?, ?, ?)";
				String wordSQLEnd = ";";
				BufferedReader fileReader = new BufferedReader(new FileReader(curFile));
				String curLine;
				ArrayList curSent = new ArrayList();
				ArrayList sentList = new ArrayList();
				lineNum = 0;
				boolean first = true;
				long sentNum = 0;
				
				String lastWord = ".start";
				String lastPOS = ".";
				
				while((curLine=fileReader.readLine()) != null)
				{
					if(lineNum == 136872 || lineNum == 23343)
					{
						first = true;
					}
					if(curLine.isEmpty())
					{
						lastWord = ".start";
						lastPOS = ".";
						
						sentList.add(curSent);
						double runningSum = 0;
						HashMap wordCounts = new HashMap();
						for(int y=0; y<curSent.size(); y++)
						{
							String word = (String) ((HashMap) curSent.get(y)).get("word");
							if(wordCounts.containsKey(word))
							{
								wordCounts.put(word, (int)wordCounts.get(word) + 1);
							}
							else
							{
								wordCounts.put(word, 1);
							}
							int number = 0;
							int numPunct = 0;
							for(int z=0; z<word.length(); z++)
							{
								char toTest = word.charAt(z);
								if(Character.isDigit(toTest))
								{
									number++;
								}
								else if(Character.isAlphabetic(toTest))
								{
									
								}
								else
								{
									numPunct++;
								}
							}
							if(numPunct == word.length())
							{
								//wordCounts.put(word, 0);
							}
							else if(numPunct + number == word.length())
							{
								//The word is a number
							}
							if(first)
							{
								System.out.println(curSent.get(y));
							}
						}
						Iterator iter = wordCounts.entrySet().iterator();
						double length = 0;
						while(iter.hasNext())
						{
							Entry pair = (Entry)iter.next();
							int count = (int) pair.getValue();
							length += (count * count);
						}
						//if(length == 0)
						//{
						//	length = .0000000000000001;
						//}
						length = Math.sqrt(length);
						iter = wordCounts.entrySet().iterator();
						while(iter.hasNext())
						{
							Entry pair = (Entry)iter.next();
							double count = (int) pair.getValue();
							count = count / length;
							wordCounts.put(pair.getKey(), count);
							if(first)
							{
								System.out.print(pair.getKey() + " ");
							}
						}
						if(first)
						{
							System.out.println();
							System.out.println(wordCounts);
							System.out.println(curSent);
							System.out.println();
						}
						PreparedStatement sentStmt = myConnection.prepareStatement(sentSQL);
						sentStmt.setString(1, rootName);
						sentStmt.setLong(2, sentNum);
						sentStmt.execute();
						String fullWord = wordSQL;
						for(int y=1; y<curSent.size(); y++)
						{
							fullWord += wordSQLValue;
						}
						fullWord += wordSQLEnd;
						//System.out.println(fullWord);
						PreparedStatement wordStmt = myConnection.prepareStatement(fullWord);
						int paramNum = 1;
						for(int y=0; y<curSent.size(); y++)
						{
							HashMap sentMap = ((HashMap) curSent.get(y));
							wordStmt.setString(paramNum, rootName);
							paramNum++;
							wordStmt.setLong(paramNum, sentNum);
							paramNum++;
							wordStmt.setInt(paramNum, y);
							paramNum++;
							wordStmt.setString(paramNum, (String) sentMap.get("word"));
							if(first)
							{
								System.out.println(sentMap.get("word"));
							}
							paramNum++;
							wordStmt.setString(paramNum, (String) sentMap.get("pos"));
							paramNum++;
							wordStmt.setLong(paramNum, lineNum);
							paramNum++;
							wordStmt.setDouble(paramNum, (double) wordCounts.get((String) sentMap.get("word")));
							paramNum++;
						}
						if(first)
						{
							System.out.println();
						}
						//System.out.println(paramNum);
						wordStmt.execute();
						if(sentNum % 2000 == 0.0)
						{
							myConnection.commit();
						}
						first = false;
						curSent = new ArrayList();
						sentNum++;
					}
					else
					{
						Scanner posScanner = new Scanner(curLine);
						HashMap wordMap = new HashMap();
						String tmp = posScanner.next();
						
						String tmp2 = "";
						
						if(posScanner.hasNext())
						{
							wordMap.put("word", tmp);
							tmp2 = posScanner.next();
							wordMap.put("pos", tmp2);
							lastWord = tmp;
							lastPOS = tmp2;
						}
						else
						{
							wordMap.put("word", "");
							wordMap.put("pos", tmp);
							//System.out.println(wordMap);
							lastWord = "";
							lastPOS = tmp;
						}
						wordMap.put("line", lineNum);
						curSent.add(wordMap);
						
						//System.out.println(x);
					}
					lineNum++;
				}
			}
			catch(Exception e)
			{
				System.err.println(lineNum);
				e.printStackTrace();
				return;
			}
			try
			{
				myConnection.commit();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}

}
