package de.ulb.istg.geodata;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class GivlodumAllegro {

	public static void main(String[] args) throws UnsupportedEncodingException {

		GivlodumAllegro queryDBpedia = new GivlodumAllegro();
		queryDBpedia.queryExternalSources();
	}

	public void queryExternalSources() throws UnsupportedEncodingException {

		String queryString = "SELECT DISTINCT ?o WHERE {GRAPH <http://data.lodum.de/istg/ISTG_allegro.trig> { " +
				"?s a <http://vocab.lodum.de/istg/WrittenResource> . " +
				"?s <http://purl.org/dc/elements/1.1/subject> ?o}} ORDER BY ?o ";

		Query query = QueryFactory.create(queryString);
		ARQ.getContext().setTrue(ARQ.useSAX);


		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://giv-lodumdata.uni-muenster.de:8080/openrdf-sesame/repositories/istg", query);
		ResultSet results = qexec.execSelect();


		try {

			FileOutputStream fileStream = new FileOutputStream(new File("/home/jones/delete/givlodum-allegro.txt"),true);
			OutputStreamWriter writer = new OutputStreamWriter(fileStream, "UTF8");
			long total = 0;
			
			while (results.hasNext()) {

				QuerySolution soln = results.nextSolution();

				if(soln.get("?o").isLiteral()){

					System.out.println(soln.get("?o") + " >> "+soln.getLiteral("?o").getValue());
					writer.append(soln.getLiteral("?o").getValue().toString()+"\n");
					total++;
				}

			}

			writer.close();
			qexec.close();
			
			System.out.println("Total records: " + total);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}