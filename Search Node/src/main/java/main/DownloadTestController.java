package main;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DownloadTestController {

	@RequestMapping("/test")
	public void getFile( HttpServletResponse response)
	{
		System.out.println("SomeCode");
		try{
			
			InputStream is = new FileInputStream("/home/sanket/Experiments/SearchEngineProject/DataText/37921.txt");
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
			
		}
		catch(Exception e){
			
			e.printStackTrace();
			
		}
	}
	
}
