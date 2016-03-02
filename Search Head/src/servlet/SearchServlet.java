package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import searchhead.DistributedSearch;
import base.EncapsulatedIncomingResult;
import base.SearchResultDocument;

import com.google.gson.Gson;

/**
 * Servlet implementation class SearchServlet
 */
@WebServlet("/search")
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchServlet() {
        super();
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String query=request.getParameter("query");
		Map<String, SearchResultDocument> result = DistributedSearch.getResults(query);
		EncapsulatedIncomingResult encapres = new EncapsulatedIncomingResult();
		Integer i=1;
		for (Entry<String, SearchResultDocument> entry : result.entrySet()) {
		    encapres.map.put(i.toString(), new String[] {entry.getValue().getFilename(),entry.getValue().getScore(),entry.getValue().getHighlight(),entry.getValue().getShardLink()});
			i++;
		}
		Gson gson = new Gson();
		String resultString = gson.toJson(encapres);
		PrintWriter out = response.getWriter();
		out.print(resultString);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
