package base;
public class SearchResultDocument {
	public String path;
	public String filename;
	public String score;
	public String highlight;
	public String ShardLink;
	public String getHighlight() {
		return highlight;
	}
	public void setHighlight(String highlight) {
		this.highlight = highlight;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getShardLink() {
		return ShardLink;
	}
	public void setShardLink(String shardLink) {
		ShardLink = shardLink;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	
}
