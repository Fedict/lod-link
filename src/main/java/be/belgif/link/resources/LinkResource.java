/*
 * Copyright (c) 2016, Bart Hanssens <bart.hanssens@fedict.be>
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
package be.belgif.link.resources;

import be.belgif.link.helpers.QueryHelper;
import be.belgif.link.helpers.RDFMediaType;

import com.codahale.metrics.annotation.ExceptionMetered;

import javax.annotation.security.PermitAll;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.repository.Repository;

/**
 * Storage for link metadata
 * 
 * @author Bart.Hanssens
 */

@Path("/link")
@Produces({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES, RDFMediaType.TTL})
public class LinkResource  {
	private final Repository repo;
	
	/**
	 * Get all triples for a subject
	 * 
	 * @param url URI of the subject
	 * @return HTTP OK
	 */
	@GET
	@ExceptionMetered
	public Model getById(@QueryParam("url") String url) {
		return QueryHelper.getBySubj(repo, QueryHelper.asURI(url));
	}
	
	/**
	 * Add statements to the store
	 * 
	 * @param m
	 * @return HTTP OK when done 
	 */
	@PermitAll
	@PUT
	@Consumes({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES, RDFMediaType.TTL})
	@ExceptionMetered
	public Response postLinkdata(Model m) {
		QueryHelper.putStatements(repo, m);
		return Response.ok().build();
	}
	
	/**
	 * Delete all statements for a given subject
	 * 
	 * @param url subject URI
	 * @return HTTP OK when done
	 */
	@PermitAll
	@DELETE
	@ExceptionMetered
	public Response deleteLinkdata(@QueryParam("url") String url) {
		QueryHelper.deleteStatements(repo, url);
		return Response.ok().build();
	}

	/**
	 * Lucene re-index
	 * 
	 * @return HTTP OK when done
	 */
	@PermitAll
	@POST
	@Path("/_reindex")
	public Response reIndex() {
		QueryHelper.reIndex(repo);
		return Response.ok().build();
	}
	
	/**
	 * Full text search
	 * 
	 * @param text
	 * @return triples
	 */
	@GET
	@Path("/_search")
	@ExceptionMetered
	public Model searchLink(@QueryParam("q") String text) {
		return QueryHelper.getFTS(repo, text);
	}

	/**
	 * Search by dcat:theme
	 * 
	 * @param uri theme uri
	 * @return triples
	 */
	@GET
	@Path("/_filter")
	@ExceptionMetered
	public Model searchBy(@QueryParam("theme") String uri) {
		return QueryHelper.getLabelByPred(repo, DCAT.THEME, QueryHelper.asURI(uri));
	}

	/**
	 * Constructor
	 * 
	 * @param repo 
	 */
	public LinkResource(Repository repo) {
		this.repo = repo;
	}
}
