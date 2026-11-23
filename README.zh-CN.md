[English](README.md) | [简体中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

# 异常处理器 Spring Boot Starter

为 Spring Boot 应用启用全局异常处理器，以优雅地处理异常。支持处理过滤器、控制器和事件监听器中发生的异常。

## 快速开始

- 导入依赖

```xml
    <dependency>
        <groupId>com.jiovan.spring.starter</groupId>
        <artifactId>exception-handler-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> 默认情况下，此 starter 会自动生效，你可以通过 `jiovan.exception-handler.enabled = false` 关闭它。

- 使用前缀 `jiovan.exception-handler` 配置 Spring Boot 的 `application.yml`（**可选**）

```yml
jiovan:
    exception-handler:
        exception-filter:
            filter-paths:
                - '/**'
            exclude-paths:
                - '/foo/**'
                - '/bar/**'
        exception-resolver:
            charset: UTF-8
            invokable-interceptors:
                - com.foo.bar
```

> `charset` 属性用于设置写入错误响应体时使用的字符集，默认为 `UTF-8`。

> Spring Boot 4 中错误属性绑定在 `spring.web.error` 前缀下（例如 `spring.web.error.include-message=always`），而 Spring Boot 3 使用的是 `server.error` 前缀。starter 会将 `WebProperties#getError()` 注册为 `ErrorProperties` Bean，便于自定义错误控制器注入。

- 此 starter 支持主流模板引擎，包括：
  - Thymeleaf
  - FreeMarker
  - Groovy
  - Mustache
  - JSP

- **可选功能**：如果应用中使用了模板引擎，可以通过 `@EnableSimpleErrorController` 注解启用一个简单的错误控制器，它继承自 `BasicErrorController`，可同时处理 HTML 请求与 REST 请求。

> 你还可以通过定义一个实现了 `ErrorControllerCustomizer` 接口的 Bean，自定义该错误控制器生成的错误数据。

> 你可以处理 `ServletExceptionHandledEvent` 事件，以执行自己的逻辑，例如日志记录等。

## 文档

- GitHub：https://github.com/jiovan-labs/exception-handler-spring-boot-starter

## 环境要求

- jdk 17+

## 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

请参阅 `NOTICE.txt` 文件了解所需的声明与归属信息。

## 捐赠

喜欢这个工具包吗？欢迎[向我们捐赠](https://jiovan.com/donation)以支持开发。

## 版权

北京玖漫科技有限公司

## 网站

- 玖漫：https://jiovan.com
