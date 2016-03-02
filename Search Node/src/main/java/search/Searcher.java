package search;

import nl.rug.eco.lucene.EnglishLemmaAnalyzer;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.File;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Searcher {

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the query");
		File indexDir = new File("/home/sanket/Experiments/SearchEngineProject/DistributedSearch/SubNode1/index");
		String q = sc.nextLine();
		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new Exception(indexDir
					+ " does not exist or is not a directory.");
		}
		search(indexDir, q, "/home/sanket/Downloads/stanford-postagger-2014-10-26/models/english-left3words-distsim.tagger");
	}
	
	public static Map<String, String[]> getResults(String query, String pathname, String modelpath) throws Exception 
	{
	
		if(pathname==null)
			pathname = "/home/sanket/Experiments/SearchEngineProject/GenIndex";
		
		File indexDir = new File(pathname);

		if (!indexDir.exists() || !indexDir.isDirectory()) {
			throw new Exception(indexDir+ " does not exist or is not a directory.");
		}
		else{
			return search(indexDir, query, modelpath);
		}
	}
	
	public static Map<String, String[]> search(File indexDir, String q, String modelpath) throws Exception {
		
		///Store results in an ArrayList
		Map<String,String[]> map = new HashMap<String, String[]>();
		///
		Directory fsDir = FSDirectory.open(indexDir);
		IndexSearcher searcher = new IndexSearcher(fsDir);

		//Analyzer analyzer = new EnglishLemmaAnalyzer("/home/sanket/Downloads/stanford-postagger-2014-10-26/models/english-bidirectional-distsim.tagger");	
		//Query query = MultiFieldQueryParser.parse(null, q,new String[]{"title", "subject","contents","filename","body"},null, new StandardAnalyzer(Version.LUCENE_CURRENT));
		long start = new Date().getTime();
		//Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_CURRENT, "English");
		Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_CURRENT);
		//Analyzer analyzer = new EnglishLemmaAnalyzer(modelpath);
		QueryParser qparser = new QueryParser(Version.LUCENE_CURRENT, "content", analyzer);
		
		Map<String,Float> boostmap = new HashMap<String, Float>();
		boostmap.put("content", new Float(1.0));
		boostmap.put("filename", new Float(3.0));
		boostmap.put("title", new Float(3.0));
		boostmap.put("body", new Float(1.0));
		
		
		
		MultiFieldQueryParser multiparser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, new String[] {"content","filename","title","body"}, analyzer,boostmap);
		
		System.out.println("This is the query:"+q);
		q=removeStopWords(q);
		System.out.println("This is the query after stop word removal:"+q);
		if (!q.equals(""))
		{
			Query query=multiparser.parse(q);
			TopDocs k = searcher.search(query, 10);
			long end = new Date().getTime();
			System.out.println();
			System.out.println();
			QueryScorer scorer = new QueryScorer(query);
			Highlighter highlighter = new Highlighter(scorer);
			System.out.println();
			System.out.println();
			System.err.println("Found " + k.totalHits + " document(s) (in "
					+ (end - start) + " milliseconds) that matched query '" + q
					+ "':");
			ScoreDoc[] hits = k.scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				
				
				Document doc = searcher.doc(hits[i].doc);
				
				Explanation expl = searcher.explain(query, hits[i].doc);
				String asText = expl.toString();
				//System.out.println(asText);
				
				
				String filename=doc.get("filename");
				String highlight=getHiglight(doc,filename,highlighter);
				
				String score= new Double(hits[i].score).toString();
				
				//Highlight code
//				String body = doc.get("body");
//				TokenStream stream =new EnglishAnalyzer(Version.LUCENE_CURRENT).tokenStream("",new StringReader(body));
//				String fragment =
//				highlighter.getBestFragment(stream, body);
//				System.out.println("Found in Body :"+fragment);
//				
//				body = doc.get("title");
//				stream =new EnglishAnalyzer(Version.LUCENE_CURRENT).tokenStream("",new StringReader(body));
//				fragment =highlighter.getBestFragment(stream, body);
//				System.out.println("Found in Title :"+fragment);
//				
//				body = doc.get("filename");
//				stream =new EnglishAnalyzer(Version.LUCENE_CURRENT).tokenStream("",new StringReader(body));
//				fragment =highlighter.getBestFragment(stream, body);
//				System.out.println("Found in FileName :"+fragment);
//				body = doc.get("links");
//				stream =new EnglishAnalyzer(Version.LUCENE_CURRENT).tokenStream("",new StringReader(body));
//				fragment =highlighter.getBestFragment(stream, body);
//				System.out.println("Found in Links :"+fragment);
//				
//				
				
//				String body = doc.get("content");
//				TokenStream stream =new EnglishAnalyzer(Version.LUCENE_CURRENT).tokenStream("",new StringReader(body));
//				String fragment =highlighter.getBestFragment(stream, body);
//				//System.out.println("Found in content :"+fragment);
//				
				System.out.println(doc.get("filename"));
				System.out.println(hits[i].score);
				System.out.println(highlight);
				map.put(new Integer(i+1).toString(), new String[]{ filename,score,highlight});
			}
			return map;
		}
		else
		{
			System.out.println("No Results");
			return null;
		}
		
	}
	
	
	
	
	private static String getHiglight(Document doc, String filename, Highlighter highlighter) {
		
		System.out.println("Filename in consideration:"+filename);
		if (filename.endsWith("html"))
		{
			String bodyhighlight = null;
			String filenamehighlight;
			String titlehighlight=get(doc,"title",highlighter);
			if(titlehighlight==null||titlehighlight.equals(""))
			 {bodyhighlight = get(doc,"body",highlighter);
			 if(bodyhighlight==null||bodyhighlight.equals(""))
				 {filenamehighlight= get(doc,"filename",highlighter);
			 	return filenamehighlight;
				 }
			 else{
				return bodyhighlight;
			}
			 }
			else{
				return titlehighlight;
			}
			
		}
		else if (filename.endsWith("pdf"))
		{
			String bodyhighlight = null;
			String filenamehighlight;
			String titlehighlight=get(doc,"title",highlighter);
			if(titlehighlight==null||titlehighlight.equals(""))
			 {bodyhighlight = get(doc,"body",highlighter);
			 if(bodyhighlight==null||bodyhighlight.equals(""))
				 {filenamehighlight= get(doc,"filename",highlighter);
			 	return filenamehighlight;
				 }
			 else{
				return bodyhighlight;
			}
			 }
			else{
				return titlehighlight;
			}
			
		}
		else if(filename.endsWith("txt"))
		{
			System.out.println("Reached");
			String contenthighlight=get(doc,"content",highlighter);
		
			return contenthighlight;
		}
		else if(filename.endsWith(""))
		{
			System.out.println("Reached");
			String contenthighlight=get(doc,"content",highlighter);
		
			return contenthighlight;
		}
		return null;
	}



	private static String get(Document doc, String field, Highlighter highlighter) {
		try{
		String body = doc.get(field);
		//System.out.println(body);
		TokenStream stream =new EnglishAnalyzer(Version.LUCENE_CURRENT).tokenStream("", new StringReader(body));
		String fragment =highlighter.getBestFragment(stream, body);
		return fragment;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	public static String removeStopWords(String textFile) throws Exception {
	    Set<?> stopWords = EnglishAnalyzer.getDefaultStopSet();
	    TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_CURRENT, new StringReader(textFile.trim()));

	    tokenStream = new StopFilter(Version.LUCENE_CURRENT, tokenStream, stopWords);
	    StringBuilder sb = new StringBuilder();
	    CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
	    tokenStream.reset();
	    while (tokenStream.incrementToken()) {
	        String term = charTermAttribute.toString();
	        sb.append(term + " ");
	    }
	    return sb.toString();
	}
}