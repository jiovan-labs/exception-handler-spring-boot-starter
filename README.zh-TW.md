[English](README.md) | [簡體中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

# 異常處理器 Spring Boot Starter

為 Spring Boot 應用啟用全域性異常處理器，以優雅地處理異常。支援處理過濾器、控制器和事件監聽器中發生的異常。

## 快速開始

- 匯入相依性

```xml
    <dependency>
        <groupId>com.jiovan.spring.starter</groupId>
        <artifactId>exception-handler-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> 預設情況下，此 starter 會自動生效，你可以透過 `jiovan.exception-handler.enabled = false` 關閉它。

- 使用字首 `jiovan.exception-handler` 設定 Spring Boot 的 `application.yml`（**可選**）

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
            invokable-interceptors:
                - com.foo.bar
```

- 此 starter 支援主流模板引擎，包括：
  - Thymeleaf
  - FreeMarker
  - Groovy
  - Mustache
  - JSP

- **可選功能**：如果應用中使用了模板引擎，可以透過 `@EnableSimpleErrorController` 註解啟用一個簡單的錯誤控制器，它繼承自 `BasicErrorController`，可同時處理 HTML 請求與 REST 請求。

> 你還可以透過定義一個實現了 `ErrorControllerCustomizer` 介面的 Bean，自定義該錯誤控制器生成的錯誤資料。

> 你可以處理 `ServletExceptionHandledEvent` 事件，以執行自己的邏輯，例如日誌記錄等。

## 文件

- GitHub：https://github.com/jiovan-labs/exception-handler-spring-boot-starter

## 環境要求

- jdk 17+

## 許可證

本專案採用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 許可證。

請參閱 `NOTICE.txt` 檔案瞭解所需的宣告與歸屬資訊。

## 捐贈

喜歡這個工具包嗎？歡迎[向我們捐贈](https://jiovan.com/donation)以支援開發。

## 版權

北京玖漫科技有限公司

## 網站

- 玖漫：https://jiovan.com
