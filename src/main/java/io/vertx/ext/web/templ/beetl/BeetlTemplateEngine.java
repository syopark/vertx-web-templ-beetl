package io.vertx.ext.web.templ.beetl;

import org.beetl.core.GroupTemplate;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.common.template.TemplateEngine;
import io.vertx.ext.web.templ.beetl.impl.BeetlTemplateEngineImpl;

@VertxGen
public interface BeetlTemplateEngine extends TemplateEngine {
	/**
	 * Default template extension
	 */
	String DEFAULT_TEMPLATE_EXTENSION = "html";

	/**
	 * Default template root
	 */
	String DEFAULT_TEMPLATE_ROOT = "templates";
	/**
	 * Function Request 
 	 *  [@RoutingContext] state 
	 */
	String REQUEST ="request";
	/**
	 * String CTXPATH 
 	 *  [@RoutingContext] state 
	 */
	String CTXPATH ="ctxPath";
	/**
	 * Map PAGE 
 	 *  [@RoutingContext] state 
	 */
	String PAGE ="$page";
	/**
	 *  Map PARAMS 
 	 *  [@RoutingContext] state 
	 */
	String PARAMS = "params";
	/**
	 * Create a template engine using defaults
	 *
	 * @return the engine
	 */
	static BeetlTemplateEngine create(Vertx vertx) {
		return new BeetlTemplateEngineImpl(vertx, DEFAULT_TEMPLATE_ROOT, DEFAULT_TEMPLATE_EXTENSION);
	}

	/**
	 * Create a template engine using defaults
	 *
	 * @return the engine
	 */
	static BeetlTemplateEngine create(Vertx vertx, String root) {
		return new BeetlTemplateEngineImpl(vertx, root, DEFAULT_TEMPLATE_EXTENSION);
	}

	/**
	 * Create a template engine using defaults
	 *
	 * @return the engine
	 */
	static BeetlTemplateEngine create(Vertx vertx, String root, String extension) {
		return new BeetlTemplateEngineImpl(vertx, root, extension);
	}
	/**
	 * 扩展Beetl模型输出RoutingContext属性
	 * @param ctx
	 * @return
	 */
	@Fluent
	BeetlTemplateEngine setRoutingContext(RoutingContext ctx);

	/**
	 * 
	 * @return
	 */
	@GenIgnore
	GroupTemplate getGroupTemplate();

}
