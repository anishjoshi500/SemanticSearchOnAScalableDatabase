package base;
import java.util.HashMap;
import java.util.Map;
public class EncapsulatedResult {
	Map<String, String[]> map;
	String ShardLink;
	
	public EncapsulatedResult()
	{
		map= new HashMap<String, String[]>();
	}
	
	
	public void insert(String key, String[] value)
	{
		
		map.put(key, value);
		
	}

}
