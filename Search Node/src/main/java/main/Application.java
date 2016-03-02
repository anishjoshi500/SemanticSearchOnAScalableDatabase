package main;
import index.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.lucene.search.Searcher;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import base.Config;

import com.google.gson.Gson;
@ComponentScan
@EnableAutoConfiguration
public class Application {
	private static String indexdir;
	private static String datadir;
	private static String model;
	public static void main(String[] args) throws Exception
	{
		Scanner sc = new Scanner(System.in);
		int flag=0;
		System.out.println("Select Port to start application on:");
		String port = sc.nextLine();
		System.setProperty("server.port",port);
		
		Config config= new Config();
		config.configmap= new HashMap<String, String>();
		int choice =1;
		 if(choice==1)
		{
			System.out.println("Enter the current link ip address along with port no to launch on:");
			String link= sc.nextLine();
			link=link+":"+port;
			config.configmap.put("link", link);
			System.out.println("Enter the storage value:");
			String storage= sc.nextLine();
			config.configmap.put("storage", storage);
			System.out.println("Enter the performance rating");
			String rating= sc.nextLine();
			config.configmap.put("rating", rating);
			System.out.println("Enter the URL of the search head");
			String url= sc.nextLine();
			config.configmap.put("searchhead", url);
			System.out.println("Enter the index directory");
			indexdir= sc.nextLine();
			config.configmap.put("indexdir", indexdir);
			System.out.println("Enter the data directory");
			datadir= sc.nextLine();
			config.configmap.put("datadir", datadir);
			System.out.println("Enter the model file");
			model= sc.nextLine();
			config.configmap.put("model", model);
			System.out.println("Enter the temp directory");
			String tempdir= sc.nextLine();
			config.configmap.put("tempdir", tempdir);
			
			Gson gson = new Gson();
			String configstring= gson.toJson(config);
			try {
				FileWriter writer = new FileWriter("config.json");
				writer.write(configstring);
				writer.close();
		 
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
				try{
				makeRequest(link,storage,rating,url);
				}
				catch(Exception e)
				{
					System.out.println("Problem in making request to search head");
					e.printStackTrace();
					
				}
				System.out.println("Selection\n1.Create an Index\n2.Create a Search \n3.Exit");
				choice=sc.nextInt();
				while(choice!=3)
				{if(choice==1)
				{
					System.out.println("Please Enter the Index Directory Path:");
					String indexpath=sc.next();
					File index = new File(indexpath);
					System.out.println("Please Enter the Data Directory Path:");
					String datapath=sc.next();
					System.out.println("Please Enter the Model Directory Path:");
					String modelpath=sc.next();
					File data= new File(datapath);
					try {
						Indexer.index(index, data, modelpath);
					} catch (Exception e) {
						e.printStackTrace();
			   		}
				}
				else if (choice==2){
					
					System.out.println("Launching Server..............");
					flag=1;
					break;
				}
				System.out.println("Selection\n1.Create an Index\n2.Create a Search \n3.Exit");
				choice=sc.nextInt();
				}
		}
		if(flag==1)
		{
			try {
				FileUtils.cleanDirectory(new File(indexdir));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Indexer.index(new File(indexdir), new File(datadir), model);
			IndexThread thread = new IndexThread();
			thread.start();
			SpringApplication.run(Application.class, args);
		}
	}
	private static void makeRequest(String link, String storage,
			String rating, String url) throws ClientProtocolException, IOException {
		
		HttpClient client = new DefaultHttpClient();
		url="http://"+url+"/SemanticSearchHead/RegisterShard?storage="+storage+"&rating="+rating+"&link="+link;
		HttpGet request = new HttpGet(url);
		// add request header
		request.addHeader("User-Agent", "Mozilla/5.0");
		HttpResponse response = client.execute(request);
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
		System.out.println(result.toString());
	}
}	