package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		sc.close();
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException 
	{
		HashMap<String,Occurrence> docHash = new HashMap<String,Occurrence>();
		Scanner scan = new Scanner(new File(docFile));
		
		while (scan.hasNext()) 
		{
			
			String line = scan.nextLine();
			
			if (!line.trim().isEmpty() && !(line == null))
			{	
				String[] token = line.split(" "); 
				
				for (int i = 0; i < token.length; i++)
				{
					String word = getKeyWord(token[i]);
					
					if (word != null) 
					{
						if (docHash.containsKey(word))
						{
							Occurrence temp = docHash.get(word);
							temp.frequency++; 
							docHash.put(word, temp); 
						}
						
						else
						{
							Occurrence occurrence = new Occurrence (docFile, 1); 
							docHash.put(word, occurrence); 
						}
					}		
				}
			}
		}
		scan.close();
		return docHash; 		
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) 
	{
		Iterator<String> iterator = kws.keySet().iterator();
		int total; 
		
		while(iterator.hasNext()) 
		{
			total = 0; 
			String key = iterator.next();
			Occurrence occurrence = kws.get(key);
			
			if (keywordsIndex.containsKey(key))
			{
				ArrayList<Occurrence> array = keywordsIndex.get(key); 
				array.add(occurrence); 
				
				ArrayList<Integer> result = insertLastOccurrence(array);
				
				ArrayList<Occurrence> updated = new ArrayList<Occurrence>(); 
				
				for (int i = 0; i < array.size()-1; i++)
				{
					updated.add(array.get(i)); 
				}
				
				if (updated.size() == 1)
				{
					if (updated.get(0).frequency > occurrence.frequency)
					{
						updated.add(occurrence); 	
					}
					
					else 
					{
						updated.add(result.get(result.size()-1), occurrence);
					}
				}
				
				else if (result.get(result.size()-1) == updated.size()-1)
				{
					if (occurrence.frequency <= updated.get(updated.size()-1).frequency)
					{
						updated.add(occurrence); 	
					}
					
					else
					{
						updated.add(result.get(result.size()-1), occurrence);
					}
				}
				
				else if (result.get(result.size()-1) == 0)
				{
					if (occurrence.frequency >= updated.get(0).frequency)
					{
						total = 1; 
						ArrayList<Occurrence> temp = new ArrayList<Occurrence>(); 
						temp.add(occurrence); 
						temp.addAll(updated); 
						keywordsIndex.put(key, temp);
					}
					
					else
					{
						updated.add(1, occurrence);
					}
				}
				
				else
				{
					updated.add(result.get(result.size()-1), occurrence); 
				}
				
				if (total != 1)
				{
					keywordsIndex.put(key, updated); 
				}
			}
			
			else
			{
				ArrayList<Occurrence> ins = new ArrayList<Occurrence>();
				ins.add(occurrence); 
				keywordsIndex.put(key, ins);
			}
				 
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word)
	{ 
		String bef = "";
		String mid = ""; 
		String aft = ""; 
		int i, j, k;
		
		if (checkPunct(word)) 
		{	
			for (i = 0; i < word.length(); i++)
			{
				if (!Character.isLetter(word.charAt(i)))
					break;  
				else
					bef = bef + word.charAt(i); 
			}
			
			for (j = i; j < word.length(); j++)
			{
				if (Character.isLetter(word.charAt(j)))
					break; 
				else
					mid = mid + word.charAt(j);
			}
			
			for (k = j; k < word.length(); k++)
				aft = aft + word.charAt(k);
			
		if (aft.isEmpty())
		{
			if (noiseWords.containsKey(bef.toLowerCase()))
					return null;
				else
				{
					if (!bef.trim().isEmpty())
						return bef.toLowerCase();
					else
						return null; 
				}
			}
			else 
			{
				return null; 
			}
		}
		
		else
		{
			if (noiseWords.containsKey(word.toLowerCase()))
			{
				return null;
			}
			else
			{
				if (!word.trim().isEmpty())
				{
					return word.toLowerCase();
				}
				else
				{
					return null;  
				}
			}
		}
		
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
	}
	private static boolean checkPunct(String word)
	{
		for (int i = 0; i < word.length(); i++)
		{
			if (!Character.isLetter(word.charAt(i)))
			{
				return true; 
			}
		}
		
		return false; 
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occurrences)
	{
		ArrayList<Integer> arrayint = new ArrayList<Integer>(); 
		
		for (int i = 0; i < occurrences.size()-1; i++)
		{
			arrayint.add(occurrences.get(i).frequency); 
		}
		
		int val = occurrences.get(occurrences.size()-1).frequency; 
		
		ArrayList<Integer> result = bnrySrch(arrayint, val, 0, arrayint.size()-1); 
		
		
		// COMPLETE THIS METHOD
		// THE FOLLOWING LINE HAS BEEN ADDED TO MAKE THE METHOD COMPILE
		return result;
	}
	
	private ArrayList<Integer> bnrySrch(ArrayList<Integer> arraylist, int key, int min, int max)
	{
		ArrayList<Integer> mids = new ArrayList<Integer>(); 
	  
		while (max >= min)
		{
			int mid = (min + max) / 2;
	      
			mids.add(mid); 
	      
			if (arraylist.get(mid) <  key)
			{
				max = mid - 1;
			}
	      
			else if (arraylist.get(mid) > key )
			{
				min = mid + 1;
			}
	      
			else
			{
				break; 
			}
		}
	  
		return mids; 
	}

	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) 
	{
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Occurrence> arraylist1 = keywordsIndex.get(kw1);
		ArrayList<Occurrence> arraylist2 = keywordsIndex.get(kw2);
		
		int i = 0;
		int j = 0; 
		int total = 0; 
		
		if (arraylist1 == null && arraylist2 == null)
		{
			return result; 
		}
		
		else if (arraylist1 == null)
		{
			while (j < arraylist2.size() && total < 5)
			{
				result.add(arraylist2.get(j).document); 
				j++; 
				total++; 
			}
			
		}
		
		else if (arraylist2 == null)
		{
			while (i < arraylist1.size() && total < 5)
			{
				result.add(arraylist1.get(i).document); 
				i++; 
				total++; 
			}
		}
	
		else 
		{	
			while ((i < arraylist1.size() || j < arraylist2.size()) && total < 5) 
			{
				if (arraylist1.get(i).frequency > arraylist2.get(j).frequency && (!result.contains(arraylist1.get(i).document))) 
				{
					result.add(arraylist1.get(i).document); 
					i++;
					total++; 
				}
				
				else if (arraylist1.get(i).frequency < arraylist2.get(j).frequency && (!result.contains(arraylist2.get(j).document)))
				{
					result.add(arraylist2.get(j).document); 
					j++;
					total++; 
				}
				
				else
				{
					if (!result.contains(arraylist1.get(i).document))
					{
						result.add(arraylist1.get(i).document);
						total++; 
						i++;
					}
					
					else
					{
						i++; 
					}
					
					if ((!result.contains(arraylist2.get(j).document)))
					{
						if (total < 5)
						{
							result.add(arraylist2.get(j).document); 
							j++;
							total++; 
						}
					}
					
					else 
					{
						j++; 
					}
				}
			}
		}
		
		return result;
	}
}