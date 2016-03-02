package searchhead;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.Scanner;
import base.Shard;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import base.EncapsulatedIncomingResult;
import base.SearchResultDocument;

import com.google.gson.Gson;


public class DistributedSearch {
	public static Integer TotalShards;
	public static Integer Results;
	public static List<EncapsulatedIncomingResult> Resultlist;
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/DistributedSearch";
	//  Database credentials
	static final String USER = "root";
	static final String PASS = "password";
	public static void main(String[] args) throws Exception
	{
		System.out.println("Enter query");
		Scanner sc = new Scanner(System.in);
		String query = sc.nextLine();
		Map<String, SearchResultDocument> result = getResults(query);
		EncapsulatedIncomingResult encapres = new EncapsulatedIncomingResult();
		Integer i=1;
		for (Entry<String, SearchResultDocument> entry : result.entrySet()) {
		    encapres.map.put(i.toString(), new String[] {entry.getValue().getFilename(),entry.getValue().getScore(),entry.getValue().getHighlight(),entry.getValue().getShardLink()});
		    i++;
		}
		Gson gson = new Gson();
		String resultString = gson.toJson(result);
		System.out.println(resultString);
		
	}

	public static Map<String, SearchResultDocument> getResults(String query)
	{
		
		long start = new Date().getTime();
		boolean suggestion=false;
		
		Resultlist=getList(query);
		TotalShards=3;
		Results=10;
		Double testmatrix[][] = new Double[TotalShards][Results];
		SearchResultDocument results[][] = new SearchResultDocument[TotalShards][Results];
		initialize(testmatrix,results);
		int i=0;
		for(EncapsulatedIncomingResult s : Resultlist)
		{
			printmap(s.map,testmatrix[i],results[i],s.ShardLink);
			System.out.println("Result Size"+s.map.size());
			i++;
		}
		printmatrix(testmatrix);
		Map<String, SearchResultDocument> finalresultmap = topalgo(testmatrix,results);
		
		long end = new Date().getTime();
		System.out.println("Time Taken:"+ (end - start) + " milliseconds");
		return finalresultmap;
	}
	
	private static List<EncapsulatedIncomingResult> getList(String query) {
		HttpClient httpClient = new DefaultHttpClient();
		Resultlist = new ArrayList<EncapsulatedIncomingResult>();
		List<Shard> shardlist = getShardList();
		try{
			for(int i=0;i<shardlist.size();i++)
			{
				query=URLEncoder.encode(query,"UTF-8");
				String url="http://"+shardlist.get(i).getLink()+"/search?query="+query;
				HttpGet request = new HttpGet(url);
				Gson gson = new Gson();
			String USER_AGENT = "Mozilla/5.0";
			// add request header
					request.addHeader("User-Agent", USER_AGENT);
					HttpResponse response = httpClient.execute(request);
					System.out.println("\nSending 'GET' request to URL : " + url);
					System.out.println("Response Code : " + 
			                       response.getStatusLine().getStatusCode());
					BufferedReader rd = new BufferedReader(
			                       new InputStreamReader(response.getEntity().getContent()));
					StringBuffer result = new StringBuffer();
					String line = "";
					while ((line = rd.readLine()) != null) {
						result.append(line);
					}
					
					try{
					String jsonList = result.toString();
					EncapsulatedIncomingResult resultret= gson.fromJson(jsonList, EncapsulatedIncomingResult.class);
					resultret.ShardLink=shardlist.get(i).getLink();
					Resultlist.add(resultret);
					}
					catch(Exception e)
					{
						System.out.println("Error encountered in Result JSON Conversion");
					}
			}
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
		return Resultlist;
	}

	private static List<Shard> getShardList() {
		

		List<Shard> shardList = new ArrayList<Shard>();
		Connection conn = null;
		   Statement stmt = null;
		   try{
		      Class.forName("com.mysql.jdbc.Driver");

		      System.out.println("Connecting to database...");
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);

		      System.out.println("Creating statement...");
		      stmt = conn.createStatement();
		      String sql;
		      sql = "SELECT * FROM ShardList";
		      ResultSet rs = stmt.executeQuery(sql);

		      while(rs.next()){
		         int id  = rs.getInt("id");
		         int storage = rs.getInt("storage");
		         int rating = rs.getInt("rating");
		         String link = rs.getString("JDBClink");

		         Shard temp = new Shard();
		         temp.setId(id);
		         temp.setLink(link);
		         temp.setRating(rating);
		         temp.setStorage(storage);
		         
		         shardList.add(temp);
		         //Display values
		         System.out.print("ID: " + id);
		         System.out.print(", Storage: " + storage);
		         System.out.print(", Rating: " + rating);
		         System.out.println(", Link: " + link);
		      }
		      rs.close();
		      stmt.close();
		      conn.close();
		   }catch(SQLException se){
		      se.printStackTrace();
		   }catch(Exception e){
		      e.printStackTrace();
		   }finally{
		      try{
		         if(stmt!=null)
		            stmt.close();
		      }catch(SQLException se2){
		      }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
		   System.out.println("Goodbye!");	
		
		return shardList;
	}

	private static void initialize(Double[][] testmatrix, SearchResultDocument[][] results) {
		for (int i =0;i<TotalShards;i++)
		{
			for (int j =0;j<Results;j++)
			{
				testmatrix[i][j]=0.0000;
				results[i][j]= new SearchResultDocument();
			}
		}
	}

	private static void printmap(Map<String, String[]> map, Double[] testmatrix, SearchResultDocument[] results2, String shardLink) {
		int i=0;
		for (Entry<String, String[]> entry : map.entrySet()) {
	    System.out.println("FileName = " + entry.getValue()[0] + ", Score = " + entry.getValue()[1]+", Highlight= "+entry.getValue()[2]);
	    testmatrix[i]=Double.parseDouble(entry.getValue()[1]);
	    results2[i].setFilename(entry.getValue()[0]);
	    results2[i].setScore((entry.getValue()[1]));
	    results2[i].setHighlight(entry.getValue()[2]);
	    results2[i].setShardLink(shardLink);
	    i++;
		}
	}
	static public void topalgotest()
	{

	}
	private static Map<String, SearchResultDocument> topalgo(Double[][] testmatrix, SearchResultDocument[][] results) {
		Integer[] mainptr= new Integer[2];
		mainptr[0]=0;
		mainptr[1]=0;
		Integer[][] shardptrlist = new Integer[TotalShards][2];
		for(int i=0;i<TotalShards;i++)
		{
			shardptrlist[i][0]=i;
			shardptrlist[i][1]=0;
		}
		List<Integer[]> list = new ArrayList<Integer[]>();
		Map<String,SearchResultDocument> docmap = new LinkedHashMap<String,SearchResultDocument>();
		int tries=TotalShards;
		while(list.size()<=Results+20)
		{
			//System.out.println( "Value of the mainptr before iterate down: ("+mainptr[0]+","+mainptr[1]+") : "+testmatrix[mainptr[0]][mainptr[1]]);
			if( iteratedown(mainptr,shardptrlist,testmatrix))
			{
			list.add(new Integer[]{ mainptr[0], mainptr[1]});
			SearchResultDocument temp = new SearchResultDocument();
			temp.setFilename(results[mainptr[0]][mainptr[1]].getFilename());
			temp.setScore(testmatrix[mainptr[0]][mainptr[1]].toString());
			temp.setHighlight(results[mainptr[0]][mainptr[1]].getHighlight());
			temp.setShardLink(results[mainptr[0]][mainptr[1]].getShardLink());
			docmap.put(results[mainptr[0]][mainptr[1]].getFilename(), temp);
			shiftright(mainptr);
			shiftshardptrmainright(mainptr, shardptrlist);
			}
			printshardlist(shardptrlist);	
		}
		///Insert Map Display Code
		System.out.println("Displaying the results found");
		for (Entry<String, SearchResultDocument> entry : docmap.entrySet()) {
			if (entry.getValue().getFilename()!=null)
			System.out.println("FileName = " + entry.getValue().getFilename() + ", Score = " + entry.getValue().getScore()+", Highlight= "+entry.getValue().getHighlight()+", ShardLink= "+entry.getValue().getShardLink());
			else
			{
				docmap.remove(entry.getKey());
			}
		
		}
		return docmap;
	}
	private static void shiftshardptrmainright(Integer[] mainptr,
			Integer[][] shardptrlist) {
	if((shardptrlist[mainptr[0]][1]+1)<Results)	
		shardptrlist[mainptr[0]][1]++;
	
	}
	private static void printshardlist(Integer[][] shardptrlist) {
	
		for (int i =0;i<TotalShards;i++)
		{
			for (int j =0;j<2;j++)
			{
				System.out.print(shardptrlist[i][j]+" ");
			}
			System.out.println();
			
		}
	}
	private static void shiftright(Integer[] ptr) {
	
		if((ptr[1]+1)<Results)
			ptr[1]++;
		
	}
	private static boolean iteratedown(Integer[] mainptr, Integer[][] shardptrlist,Double[][] testmatrix) {
		
		int i;
		boolean flag=false;
		for( i =0;i<shardptrlist.length;i++)
		{int result=compare(mainptr, shardptrlist[i], testmatrix);
			
			if(result>=0)
			{
				flag=true;
				
			}
			else
			{
				flag=false;
				break;
			}
		}
		if(flag==true)
		{
			return true;
		}
		else
		{
			mainptr[0]=shardptrlist[i][0];
			mainptr[1]=shardptrlist[i][1];
			return false;
		}
		
	}
	private static Integer compare(Integer[] firstptr, Integer[] secondptr, Double[][] testmatrix) {
		
		
		
		if(testmatrix[firstptr[0]][firstptr[1]]>testmatrix[secondptr[0]][secondptr[1]])
		{
			return 1;
		}
		else if (testmatrix[firstptr[0]][firstptr[1]]<testmatrix[secondptr[0]][secondptr[1]])
		{
			return -1;
			
		}
		else
		{
			return 0;
		}
		
	}
	private static void printmatrix(Double[][] testmatrix) {
	
		for (int i =0;i<TotalShards;i++)
		{
			for (int j =0;j<Results;j++)
			{
				System.out.print(testmatrix[i][j]+" ");
			}
			System.out.println();
			
		}
	}
	private static void populatematrix(Double[][] testmatrix,boolean Random) {
		if(Random)
		{
		for (int i =0;i<TotalShards;i++)
		{
			testmatrix[i][0]=new Double(new Random().nextDouble());
			for (int j =1;j<TotalShards;j++)
			{
				testmatrix[i][j]=testmatrix[i][j-1]-2;
			}
		}
		}
		else
		{
			testmatrix[0][0]=9.0;
			testmatrix[0][1]=8.0;
			testmatrix[0][2]=7.0;
			testmatrix[1][0]=8.5;
			testmatrix[1][1]=7.3;
			testmatrix[1][2]=6.0;
			testmatrix[2][0]=8.7;
			testmatrix[2][1]=7.3;
			testmatrix[2][2]=2.5;
		}
	}
}