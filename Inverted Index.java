import java.io.*;
import java.util.*;
import java.net.URLConnection;
public class Assignment2
{
	public static void main(String args[]) throws Exception
	{
		Scanner input = new Scanner(System.in);
		System.out.print("Enter the path to the directory: ");
		String path = input.nextLine();
		System.out.println();
		final File folder = new File(path);
		if(!folder.isDirectory())
			throw new java.lang.RuntimeException("Please enter a valid Directory name");
		/*boolean hasDirectories = checkForSubDirectories(folder);
		boolean considerSubDirectories = false;
		if(hasDirectories)
		{
			System.out.print("There are few subdirectories! Do you want to consider them?[Y/n]: ");
			String query = input.nextLine();
			if(query.equals("Y"))
				considerSubDirectories=true;
		}
		*/
		//HashMap<String,HashMap<Integer,Set<Integer>>> hm=new HashMap<String,HashMap<Integer,Set<Integer>>>();
		HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex = new HashMap<String,HashMap<Integer,Set<Integer>>>();;
		String displayIDs = buildInvertedIndex(folder,InvertedIndex,path);
		HashMap<Integer,String> displayMap = buildMap(displayIDs);
		String displayInvertedIndexes = displayInvertedIndex(InvertedIndex);
		String op;
		do
		{
			System.out.println("Choose from following:\n");
			System.out.println("(1) Display Document IDs\n");
			System.out.println("(2) Display InvertedIndex\n");
			System.out.println("(3) Query\n");
			//System.out.println("");
			
			int choice = input.nextInt();
			input.nextLine();
			System.out.println();
			switch(choice)
			{
				case 1 : 
					System.out.println(displayIDs);
					break;
				case 2 :
					System.out.println(displayInvertedIndexes);
					break;
				case 3 :
					System.out.println("What is your Query?");
					System.out.println();
					String query = input.nextLine();
					System.out.println();
					Query(query,InvertedIndex,displayMap);
					
					break;
				default :
					System.out.println("Invalid Case selected!");
					break;
			}
			System.out.print("\nDo you want to continue?[Y/n]: ");
			op = input.next();
			System.out.println();
		}
		while(op.equals("Y"));
	}
	
	public static void Query(String query, HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex,HashMap<Integer,String> displayMap)
	{
		Set<Integer> ans = userQuery(query,InvertedIndex);
		if(ans==null || ans.size() == 0)
			System.out.println("Query not found!");
		else
		{
			System.out.print("Query,\""+query+"\" occurs at Document ID(s) -\n");
			for(Integer i : ans)
			{
				System.out.println(i+": "+displayMap.get(i));
			}
			System.out.println();
		}
	}
	
	public static Set<Integer> userQuery(String query, HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex)
	{
		Set<Integer> ans = null;
		String[] words = query.split(" ");
		if(words.length == 1)
		{
			ans = getSetWord(words[0].toLowerCase(),InvertedIndex);
		}
		else if(words.length == 3 && query.contains("/"))
		{
			words[0] = words[0].toLowerCase();
			words[2] = words[2].toLowerCase();
			String number = words[1].replaceAll("/","");
			int distance = Integer.parseInt(number);
			ans = getSetWithin(words,InvertedIndex,distance);
		}
		else if(words.length > 1 )
		{
			if(query.contains(" AND "))
			{
				String[] s = query.split(" AND ");
				String[] s1 = s[0].toLowerCase().split(" ");
				String[] s2 = s[1].toLowerCase().split(" ");
				if(s1.length>1)
					ans = getSetPhrase(s1,InvertedIndex);
				else 
					ans = getSetWord(s1[0],InvertedIndex);
				if(s2.length>1)
					ans.retainAll(getSetPhrase(s2,InvertedIndex));
				else
					ans.retainAll(getSetWord(s2[0],InvertedIndex));
			}
			else if(query.contains(" OR "))
			{
				String[] s = query.split(" OR ");
				String[] s1 = s[0].toLowerCase().split(" ");
				String[] s2 = s[1].toLowerCase().split(" ");
				if(s1.length>1)
					ans = getSetPhrase(s1,InvertedIndex);
				else 
					ans = getSetWord(s1[0],InvertedIndex);
				if(s2.length>1)
					ans.addAll(getSetPhrase(s2,InvertedIndex));
				else
					ans.addAll(getSetWord(s2[0],InvertedIndex));
			}
			else
			{
				words = query.toLowerCase().split(" ");
				ans = getSetPhrase(words,InvertedIndex);
			}
		}
		else
		{
			return null;
		}
		return ans;
	}
	
	public static Set<Integer> getSetWord(String query,HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex)
	{
		Set<Integer> ans = new HashSet<Integer>();
		HashMap<Integer,Set<Integer>> wordMap = InvertedIndex.get(query);
		if(wordMap == null)
			return null;
		for(Integer i : wordMap.keySet())
			ans.add(i);
		return ans;
	}
	
	public static Set<Integer> getSetPhrase(String[] words,HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex)
	{
		Set<Integer> ans = new HashSet<Integer>();
		List<Set<Integer>> lists = new ArrayList<Set<Integer>>();
		List<HashMap<Integer,Set<Integer>>> wordMaps = new ArrayList<HashMap<Integer,Set<Integer>>>();
		int i=0;
		for(String word : words)
		{
			wordMaps.add(InvertedIndex.get(words[i]));
			if(wordMaps.get(i)==null)
				return null;
			i++;
		}
		Set<Integer> temp = new HashSet<Integer>();
		HashMap<Integer,Set<Integer>> word1 = wordMaps.get(0);
		HashMap<Integer,Set<Integer>> word2 = wordMaps.get(1);
		for(Integer j : word1.keySet() ) //get the docids of word1
		{
			Set<Integer> index2 = word2.get(j); //check if it exists
			if(index2!=null) //docids are same
			{
				Set<Integer> index1 = word1.get(j); //get the occ of word1
				for(Integer k : index1)
				{
					if(index2.contains(k+1)) //check if word2 occs at k + 1 pos
					{
						int z;
						boolean flag=true;
						for(z=2;z<words.length;z++)
						{
							HashMap<Integer,Set<Integer>> temp1 = wordMaps.get(z);
							Set<Integer> tindex1 = temp1.get(j);
							if(tindex1==null)
							{
								flag = false;
								break;
							}
							else
							{
								if(!tindex1.contains(k+z))
								{
									flag = false;
									break;
								}
							}
							
						}
						if(flag == true )
						{
							ans.add(j);
							break;
						}
					}
				}
			}
		}
		return ans;
	}
	
	public static Set<Integer> getSetWithin(String[] words,HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex,int distance)
	{
		Set<Integer> ans = new HashSet<Integer>();
		List<HashMap<Integer,Set<Integer>>> wordMaps = new ArrayList<HashMap<Integer,Set<Integer>>>();
		int i=0;
		wordMaps.add(InvertedIndex.get(words[0]));
		if(wordMaps.get(0)==null)
			return null;
		wordMaps.add(InvertedIndex.get(words[2]));
		if(wordMaps.get(1)==null)
			return null;
		HashMap<Integer,Set<Integer>> word1 = wordMaps.get(0);
		HashMap<Integer,Set<Integer>> word2 = wordMaps.get(1);
		for(Integer j : word1.keySet() ) //get the docids of word1
		{
			Set<Integer> index2 = word2.get(j); //check if it exists
			if(index2!=null) //docids are same
			{
				Set<Integer> index1 = word1.get(j); //get the occ of word1
				for(Integer k : index1)
				{
					int z;
					for(z = distance * -1; z <= distance; z++)
					{
						if(index2.contains(k+z)) //check if word2 occs at k + 1 pos
						{
							ans.add(j);
							break;
						}
					}
				}
			}
		}
		return ans;
	}
	
	public static String displayInvertedIndex(HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex)
	{
		String s = "";
		for(String word : InvertedIndex.keySet())
		{
			s+=(word+": {");
			HashMap<Integer,Set<Integer>> wordMap = InvertedIndex.get(word);
			for(Integer i : wordMap.keySet())
			{
				s+=(""+i+", {");
				Set<Integer> wordList = wordMap.get(i);
				for(Integer j : wordList)
				{
					s+=(""+j+",");
				}
				s = s.substring(0,s.length() - 1);
				s+=("} ");
			}
			s+=("}\n");
		}
		return s;
	}
	public static String buildInvertedIndex(final File folder, HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex, String path) 
	{
		path+="/";
		int docid=1;
		String display = "Document Name:Document Id\n";
		for (final File fileEntry : folder.listFiles()) 
		{
			if (fileEntry.isDirectory()) 
			{
				continue;
			} 
			else 
			{
				insertIntoMap(fileEntry.getName(),InvertedIndex,docid,path);
				display+=(fileEntry.getName() + ":" + docid++ + "\n");
			}
		}
		return display;
	}
	public static void insertIntoMap(String file, HashMap<String,HashMap<Integer,Set<Integer>>> InvertedIndex,int docid,String path)
	{
		String text = "";
		try(BufferedReader br = new BufferedReader(new FileReader(path+file)))
		{
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) 
			{
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			text = sb.toString().toLowerCase();
		} 
		catch(Exception e)
		{
			System.out.println(e);
		}
		
		text = text.replaceAll("[-+.^:,]","");
		text = text.trim().replaceAll("(\r?\n\r?)+", "\n");
		text = text.trim().replaceAll(" +", " ");
		String[] words = text.split("[ -\"\'\n\t\r.,;:!?(){]");
		int wordIndex = 0;
		for(String word : words)
		{
			if(word.length()>0)
			{
				HashMap<Integer,Set<Integer>> wordMap = InvertedIndex.get(word);
				if(wordMap == null)
				{
					wordMap = new HashMap<Integer,Set<Integer>>();
					Set<Integer> wordList = wordMap.get(docid);
					if(wordList == null)
					{
						wordList = new HashSet<Integer>();
						wordList.add(wordIndex++);
					}
					else
					{
						wordList.add(wordIndex++);
					}
					wordMap.put(docid,wordList);
				}
				else
				{
					Set<Integer> wordList = wordMap.get(docid);
					if(wordList == null)
					{
						wordList = new HashSet<Integer>();
						wordList.add(wordIndex++);
					}
					else
					{
						wordList.add(wordIndex++);
					}
					wordMap.put(docid,wordList);
				}
				InvertedIndex.put(word,wordMap);
			}
		}
	}
	public static HashMap<Integer,String> buildMap(String str)
	{
		HashMap<Integer,String> ans = new HashMap<Integer,String>();
		String[] words = str.split("\n");
		int i;
		for(i=1;i<words.length;i++)
		{
			String[] temp = words[i].split(":");
			ans.put(Integer.parseInt(temp[1]),temp[0]);
		}
		return ans;
	}
}
