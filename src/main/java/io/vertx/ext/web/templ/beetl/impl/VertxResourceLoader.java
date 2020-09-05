package io.vertx.ext.web.templ.beetl.impl;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.beetl.core.GroupTemplate;
import org.beetl.core.Resource;
import org.beetl.core.ResourceLoader;
import org.beetl.core.fun.FileFunctionWrapper;
import org.beetl.core.misc.BeetlUtil;

import io.vertx.core.Vertx;

class VertxResourceLoader implements ResourceLoader {

	Vertx vertx = null;
	String root = null;
	boolean autoCheck = false;
	String functionRoot = "funtion";
	String functionSuffix = "fn";

	VertxResourceLoader(Vertx vertx, String root) {
		this.vertx = vertx;
		this.root = root;
	}

	@Override
	public void init(GroupTemplate gt) {
		Map<String, String> resourceMap = gt.getConf().getResourceMap();
		if (resourceMap.get("root") != null) {
			String temp = resourceMap.get("root");
			temp = checkRoot(temp);
			this.root = getChildPath(root, temp);
		}

		this.autoCheck = Boolean.parseBoolean(resourceMap.get("autoCheck"));
		this.functionSuffix = resourceMap.get("functionSuffix");
		this.functionRoot = resourceMap.get("functionRoot");
		String funcpath = getChildPath(root, functionRoot);
		vertx.fileSystem().exists(funcpath, ar -> {
			if (ar.result()) {
				Path start = Paths.get(funcpath);
				try {
					Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							String pathStr = file.toString();
							if (pathStr.endsWith(functionSuffix)) {
								String fileName = file.toString();
								String functionName = file.toString().substring(funcpath.length()+1,(fileName.length() - functionSuffix.length() - 1)).replace("\\",".");
								String resourceId = file.toString().substring(root.length());
								FileFunctionWrapper fun = new FileFunctionWrapper(resourceId);
								gt.registerFunction(functionName, fun);
							}
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public Resource getResource(String key) {
		return new VertxResource(vertx.fileSystem(), key, getChildPath(root, key), this);
	}

	@Override
	public boolean isModified(Resource resource) {
		if (this.autoCheck)
			return resource.isModified();
		return false;
	}

	@Override
	public boolean exist(String key) {
		return vertx.fileSystem().existsBlocking(getChildPath(root, key));
	}

	@Override
	public void close() {

	}

	@Override
	public String getResourceId(Resource resource, String id) {
		if (resource == null)
			return id;
		return BeetlUtil.getRelPath(resource.getId(), id);
	}

	@Override
	public String getInfo() {
		return "Vertx/Beetl ResourceLoader,Root" + this.root;
	}

	/**
	 * 引用 [@ClasspathResourceLoader] 检查classpath路径
	 * 
	 * @param path
	 * @return
	 */
	protected String checkRoot(String path) {
		if (path == null || path.length() == 0 || path.equals("/")) {
			return "";

		} else if (path.endsWith("/")) {
			return path.substring(0, path.length() - 1);
		} else if (path.startsWith("/")) {
			return path.substring(1, path.length());
		} else {
			return path;
		}
	}

	protected String getChildPath(String path, String child) {
		if (child.length() == 0) {
			return path;
		} else if (child.startsWith("/")) {
			return path + child;
		} else if("".equals(path)) {
			return child;
		} else {
			return path + "/" + child;
		}
	}
}
