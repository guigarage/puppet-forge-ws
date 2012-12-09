package com.guigarage.puppet.forge;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties
public class PuppetForgeModule implements Serializable{

	private static final long serialVersionUID = 1L;

	private String file;
	
	private String version;

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
