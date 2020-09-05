package io.vertx.ext.web.templ.beetl.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;

import io.vertx.core.file.FileSystem;

class VertxResource extends Resource {

	FileSystem filesystem = null;
	String path = null;
	long lastModified = 0;

	public VertxResource(FileSystem filesystem, String id, String path, ResourceLoader loader) {
		super(id, loader);
		this.filesystem = filesystem;
		this.path = path;
		filesystem.props(path, ar -> {
			if(ar.succeeded()){
				lastModified = ar.result().lastModifiedTime();
			}
		});
	}

	@Override
	public Reader openReader() {
		return new BufferedReader(new InputStreamReader(
				new ByteArrayInputStream(filesystem.readFileBlocking(path).getBytes()), Charset.defaultCharset()));
	}

	@Override
	public boolean isModified() {
		if (filesystem.existsBlocking(path))
			return filesystem.propsBlocking(path).lastModifiedTime() != this.lastModified;
		return false;
	}

}
