package base;
import java.util.HashMap;
import java.util.Map;


public class EncapsulatedIncomingResult {

	public String ShardLink;
	
	public Map<String, String[]> map;
	
	
	public EncapsulatedIncomingResult()
	{
		map= new HashMap<String, String[]>();
	}
	
	
	public void insert(String key, String[] value)
	{
		
		map.put(key, value);
		
	}

}
