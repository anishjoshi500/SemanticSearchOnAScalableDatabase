package base;

public class SearchResultDoc {

	public String name;
	public String highlight;
	public String score;
	
	
	public SearchResultDoc(String name,String highlight,String score)
	{
		this.name=name;
		this.highlight=highlight;
		this.score=score;
	}
	
	public SearchResultDoc()
	{
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getName() {
		return name;
	}
	public String getHighlight() {
		return highlight;
	}
	public String getScore() {
		return score;
	}
	
	
}
