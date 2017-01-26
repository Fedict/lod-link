/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.belgif.link.helpers;

import be.belgif.link.App;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;

import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.model.vocabulary.VOID;

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.lucene.LuceneSail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for querying the triple store
 * 
 * @author Bart.Hanssens
 */
public class QueryHelper {	
	private final static Logger LOG = (Logger) LoggerFactory.getLogger(QueryHelper.class);
	private final static ValueFactory F = SimpleValueFactory.getInstance();
	
	private final static String Q_FTS = 
		"PREFIX search: <http://www.openrdf.org/contrib/lucenesail#> " + "\n" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "\n" +
		"PREFIX dcterms: <http://purl.org/dc/terms/> " + "\n" +	
		"CONSTRUCT { ?s rdfs:label ?o } " +
		" WHERE { ?s search:matches [ search:query ?query ] . " +
				" ?s rdfs:label|dcterms:title ?o } ";

	/**
	 * Get string as URI
	 * 
	 * @param uri
	 * @return URI representation
	 */
	public static IRI asURI(String uri) {
		return F.createIRI(uri);
	}
	
	/**
	 * Get name graph + context id from name
	 * 
	 * @param name
	 * @return context URI 
	 */
	public static IRI asGraph(String name) {
		return F.createIRI(App.PREFIX_GRAPH + name);
	}
	
	/**
	 * Get string as RDF literal
	 * 
	 * @param lit
	 * @return literal 
	 */
	public static Literal asLiteral(String lit) {
		return F.createLiteral(lit);
	}

	/**
	 * Add namespaces to triple model
	 * 
	 * @param m model
	 * @return model with namespaces
	 */
	public static Model setNamespaces(Model m) {
		if (! m.isEmpty()) {
			m.setNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
			m.setNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
			m.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
			m.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
			m.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
			m.setNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
			m.setNamespace(VOID.PREFIX, VOID.NAMESPACE);
		}
		return m;
	}
	
	/**
	 * Reindex Lucene Sail
	 * 
	 * @param repo  
	 */
	public static void reIndex(Repository repo) {
		Sail sail = ((SailRepository) repo).getSail();
		if (sail instanceof LuceneSail) {
			LOG.info("Reindexing lucene sail");
			try {
				((LuceneSail) sail).reindex();
			} catch (Exception ex) {
				throw new WebApplicationException(ex);
			}
			LOG.info("Done");
		}
	}
	
	/**
	 * Get all triples by subject
	 * 
	 * @param repo RDF store
	 * @param subj subject IRI or null
	 * @param pred predicate IRI or null
	 * @param obj object IRI, Literal or null 
	 * @return all triples
	 */
	public static Model getBySPO(Repository repo, IRI subj, IRI pred, Value obj) {
		Model m = new LinkedHashModel();
	System.err.println(subj);
	System.err.println(pred);
	System.err.println(obj);
	
		try (RepositoryConnection conn = repo.getConnection()) {
			Iterations.addAll(conn.getStatements(subj, pred, obj), m);
		} catch (RepositoryException e) {
			throw new WebApplicationException(e);
		}
		return setNamespaces(m);
	}
	
	/**
	 * Prepare and run a SPARQL query
	 *
	 * @param repo repository
	 * @param qry query string
	 * @param bindings bindings (if any)
	 * @return results in triple model
	 */
	public static Model query(Repository repo, String qry, Map<String,Value> bindings) {
		try (RepositoryConnection conn = repo.getConnection()) {
			GraphQuery gq = conn.prepareGraphQuery(QueryLanguage.SPARQL, qry);
			bindings.forEach((k,v) -> gq.setBinding(k, v));

			return setNamespaces(QueryResults.asModel(gq.evaluate()));
		} catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
			throw new WebApplicationException(e);
		}
	}

	/**
	 * Full text search
	 *
	 * @param repo RDF store 
	 * @param text text to search for
	 * @return RDF model 
	 */
	public static Model getFTS(Repository repo, String text) {
		String qry = Q_FTS;
		Map<String,Value> map = new HashMap();
		map.put("query", asLiteral(text + "*"));
		return QueryHelper.query(repo, qry, map);
	}
	
	/**
	 * Put statements in the store
	 *
	 * @param repo RDF store
	 * @param m triples
	 */
	public static void putStatements(Repository repo, Model m) {
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.add(m);
		} catch (RepositoryException e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Delete all triples for subject URL
	 * 
	 * @param repo RDF store
	 * @param url subject to delete
	 */
	public static void deleteStatements(Repository repo, String url) {
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.remove(F.createIRI(url), null, null);
		} catch (RepositoryException e) {
			throw new WebApplicationException(e);
		}
	}
}
