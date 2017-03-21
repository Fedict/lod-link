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
package be.belgif.link.tasks;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;

import io.dropwizard.servlets.tasks.Task;
import java.io.BufferedWriter;
import java.io.IOException;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import javax.ws.rs.WebApplicationException;
import org.eclipse.rdf4j.model.Resource;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Export all data from repository.
 *
 * @author Bart.Hanssens
 */
public class RDFExportTask extends Task {

	private final Logger LOG = (Logger) LoggerFactory.getLogger(RDFExportTask.class);

	private final String exportDir;
	private final Repository repo;

	/**
	 * Export triples from RDF store into file.
	 *
	 * @param file output file path
	 * @param graph optional graph name
	 */
	private void exportFile(Path file, String graph) {
		LOG.info("Trying to write  {}", file);

		Optional<RDFFormat> format = Rio.getParserFormatForFileName(file.toString());
		if (!format.isPresent()) {
			throw new WebApplicationException("File type not supported");
		}

		try (BufferedWriter buf = 
				Files.newBufferedWriter(file, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
				RepositoryConnection conn = repo.getConnection()) {
			Resource ctx = (graph != null) ? repo.getValueFactory().createIRI(graph) : null;
			RDFWriter w = Rio.createWriter(format.get(), buf);

			conn.export(w, ctx);
		} catch (IOException rex) {
			// will be rolled back automatically
			throw new WebApplicationException("Error exporting to file", rex);
		}

		LOG.info("Done");
	}

	/**
	 * Execute task
	 *
	 * @param param parameters
	 * @param w output writer
	 * @throws Exception
	 */
	@Override
	@Timed
	public void execute(ImmutableMultimap<String, String> param, PrintWriter w)
			throws Exception {
		ImmutableCollection<String> files = param.get("file");
		if (files == null || files.isEmpty()) {
			throw new WebApplicationException("No file name given");
		}

		ImmutableCollection<String> graphs = param.get("graph");
		String graph = (graphs == null || graphs.isEmpty()) ? null
				: graphs.asList().get(0);

		for (String file : files.asList()) {
			Path infile = Paths.get(exportDir, file);
			exportFile(infile, graph);
		}
	}

	/**
	 * Constructor
	 *
	 * @param repo triple store
	 * @param outDir output directory
	 */
	public RDFExportTask(Repository repo, String outDir) {
		super("rdf-export");
		this.repo = repo;
		this.exportDir = outDir;
	}
}
