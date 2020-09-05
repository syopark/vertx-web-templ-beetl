package io.vertx.ext.web.templ.beetl.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.templ.beetl.BeetlTemplateEngine;

public class BeetlTemplateEngineImpl implements BeetlTemplateEngine {

	protected GroupTemplate groupTemplate = null;
	protected RoutingContext ctx = null;
	protected Vertx vertx = null;
	protected String extension;

	public BeetlTemplateEngineImpl(Vertx vertx, String root, String ext) {
		Objects.requireNonNull(vertx);
		this.vertx = vertx;
		// 如果未指定groupTemplate，取上下文中唯一的GroupTemplate对象
		if (groupTemplate == null) {
			Configuration cfg = null;
			try {
				cfg = Configuration.defaultConfiguration();
			} catch (IOException e) {
				e.printStackTrace();
			}
			groupTemplate = new GroupTemplate(new VertxResourceLoader(vertx, root), cfg);
		}
		this.extension = ext.charAt(0) == '.' ? ext : "." + ext;
	}

	@Override
	public void render(Map<String, Object> context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
		try {
			Template template = getBeetlTemplate(templateFileName);
			if (ctx != null) {
				HttpServerRequest request = ctx.request();
				template.binding(REQUEST, request);
				template.binding(CTXPATH, request.scheme() + "://" + request.host() + "/");
				template.binding(PAGE, new HashMap<String, Object>());
				template.binding(PARAMS, request.params());
			}
			template.binding(context);
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				template.renderTo(new OutputStreamWriter(baos));
				ctx = null;
				handler.handle(Future.succeededFuture(Buffer.buffer(baos.toByteArray())));
			}

		} catch (Exception ex) {
			handler.handle(Future.failedFuture(ex));
		}
	}

	@Override
	public GroupTemplate getGroupTemplate() {
		return this.groupTemplate;
	}

	@Override
	public BeetlTemplateEngine setRoutingContext(RoutingContext ctx) {
		this.ctx = ctx;
		return this;
	}

	/**
	 * 获取模板
	 * 
	 * @param key ，key为模板resourceId
	 * @return
	 */
	protected Template getBeetlTemplate(String key) {
		Template template = null;
		int ajaxIdIndex = key.lastIndexOf("#");
		if (ajaxIdIndex != -1) {
			String ajaxId = key.substring(ajaxIdIndex + 1);
			key = adjustLocation(key.substring(0, ajaxIdIndex));
			template = groupTemplate.getAjaxTemplate(key, ajaxId);

		} else {
			template = groupTemplate.getTemplate(adjustLocation(key));
		}
		return template;
	}

	protected String adjustLocation(String location) {
		if (extension != null) {
			if (!location.endsWith(extension)) {
				location += extension;
			}
		}
		return location;
	}

}
