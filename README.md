# Vertx Beetl Template
在Vert.x已支持的模板引擎的基础上，增加对国内流行模板引擎[Beetl3](http://ibeetl.com/)的支持。
> 目前，对于Vert.x 和 Beetl都处于学习阶段，仅供参考。

## 组件版本
- Vert.x(4.0.0.Beta2)
- Beetl(3.1.7.RELEASE)

## 使用
默认模板目录：templates（在src/main/resources/下）
默认模板后缀：html


```
   // 创建一个模板引擎
	BeetlTemplateEngine engine = BeetlTemplateEngine.create(vertx);
	Router router = Router.router(vertx);
	router.route().handler(BodyHandler.create());
	// 设置访问路径
	router.route("/hello").handler(routingContext -> {
	// 渲染模板
		engine.render(routingContext.data(), "/helloworld", res -> {
			// 如果模板解析成功，就将结果写到response
			if (res.succeeded()) {
				routingContext.response().putHeader("Content-Type", "text/html;charset=utf-8").end(res.result());
			} else { // 如果解析失败，就显示fail
				routingContext.fail(res.cause());
			}
		});
	});
```

