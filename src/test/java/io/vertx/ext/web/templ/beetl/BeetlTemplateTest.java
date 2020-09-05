package io.vertx.ext.web.templ.beetl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.common.template.TemplateEngine;

@RunWith(VertxUnitRunner.class)
public class BeetlTemplateTest {
	private static Vertx vertx;

	@BeforeClass
	public static void before() {
		vertx = Vertx
				.vertx(new VertxOptions().setFileSystemOptions(new FileSystemOptions().setFileCachingEnabled(true)));
	}

	@Test
	public void testTemplateHandlerOnClasspath(TestContext should) {
		final Async test = should.async();
		TemplateEngine engine = BeetlTemplateEngine.create(vertx);

		final JsonObject context = new JsonObject().put("foo", "badger").put("bar", "fox");

		context.put("context", new JsonObject().put("path", "/test-beetl-template.html"));

		engine.render(context, "test-beetl-template.html", render -> {
			should.assertTrue(render.succeeded());
			should.assertEquals("Hello badger and fox", render.result().toString());
			test.complete();
		});
		test.await();
	}

	@Test
	public void testCachingEnabled(TestContext should) throws IOException {
		final Async test = should.async();

		System.setProperty("vertxweb.environment", "production");
		TemplateEngine engine = BeetlTemplateEngine.create(vertx, "");

		PrintWriter out;
		File temp = File.createTempFile("template", ".html", new File("target/classes"));
		temp.deleteOnExit();

		out = new PrintWriter(temp);
		out.print("before");
		out.flush();
		out.close();

		engine.render(new JsonObject(), temp.getName(), render -> {
			should.assertTrue(render.succeeded());
			should.assertEquals("before", render.result().toString());
			// cache is enabled so if we change the content that should not affect the
			// result

			try {
				PrintWriter out2 = new PrintWriter(temp);
				out2.print("after");
				out2.flush();
				out2.close();
			} catch (IOException e) {
				should.fail(e);
			}

			engine.render(new JsonObject(), temp.getName(), render2 -> {
				should.assertTrue(render2.succeeded());
				should.assertEquals("before", render2.result().toString());
				test.complete();
			});
		});
		test.await();
	}

	@Test
	public void testTemplateHandlerOnFileSystem(TestContext should) {
		final Async test = should.async();
		TemplateEngine engine = BeetlTemplateEngine.create(vertx, "");

		final JsonObject context = new JsonObject().put("foo", "badger").put("bar", "fox");

		context.put("context", new JsonObject().put("path", "/test-beetl-template1.html"));

		engine.render(context, "src/test/filesystemtemplates/test-beetl-template1.html", render -> {
			should.assertTrue(render.succeeded());
			should.assertEquals("Hello badger and fox\nRequest path is /test-beetl-template1.html\n",
					render.result().toString());
			test.complete();
		});
		test.await();
	}

	@Test
	public void testTemplateHandlerOnClasspathDisableCaching(TestContext context) {
		System.setProperty("vertxweb.environment", "development");
		testTemplateHandlerOnClasspath(context);
	}

	@Test
	public void testTemplateHandlerNoExtension(TestContext should) {
		final Async test = should.async();
		TemplateEngine engine = BeetlTemplateEngine.create(vertx);

		final JsonObject context = new JsonObject().put("foo", "badger").put("bar", "fox");

		context.put("context", new JsonObject().put("path", "/test-beetl-template.html"));

		engine.render(context, "test-beetl-template", render -> {
			should.assertTrue(render.succeeded());
			should.assertEquals("Hello badger and fox", render.result().toString());
			test.complete();
		});
		test.await();
	}

	@Test
	public void testTemplateHandlerChangeExtension(TestContext should) {
		final Async test = should.async();
		TemplateEngine engine = BeetlTemplateEngine.create(vertx, BeetlTemplateEngine.DEFAULT_TEMPLATE_ROOT, "mvl");

		final JsonObject context = new JsonObject().put("foo", "badger").put("bar", "fox");

		context.put("context", new JsonObject().put("path", "/test-beetl-template.mvl"));

		engine.render(context, "test-beetl-template", render -> {
			should.assertTrue(render.succeeded());
			should.assertEquals("Cheerio Request path is /test-beetl-template.mvl", render.result().toString());
			test.complete();
		});
		test.await();
	}

	@Test
	public void testTemplateHandlerIncludes(TestContext should) {
		final Async test = should.async();
		TemplateEngine engine = BeetlTemplateEngine.create(vertx);

		final JsonObject context = new JsonObject().put("foo", "badger").put("bar", "fox");

		context.put("context", new JsonObject().put("path", "/test-beetl-template.html"));

		engine.render(context, "include-template", render -> {
			should.assertTrue(render.succeeded());
			should.assertEquals("Hello badger and fox", render.result().toString());
			test.complete();
		});
		test.await();
	}

	@Test
	public void testNoSuchTemplate(TestContext should) {
		final Async test = should.async();
		TemplateEngine engine = BeetlTemplateEngine.create(vertx);

		engine.render(new JsonObject(), "not-found", render -> {
			should.assertTrue(render.failed());
			test.complete();
		});
		test.await();
	}
}
