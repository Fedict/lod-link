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
package be.belgif.link;

import be.belgif.link.auth.DummyUser;
import be.belgif.link.auth.UpdateAuth;
import be.belgif.link.health.RdfStoreHealthCheck;
import be.belgif.link.helpers.ManagedRepository;
import be.belgif.link.helpers.RDFMessageBodyReader;
import be.belgif.link.helpers.RDFMessageBodyWriter;

import be.belgif.link.resources.LinkResource;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;


import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;


/**
 * Main Dropwizard web application
 * 
 * @author Bart.Hanssens
 */
public class App extends Application<AppConfig> {
	private final static String PREFIX = "http://id.belgium.be/link/";
	private final static String PREFIX_GRAPH = "http://id.belgium.be/graph/link/";

	/**
	 * Get base URI
	 * 
	 * @return 
	 */
	public static String getPrefix() {
		return PREFIX;
	}

	/**
	 * Get base URI
	 * 
	 * @return 
	 */
	public static String getGraphPrefix() {
		return PREFIX_GRAPH;
	}
	
	/**
	 * Configure a triple store repository
	 * 
	 * @param cfg configuration object
	 * @return repository 
	 */
	private Repository configRepo(AppConfig cfg) {
		RemoteRepositoryManager mgr = new RemoteRepositoryManager(cfg.getStore());
		if (cfg.getStoreUsername() != null && !cfg.getUsername().isEmpty()) {
			mgr.setUsernameAndPassword(cfg.getStoreUsername(), cfg.getStorePassword());
		}
		mgr.initialize();
		return mgr.getRepository(cfg.getStoreName());
	}

	@Override
	public String getName() {
		return "lod-link";
	}
	
	
	@Override
    public void run(AppConfig config, Environment env) {
		Repository repo = configRepo(config);
	
		// Authentication
		env.jersey().register(new AuthDynamicFeature(
				new BasicCredentialAuthFilter.Builder<DummyUser>()
						.setAuthenticator(
								new UpdateAuth(config.getUsername(), config.getPassword()))
						.buildAuthFilter()));
		
		// Managed resource
		env.lifecycle().manage(new ManagedRepository(repo));
		
		// RDF Serialization formats
		env.jersey().register(new RDFMessageBodyReader());
		env.jersey().register(new RDFMessageBodyWriter());
		
		// Resources / "web pages"
		env.jersey().register(new LinkResource(repo));

		// Monitoring
		RdfStoreHealthCheck check = new RdfStoreHealthCheck(repo);
		env.healthChecks().register("triplestore", check);
	}
	
	/**
	 * Main 
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new App().run(args);
	}
}
