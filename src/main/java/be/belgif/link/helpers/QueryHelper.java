/*
 * Copyright (c) 2017, Bart Hanssens <bart.hanssens@fedict.be>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
import org.eclipse.rdf4j.model.Resource;
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
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;

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

	private final static String Q_FTS
			= "PREFIX luc: <http://www.ontotext.com/owlim/lucene#> " + "\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "\n"
			+ "PREFIX dcterms: <http://purl.org/dc/terms/> " + "\n"
			+ "PREFIX schema: <http://schema.org/> " + "\n"
 			+ "CONSTRUCT { ?s rdfs:label ?o } "
			+ " WHERE { ?s luc:myIndex ?query . "
			+ " ?s rdfs:label|dcterms:title|schema:name ?o } ";

	private final static String Q_PROP
			= "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "\n"
			+ "PREFIX dcterms: <http://purl.org/dc/terms/> " + "\n"
			+ "PREFIX schema: <http://schema.org/> " + "\n"
			+ "CONSTRUCT { ?s rdfs:label ?o } "
			+ " WHERE { ?s rdfs:label|dcterms:title|schema:name ?o . "
			+ " ?s ?pred ?val } ";

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
		return F.createIRI(name);
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
		if (!m.isEmpty()) {
			m.setNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
			m.setNamespace(FOAF.PREFIX, FOAF.NAMESPACE);
			m.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
			m.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
			m.setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
			m.setNamespace("schema", "http://schema.org");
			m.setNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
			m.setNamespace(VOID.PREFIX, VOID.NAMESPACE);
			m.setNamespace(XMLSchema.PREFIX, XMLSchema.NAMESPACE);
		}
		return m;
	}

	/**
	 * Get all triples by subject
	 *
	 * @param repo RDF store
	 * @param subj subject IRI or null
	 * @param graph graph IRI or null
	 *
	 * @return all triples
	 */
	public static Model get(Repository repo, IRI subj, Resource graph) {
		Model m = new LinkedHashModel();

		try (RepositoryConnection conn = repo.getConnection()) {
			Iterations.addAll(conn.getStatements(subj, null, null, graph), m);
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
	public static Model query(Repository repo, String qry, Map<String, Value> bindings) {
		try (RepositoryConnection conn = repo.getConnection()) {
			GraphQuery gq = conn.prepareGraphQuery(QueryLanguage.SPARQL, qry);
			bindings.forEach((k, v) -> gq.setBinding(k, v));

			return setNamespaces(QueryResults.asModel(gq.evaluate()));
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
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
		Map<String, Value> map = new HashMap();
		map.put("query", asLiteral(text + "*"));
		return QueryHelper.query(repo, qry, map);
	}

	/**
	 * Get URI and RDFS label for triples having a specific property
	 *
	 * @param repo repository
	 * @param pred predicate URI
	 * @param val object value
	 * @return
	 */
	public static Model getLabelByPred(Repository repo, IRI pred, Value val) {
		String qry = Q_PROP;
		Map<String, Value> map = new HashMap();
		map.put("pred", pred);
		map.put("val", val);
		return QueryHelper.query(repo, qry, map);
	}

	/**
	 * Put statements in the store
	 *
	 * @param repo RDF store
	 * @param m triples
	 */
	public static void add(Repository repo, Model m) {
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
	public static void delete(Repository repo, IRI url, Resource graph) {
		try (RepositoryConnection conn = repo.getConnection()) {
			conn.remove(url, null, null, graph);
		} catch (RepositoryException e) {
			throw new WebApplicationException(e);
		}
	}
}
