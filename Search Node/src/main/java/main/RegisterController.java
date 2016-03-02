package main;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import base.EncapsulatedResult;

import com.google.gson.Gson;

public class RegisterController {
	public static final String template= "Hello , %s!";
	private final AtomicLong counter = new AtomicLong();
	@RequestMapping("/register")
	public String search(@RequestParam(value="query",defaultValue="for") String name) throws Exception
	{
		Map<String, String[]> map = search.Searcher.getResults(name,null,"/home/vagrant/semanticnode/model/english-left3words-distsim.tagger");
		EncapsulatedResult result = printmap(map);
		Gson gson = new Gson();
		String jsonList = gson.toJson(result);
		return jsonList;
	}
	private static EncapsulatedResult printmap(Map<String, String[]> map) {
		EncapsulatedResult result = new EncapsulatedResult();
		for (Entry<String, String[]> entry : map.entrySet()) {
	    System.out.println("FileName = " + entry.getValue()[0] + ", Score = " + entry.getValue()[1]);
	    result.insert(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	
}
