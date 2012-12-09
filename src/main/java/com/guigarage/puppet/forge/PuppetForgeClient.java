package com.guigarage.puppet.forge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.type.TypeReference;

public class PuppetForgeClient {

	private String repositoryUrl;
	
	public PuppetForgeClient() {
		this("http://forge.puppetlabs.com");
	}
	
	public PuppetForgeClient(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}
	
	public List<PuppetForgeModuleDescription> findModules(String searchTerm)
			throws IOException {
		URL url = new URL(repositoryUrl + "/modules.json?q="
				+ searchTerm.trim());
		URLConnection connection = url.openConnection();

		MappingJsonFactory mappingJsonFactory = new MappingJsonFactory();
		JsonParser jp = mappingJsonFactory.createJsonParser(connection.getInputStream());
		return jp.readValueAs(new TypeReference<List<PuppetForgeModuleDescription>>(){});
	}
	
	public PuppetForgeModule findModule(String user, String moduleName)
			throws IOException {
		URL url = new URL(repositoryUrl + "/users/" + user + "/modules/" + moduleName + "/releases/find.json");
		URLConnection connection = url.openConnection();

		MappingJsonFactory mappingJsonFactory = new MappingJsonFactory();
		JsonParser jp = mappingJsonFactory.createJsonParser(connection.getInputStream());
		return jp.readValueAs(PuppetForgeModule.class);
	}
	
	public PuppetForgeModule findModule(PuppetForgeModuleDescription moduleDescription)
			throws IOException {
		return findModule(moduleDescription.getAuthor(), moduleDescription.getName());
	}
	
	public void installToModulesDir(File modulesPath, PuppetForgeModule module) throws IOException {
		if(!modulesPath.exists()) {
			modulesPath.mkdirs();
		}
		if(!modulesPath.isDirectory()) {
			throw new IOException("modulesPath has to be a directory and not a file!");
		}
		if(module.getFile().endsWith(".tar.gz")) {
			URL url = new URL(repositoryUrl + module.getFile());
			URLConnection connection = url.openConnection();
			GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(connection.getInputStream());
			TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);
			TarArchiveEntry entry = null; 
		    while ((entry = (TarArchiveEntry)tarIn.getNextEntry()) != null) {
		        final File outputFile = new File(modulesPath, entry.getName());
		        if (entry.isDirectory()) {
		            if (!outputFile.exists()) {
		                if (!outputFile.mkdirs()) {
		                    throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
		                }
		            }
		        } else {
		            final OutputStream outputFileStream = new FileOutputStream(outputFile); 
		            IOUtils.copy(tarIn, outputFileStream);
		            outputFileStream.close();
		        }
		    }
		    tarIn.close();
		} else if(module.getFile().endsWith(".tar")) {
			URL url = new URL(repositoryUrl + module.getFile());
			URLConnection connection = url.openConnection();
			TarArchiveInputStream tarIn = new TarArchiveInputStream(connection.getInputStream());
			TarArchiveEntry entry = null; 
		    while ((entry = (TarArchiveEntry)tarIn.getNextEntry()) != null) {
		        final File outputFile = new File(modulesPath, entry.getName());
		        if (entry.isDirectory()) {
		            if (!outputFile.exists()) {
		                if (!outputFile.mkdirs()) {
		                    throw new IllegalStateException(String.format("Couldn't create directory %s.", outputFile.getAbsolutePath()));
		                }
		            }
		        } else {
		            final OutputStream outputFileStream = new FileOutputStream(outputFile); 
		            IOUtils.copy(tarIn, outputFileStream);
		            outputFileStream.close();
		        }
		    }
		    tarIn.close();
		} else {
			throw new IOException("Unknown module file format for " + module.getFile());
		}
	}
	
	public void installToModulesDir(String modulesPath, PuppetForgeModule module) throws IOException {
		installToModulesDir(new File(modulesPath), module);
	}

	public static void main(String[] args) throws IOException {
		PuppetForgeClient client = new PuppetForgeClient();
		
		File moduleFolder = new File("/Users/hendrikebbers/Desktop/modules");
		
		List<PuppetForgeModuleDescription> allDescriptions = client.findModules("mongodb");
		
		for(PuppetForgeModuleDescription description : allDescriptions) {
			System.out.println("Installiere " + description.getFullName());
			PuppetForgeModule module = client.findModule(description);
			client.installToModulesDir(moduleFolder, module);
		}
	}

}
