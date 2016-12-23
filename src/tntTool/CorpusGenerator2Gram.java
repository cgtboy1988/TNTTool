package tntTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

//import tntTool.IsolatedCorpusGenerator.EntryValueComparator;

public class CorpusGenerator2Gram
{
	private static class EntryValueComparator implements Comparator<Entry>
	{
		public EntryValueComparator()
		{
			
		}
		@Override
		public int compare(Entry x, Entry y)
		{
			Comparable xVal = (Comparable) x.getValue();
			Comparable yVal = (Comparable) y.getValue();
			return -xVal.compareTo(yVal);
		}
	}
	
	public static void main(String[] args)
	{
		int maxSents = 10000;
		double cutoff = .25;
		
		TestingConnectionSource connectionSource = new TestingConnectionSource();
		Connection myConnection = connectionSource.getDatabaseConnection();
		String trainDir = "/home/developer/Desktop/project_corpora/test/untagged/";
		String outputDir = "/home/developer/Desktop/project_corpora/train/";
		File myDir = new File(trainDir);
		String[] fileNames = myDir.list();
		
		
		
		for(int x=0; x<fileNames.length; x++)
		{
			System.gc();
			
			long beginningFileTime = System.currentTimeMillis();
			Scanner nameSplitter = new Scanner(fileNames[x]).useDelimiter(Pattern.quote("."));
			String rootName = nameSplitter.next();
			System.out.println(rootName);
			
			HashMap wordValMap = new HashMap();
			HashMap sentValMap = new HashMap();
			HashMap sentOutputMap = new HashMap();
			
			try
			{
				myConnection.setAutoCommit(false);
				//Needs word, vector weight
				String selectSQLStart = "SELECT DISTINCT * FROM `corpusDB`.`2Grams`";
				//String selectSQLWhere = " WHERE (`word` = ? AND vectorWeight >= ?) ";
				String selectSQLWhere = " WHERE `corpus` != '" + rootName + "' ";
				//String selectSQLEnd = "GROUP BY `corpus`, `number`, `word` ORDER BY `word`.`vectorWeight` DESC";
				String selectSQLEnd = "ORDER BY `2Grams`.`vectorWeight` DESC";
				
				long queryTime = System.currentTimeMillis();
				PreparedStatement curStatement = myConnection.prepareStatement(selectSQLStart + selectSQLEnd);
				
				ResultSet myResults = curStatement.executeQuery();
				queryTime = System.currentTimeMillis() - queryTime;
				
				long processTime = System.currentTimeMillis();
				long numResults = 0;
				while(myResults.next())
				{
					numResults++;
					String tmpCorpus = myResults.getString("corpus");
					
					HashMap corpusMap;
					if(sentValMap.containsKey(tmpCorpus))
					{
						corpusMap = (HashMap) sentValMap.get(tmpCorpus);
					}
					else
					{
						corpusMap = new HashMap();
					}
					
					long tmpNumber = myResults.getLong("number");
					
					HashMap numberMap;
					if(corpusMap.containsKey(tmpNumber))
					{
						numberMap = (HashMap) corpusMap.get(tmpNumber);
					}
					else
					{
						numberMap = new HashMap();
					}
					
					String tmpWord = myResults.getString("word");
					
					double tmpVector = myResults.getDouble("vectorWeight");
					
					int position = myResults.getInt("position");
					
					String pos = myResults.getString("pos");
					
					ArrayList finalKeyList = new ArrayList();
					finalKeyList.add(tmpCorpus);
					finalKeyList.add(tmpNumber);
					HashMap orderedWordMap;
					if(sentOutputMap.containsKey(finalKeyList))
					{
						orderedWordMap = (HashMap) sentOutputMap.get(finalKeyList);
					}
					else
					{
						orderedWordMap = new HashMap();
					}
					
					ArrayList sentPOS = new ArrayList();
					sentPOS.add(tmpWord);
					sentPOS.add(pos);
					
					orderedWordMap.put(position, sentPOS);
					
					sentOutputMap.put(finalKeyList, orderedWordMap);
					
					boolean alreadyPresent = numberMap.containsKey(tmpWord);
					
					//System.out.println(tmpNumber);
					//System.out.println(tmpWord);
					//System.out.println(tmpVector);
					numberMap.put(tmpWord, tmpVector);
					corpusMap.put(tmpNumber, numberMap);
					sentValMap.put(tmpCorpus, corpusMap);
					
					ArrayList tmpList = new ArrayList();
					tmpList.add(tmpCorpus);
					tmpList.add(tmpNumber);
					tmpList.add(tmpVector);
					
					//System.out.println(tmpVector);
					
					if(!alreadyPresent)
					{
						ArrayList orderedWordList;
						if(wordValMap.containsKey(tmpWord))
						{
							orderedWordList = (ArrayList) wordValMap.get(tmpWord);
						}
						else
						{
							orderedWordList = new ArrayList();
						}
						orderedWordList.add(tmpList);
						wordValMap.put(tmpWord, orderedWordList);
						//System.out.println(tmpWord);
						//System.out.println(orderedWordList);
					}
					//if(numResults % 10 == 0)
					//{
					//	System.out.println(sentValMap);
					//	return;
					//}
				}
				
				//System.out.println(wordValMap.get("both"));
				//System.out.println(((HashMap)sentValMap.get("genia")).keySet());
				processTime = System.currentTimeMillis() - processTime;
				System.out.println(queryTime);
				System.out.println(processTime);
				System.out.println(numResults);
				System.out.println();
				
				
				System.out.println("Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
				System.out.println("Max memory: " + Runtime.getRuntime().maxMemory());
				System.out.println();
				
				if(true)
				{
					//return;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			System.out.println(rootName);
			File curFile = new File(trainDir + fileNames[x]);
			try
			{
				BufferedReader fileReader = new BufferedReader(new FileReader(curFile));
				String curLine;
				ArrayList curSent = new ArrayList();
				ArrayList outputSents = new ArrayList();
				ArrayList inputSents = new ArrayList();
				
				HashMap firstChoiceMap = new HashMap();
				//boolean lastLine = true;
				
				HashMap sentenceChoiceMap = new HashMap();
				
				int sentCount = 0;
				
				String prevWord = ".start";
				
				int blurDiameter = 2;
				
				while((curLine=fileReader.readLine()) != null)// || lastLine)
				{
					
					//System.out.println(curLine);
					if(curLine.isEmpty())// || lastLine)
					{
						prevWord = ".start";
						sentCount++;
						//if(lastLine)
						//{
						//	lastLine = false;
						//}
						//System.out.println(curSent);
						//System.out.println();
						HashMap counts = new HashMap();
						for(int y=0; y<curSent.size(); y++)
						{
							String curWord = (String) curSent.get(y);
							if(counts.containsKey(curWord))
							{
								counts.put(curWord, (int)counts.get(curWord) + 1);
							}
							else
							{
								counts.put(curWord, 1);
							}
						}
						
						//System.out.println("1 "+ counts);
						
						Iterator iter = counts.entrySet().iterator();
						double length = 0;
						while(iter.hasNext())
						{
							Entry pair = (Entry)iter.next();
							int count = (int) pair.getValue();
							length += (count * count);
						}
						length = Math.sqrt(length);
						
						iter = counts.entrySet().iterator();
						while(iter.hasNext())
						{
							Entry pair = (Entry)iter.next();
							double count = (int) pair.getValue();
							count = count / length;
							counts.put(pair.getKey(), count);
						}
						
						//System.out.println("2 "+ counts);
						
						HashMap sentSimilarity = new HashMap();
						//double maxSentSimilarity = 0;
						ArrayList maxSentSimilarity = new ArrayList();
						//double headSimilarityLeft = 0;
						
						Comparator<Entry> comparator = new EntryValueComparator();
						PriorityQueue<Entry> orderedWordList = new PriorityQueue<Entry>(counts.size(), comparator);
						
						double similarityLeft = 0;
						
						iter = counts.entrySet().iterator();
						while(iter.hasNext())
						{
							Entry pair = (Entry)iter.next();
							orderedWordList.add(pair);
							double vecValue = (double) pair.getValue();
							similarityLeft += vecValue;
							//headSimilarityLeft += vecValue;
						}
						
						//System.out.println("3 "+ counts);
						//System.out.println(orderedWordList);
						//System.out.println();
						
						Entry curEntry = (Entry) orderedWordList.poll();
						
						HashMap resultMap = new HashMap();
						
						double maxSimilarity = 0;
						ArrayList maxSimilarityID = new ArrayList();
						PriorityQueue<Entry> orderedResults = new PriorityQueue<Entry>(1, comparator);;
						
						while(curEntry != null)
						{
							Entry pair = curEntry;
							//System.out.println(pair);
							String curWord = (String) pair.getKey();
							double vecValue = (double) pair.getValue();
							similarityLeft -= vecValue;
							
							ArrayList curWordList = (ArrayList) wordValMap.get(curWord);
							if(curWordList == null)
							{
								
							}
							else
							{
								HashMap blackList = new HashMap();
								for(int z=0; z<curWordList.size(); z++)
								{
									ArrayList indWordList = (ArrayList) curWordList.get(z);
									String indCorpus = (String) indWordList.get(0);
									long indNumber = (long) indWordList.get(1);
									double indVec = (double) indWordList.get(2);
									double result = vecValue * indVec;
									ArrayList keyList = new ArrayList();
									keyList.add(indCorpus);
									keyList.add(indNumber);
									if(!blackList.containsKey(keyList) && !sentenceChoiceMap.containsKey(keyList))
									{	
										if(resultMap.containsKey(keyList))
										{
											resultMap.put(keyList, result + (double)resultMap.get(keyList));
										}
										else
										{
											resultMap.put(keyList, result);
										}
										
										if((double)resultMap.get(keyList) > maxSimilarity)
										{
											maxSimilarity = (double)resultMap.get(keyList);
											maxSimilarityID = keyList;
										}
										
										double removeLimit = cutoff;
										if(maxSimilarity < cutoff)
										{
											removeLimit = maxSimilarity;
										}
										
										
										
										if((double)resultMap.get(keyList) + similarityLeft < removeLimit)
										{
											//System.out.println("Removing " + keyList);
											resultMap.remove(keyList);
											blackList.put(keyList, true);
										}
									}
								}
								
								if(!resultMap.isEmpty())
								{
									orderedResults = new PriorityQueue<Entry>(resultMap.size(), comparator);
								}
								else
								{
									orderedResults = new PriorityQueue<Entry>(1, comparator);
								}
								Iterator resultsIterator = resultMap.entrySet().iterator();
								while(resultsIterator.hasNext())
								{
									Entry resultsEntry = (Entry) resultsIterator.next();
									orderedResults.add(resultsEntry);
								}
								
							}
							//System.out.println("Originally from " + rootName);
							//System.out.println(maxSimilarity);
							//System.out.println(maxSimilarityID);
							if(!maxSimilarityID.isEmpty())
							{
								//System.out.println(((HashMap)sentValMap.get(maxSimilarityID.get(0))).get(maxSimilarityID.get(1)));
							}
							//System.out.println();
							curEntry = orderedWordList.poll();
						}
						
						if(!orderedResults.isEmpty())
						{
							//orderedResults.poll();
							
							int limitCount = 0;
							double curSim = 1;
							boolean first = true;
							//System.out.println(counts);
							
							while(!orderedResults.isEmpty() && limitCount < maxSents && (curSim > cutoff || first))
							{
								Entry resultEntry = orderedResults.poll();
								ArrayList key = (ArrayList) resultEntry.getKey();
								curSim = (double) resultEntry.getValue();
								if(first)
								{
									first = false;
									if(!firstChoiceMap.containsKey(key))
									{
										firstChoiceMap.put(key, 1);
									}
									else
									{
										firstChoiceMap.put(key, (int)firstChoiceMap.get(key) + 1);
									}
								}
								if(curSim > cutoff)
								{
									first = false;
									//System.out.println("Adding potential " + key);
									ArrayList addOutputList = new ArrayList();
									addOutputList.add(curSim);
									addOutputList.add(counts);
									addOutputList.add(key);
									if(!sentenceChoiceMap.containsKey(key))
									{
										//System.out.println("Adding " + key);
										outputSents.add(addOutputList);
										sentenceChoiceMap.put(key, true);
									}
								}
								//System.out.println(resultEntry);
								limitCount++;
							}
						}
						
						
						ArrayList outputList = new ArrayList();
						outputList.add(maxSimilarity);
						outputList.add(counts);
						outputList.add(maxSimilarityID);
						//firstChoiceMap.put(maxSimilarityID, true);
						if(!sentenceChoiceMap.containsKey(maxSimilarityID))
						{
							outputSents.add(outputList);
							sentenceChoiceMap.put(maxSimilarityID, true);
						}
						//if(!maxSimilarityID.isEmpty())
						//{
						//	outputList.add(((HashMap)sentValMap.get(maxSimilarityID.get(0))).get(maxSimilarityID.get(1)));
						//}
						
						//for(int u=0; u<outputList.size(); u++)
						//{
						//	System.out.println(outputList.get(u));
						//}
						
						//System.out.println();
						
						
						
						//double nextSimilarityLeft = 1;
						
						/*
						//iter = counts.entrySet().iterator();
						Entry curEntry = (Entry) orderedWordList.poll();
						String queryWhere = selectSQLWhere;
						boolean firstWhere = true;
						PriorityQueue<Entry> newOrderedWordList = new PriorityQueue<Entry>(counts.size(), comparator);
						while(curEntry != null)
						{
							Entry pair = curEntry;
							System.out.println(pair);
							String curWord = (String) pair.getKey();
							double vecValue = (double) pair.getValue();
							if(firstWhere)
							{
								queryWhere += "`word` = " + "?" + " ";
								firstWhere = false;
							}
							else
							{
								queryWhere += "OR `word` = " + "?" + " ";
							}
							newOrderedWordList.add(curEntry);
							curEntry = (Entry) orderedWordList.poll();
						}
						System.out.println(queryWhere);
						
						PreparedStatement curStatement = myConnection.prepareStatement(selectSQLStart + queryWhere + selectSQLEnd);
						curEntry = newOrderedWordList.poll();
						int wordNum = 1;
						
						long queryTime = System.currentTimeMillis();
						
						while(curEntry != null)
						{
							Entry pair = curEntry;
							//System.out.println(pair);
							String curWord = (String) pair.getKey();
							double vecValue = (double) pair.getValue();
							
							curStatement.setString(wordNum, curWord);
							
							curEntry = (Entry) newOrderedWordList.poll();
							wordNum++;
						}
						
						//curStatement = myConnection.prepareStatement(selectSQLStart + selectSQLEnd);
						
						ResultSet myResults = curStatement.executeQuery();
						queryTime = System.currentTimeMillis() - queryTime;
						
						long processTime = System.currentTimeMillis();
						long numResults = 0;
						while(myResults.next())
						{
							numResults++;
							String tmpCorpus = myResults.getString("corpus");
							long tmpNumber = myResults.getLong("number");
							ArrayList sentEntry = new ArrayList();
							sentEntry.add(tmpCorpus);
							sentEntry.add(tmpNumber);
							String tmpWord = myResults.getString("word");
							double tmpVector = myResults.getDouble("vectorWeight");
						}
						processTime = System.currentTimeMillis() - processTime;
						System.out.println(queryTime);
						System.out.println(processTime);
						*/
						
						/*while(curEntry != null)
						{
							
							Entry pair = curEntry;
							System.out.println(pair);
							String curWord = (String) pair.getKey();
							double vecValue = (double) pair.getValue();
							
							//System.out.println(nextSimilarityLeft);
							//nextSimilarityLeft -= vecValue;
							//double similarityLeft = 1 - maxSentSimilarity;
							
							String curWhere = selectSQLWhere;
							//Iterator similarityIter = sentSimilarity.entrySet().iterator();
							//while(similarityIter.hasNext())
							//{
							//	Entry nextSimilarity = (Entry) similarityIter.next();
							//	ArrayList sentKey = (ArrayList) nextSimilarity.getKey();
							//	String sentCorpus = (String) sentKey.get(0);
							//	long sentPosition = (long) sentKey.get(1);
							//	double nextSim = (double) nextSimilarity.getValue();
							//}
							
							long queryTime = System.currentTimeMillis();
							PreparedStatement curStatement = myConnection.prepareStatement(selectSQLStart + curWhere + selectSQLEnd);
							curStatement.setString(1, curWord);
							curStatement.setDouble(2, 0);
							ResultSet myResults = curStatement.executeQuery();
							queryTime = System.currentTimeMillis() - queryTime;
							double curSimilarity = 0;
							long numResults = 0;
							
							long processTime = System.currentTimeMillis();
							
							while(myResults.next())
							{
								numResults++;
								String tmpCorpus = myResults.getString("corpus");
								long tmpNumber = myResults.getLong("number");
								ArrayList sentEntry = new ArrayList();
								sentEntry.add(tmpCorpus);
								sentEntry.add(tmpNumber);
								String tmpWord = myResults.getString("word");
								double tmpVector = myResults.getDouble("vectorWeight");
								double tmpSentSim = tmpVector * vecValue;
								if(sentSimilarity.containsKey(sentEntry))
								{
									sentSimilarity.put(sentEntry, tmpSentSim + (double)sentSimilarity.get(sentEntry));
								}
								else
								{
									sentSimilarity.put(sentEntry, tmpSentSim);
								}
								tmpSentSim = (double)sentSimilarity.get(sentEntry);
								//System.out.println(sentEntry);
								//System.out.println(tmpSentSim);
								if(sentSimilarity.containsKey(maxSentSimilarity))
								{
									if((double)sentSimilarity.get(maxSentSimilarity) < tmpSentSim)
									{
										maxSentSimilarity = sentEntry;
									}
								}
								else
								{
									maxSentSimilarity = sentEntry;
								}
							}
							
							processTime = System.currentTimeMillis() - processTime;
							
							System.out.println(numResults + " results");
							System.out.println(maxSentSimilarity);
							System.out.println(sentSimilarity.get(maxSentSimilarity));
							System.out.println("Query time: " + queryTime);
							System.out.println("Process time " + processTime);
							System.out.println("Source from " + rootName);
							System.out.println();
							
							curEntry = (Entry) orderedWordList.poll();
						}*/
						
						//inputSents.add(curSent);
						//outputSents.add(maxSentSimilarity);
						//System.out.println();
						
						curSent = new ArrayList();
					}
					else
					{
						curSent.add(prevWord + " " + curLine);
						prevWord = curLine;
					}
				}
				//System.out.println(inputSents);
				//System.out.println(outputSents);
				HashMap corpusCounts = new HashMap();
				PrintWriter testOutputUntaggedPrinter = new PrintWriter(outputDir + rootName + "Assembled2Gram" + ".train");
				for(int t=0; t<outputSents.size(); t++)
				{
					//System.out.println(inputSents.get(t));
					ArrayList tmpList = (ArrayList) outputSents.get(t);
					//System.out.println(tmpList.get(0));
					//System.out.println(tmpList.get(1));
					//System.out.println(tmpList.get(2));
					
					ArrayList tmpTmpList = (ArrayList) tmpList.get(2);
					
					if(!tmpTmpList.isEmpty())
					{
						if(corpusCounts.containsKey(tmpTmpList.get(0)))
						{
							corpusCounts.put(tmpTmpList.get(0), (long)corpusCounts.get(tmpTmpList.get(0)) + 1);
						}
						else
						{
							corpusCounts.put(tmpTmpList.get(0), (long)1);
						}
						HashMap outputWordMap = (HashMap) sentOutputMap.get(tmpTmpList);
						//System.out.println(outputWordMap);
						for(int a=0; a<outputWordMap.size(); a++)
						{
							ArrayList outputWordList = (ArrayList) outputWordMap.get(a);
							//System.out.println(outputWordList);
							String outputWord = ((String) outputWordList.get(0)).split("\\s")[1];
							String outputPOS = ((String) outputWordList.get(1)).split("\\s")[1];
							testOutputUntaggedPrinter.print(outputWord + "\t" + outputPOS + "\n");
						}
						testOutputUntaggedPrinter.print("\n");
						//System.out.println((HashMap) ((HashMap) sentValMap.get(tmpTmpList.get(0))).get(tmpTmpList.get(1)));
						//tmpMap.get(tmpTmpList.get(1));
						//((HashMap)sentValMap.get(tmpTmpList.get(0))).get(tmpTmpList.get(1)
					}
					//System.out.println();
				}
				
				HashMap firstChoiceCorpusCounts = new HashMap();
				Iterator firstChoiceCorpusIter = firstChoiceMap.entrySet().iterator();
				while(firstChoiceCorpusIter.hasNext())
				{
					Entry myEntry = (Entry) firstChoiceCorpusIter.next();
					ArrayList myEntryID = (ArrayList) myEntry.getKey();
					//System.out.println(myEntryID);
					if(!myEntryID.isEmpty())
					{
						String corpusName = (String) myEntryID.get(0);
						if(firstChoiceCorpusCounts.containsKey(corpusName))
						{
							firstChoiceCorpusCounts.put(corpusName, (int)firstChoiceCorpusCounts.get(corpusName) + (int)myEntry.getValue());
						}
						else
						{
							firstChoiceCorpusCounts.put(corpusName, (int)myEntry.getValue());
						}
					}
				}
				
				System.out.println("Sent count: " + sentCount);
				
				System.out.println("First choices: " + firstChoiceCorpusCounts);
				
				System.out.println(corpusCounts);
				System.out.println();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			beginningFileTime = System.currentTimeMillis() - beginningFileTime;
			System.out.println("Done in " + beginningFileTime);
		}
	}

}
