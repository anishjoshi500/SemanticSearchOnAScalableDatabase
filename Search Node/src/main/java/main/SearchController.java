package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import base.Config;
import base.EncapsulatedResult;
import base.SearchResultDoc;

import com.google.gson.Gson;

@RestController
public class SearchController {
	public static final String template= "Hello , %s!";
	private final AtomicLong counter = new AtomicLong();
	public final Gson gson=new Gson();
	public  String modelloc;
	public  String indexloc;
	public SearchController() throws IOException
	{
		
		System.out.println("Some string");
		
		FileReader in = new FileReader("config.json");
		
		BufferedReader br = new BufferedReader(in);
		
		Config config = gson.fromJson(br, Config.class);
		
		
		if(config==null)
		{
			System.out.println("Shit");
		}
		//Modelloc and indexloc initialized
		modelloc = config.configmap.get("model");
		indexloc = config.configmap.get("indexdir");
		
	}
	
	
	@RequestMapping("/search")
	public String search(@RequestParam(value="query",defaultValue="for") String name) throws Exception
	{
		Map<String, String[]> map = search.Searcher.getResults(name,indexloc,modelloc);
		EncapsulatedResult result = printmap(map);
		Gson gson = new Gson();
		String jsonList = gson.toJson(result);
		return jsonList;
	}
	public static EncapsulatedResult printmap(Map<String, String[]> map) {
		EncapsulatedResult result = new EncapsulatedResult();
		for (Entry<String, String[]> entry : map.entrySet()) {
	    System.out.println("FileName = " + entry.getValue()[0] + ", Score = " + entry.getValue()[1]);
	    result.insert(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
