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

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 *
 * @author Bart.Hanssens
 */
public class AppConfig extends Configuration {

	@NotEmpty
	private String dataDir;

	@NotEmpty
	private String luceneDir;

	@NotEmpty
	private String username;

	@NotEmpty
	private String password;
	
	@NotEmpty
	private String importDir;
	
	@NotEmpty
	private String exportDir;
	

	@JsonProperty
	public String getDataDir() {
		return dataDir;
	}

	@JsonProperty
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	@JsonProperty
	public String getLuceneDir() {
		return luceneDir;
	}

	@JsonProperty
	public void setLuceneDir(String luceneDir) {
		this.luceneDir = luceneDir;
	}

	@JsonProperty
	public String getImportDir() {
		return importDir;
	}

	@JsonProperty
	public void setImportDir(String importDir) {
		this.importDir = importDir;
	}

	@JsonProperty
	public String getExportDir() {
		return exportDir;
	}

	@JsonProperty
	public void setExportDir(String exportDir) {
		this.exportDir = exportDir;
	}

	@JsonProperty
	public String getUsername() {
		return username;
	}

	@JsonProperty
	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty
	public String getPassword() {
		return password;
	}

	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
	}
}
