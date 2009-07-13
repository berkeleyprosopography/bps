package bps.services.corpus.main;

public class Corpus {
	private int			id;
	private String		name;
	private String		description;

	private static		nextID = 1;

	public Corpus() {
		Corpus(Corpus.nextID++, null, null);
	}

	public Corpus( String name, String description ) {
		Corpus(Corpus.nextID++, name, description);
	}

	public Corpus( id, String name, String Description ) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public int getID() {
		return id;
	}

	public void setID( int value ) {
		id = value;
	}

	public String getName() {
		return name;
	}

	public void setName( String value ) {
		name = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription( String value ) {
		description = value;
	}

	public static void main(String[] args) {
		Corpus testCorpus = new Corpus("test", null);

	}
}
