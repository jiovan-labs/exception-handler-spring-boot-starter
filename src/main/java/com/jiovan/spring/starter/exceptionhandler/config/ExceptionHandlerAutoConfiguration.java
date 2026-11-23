/*
 * Copyright (c) 2023 Jiovan Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiovan.spring.starter.exceptionhandler.config;


import java.util.Optional;
import jakarta.annotation.Nonnull;
import jakarta.servlet.Servlet;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.mustache.servlet.view.MustacheViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.springframework.web.servlet.view.groovy.GroovyMarkupViewResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import com.jiovan.plexus.common.toolkit.util.CollectionPlainWraps;
import com.jiovan.plexus.common.toolkit.util.MapPlainWraps;
import com.jiovan.spring.starter.exceptionhandler.filter.FilterExceptionHandlerFilter;
import com.jiovan.spring.starter.exceptionhandler.processor.SimpleEventMulticasterProcessor;
import com.jiovan.spring.starter.exceptionhandler.property.ExceptionHandlerProperties;
import com.jiovan.spring.starter.exceptionhandler.resolver.AbstractFilterExceptionResolver;
import com.jiovan.spring.starter.exceptionhandler.resolver.FreeMarkerFilterExceptionResolver;
import com.jiovan.spring.starter.exceptionhandler.resolver.GroovyFilterExceptionResolver;
import com.jiovan.spring.starter.exceptionhandler.resolver.InternalFilterExceptionResolver;
import com.jiovan.spring.starter.exceptionhandler.resolver.MustacheFilterExceptionResolver;
import com.jiovan.spring.starter.exceptionhandler.resolver.ThymeleafFilterExceptionResolver;


/**
 * Configuration of exception handler
 *
 * @author David Hsing
 *
 * @see org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
 * @see org.springframework.boot.web.servlet.support.ErrorPageFilter
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = ExceptionHandlerAutoConfiguration.PROPERTIES_PREFIX, name = "enabled", matchIfMissing = true)
@ConditionalOnClass(value = {Servlet.class, DispatcherServlet.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureOrder(value = Ordered.LOWEST_PRECEDENCE - 10)
@Import(value = {ExceptionHandlerAutoConfiguration.Entry.class, ExceptionHandlerAutoConfiguration.Resolver.class, ExceptionHandlerAutoConfiguration.Filter.class, ExceptionHandlerAutoConfiguration.Listener.class})
public class ExceptionHandlerAutoConfiguration {
    public static final String PROPERTIES_PREFIX = "jiovan.exception-handler";    // $NON-NLS-1$
    public static final String EXCEPTION_RESOLVER = "filterHandlerExceptionResolver";    // $NON-NLS-1$

    @Order(value = 0)
    @EnableConfigurationProperties(value = ExceptionHandlerProperties.class)
    static class Entry {
        @Bean
        @ConditionalOnMissingBean
        public ErrorProperties errorProperties(@Nonnull WebProperties webProperties) {
            return webProperties.getError();
        }
    }


    @Order(value = 1)
    static class Resolver {
        /**
         * Returns the {@link org.springframework.web.servlet.HandlerExceptionResolver} that renders errors through Thymeleaf views
         *
         * @param resolvers the provider of the Thymeleaf view resolver used to render the error view
         * @param properties the exception handler properties providing the resolver order
         *
         * @return a {@link com.jiovan.spring.starter.exceptionhandler.resolver.ThymeleafFilterExceptionResolver} with the configured order
         */
        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnBooleanProperty(prefix = "spring.thymeleaf", name = "enabled", matchIfMissing = true)
        @ConditionalOnClass(name = "org.thymeleaf.Thymeleaf")
        @ConditionalOnBean(name = "thymeleafViewResolver")
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver thymeleafFilterExceptionResolver(@Nonnull ObjectProvider<ThymeleafViewResolver> resolvers, @Nonnull ExceptionHandlerProperties properties) {
            ThymeleafViewResolver resolver = resolvers.getIfAvailable();
            if (resolver == null) {
                return null;
            }
            AbstractFilterExceptionResolver result = new ThymeleafFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        /**
         * Returns the {@link org.springframework.web.servlet.HandlerExceptionResolver} that renders errors through FreeMarker views
         *
         * @param resolvers the provider of the FreeMarker view resolver used to render the error view
         * @param properties the exception handler properties providing the resolver order
         *
         * @return a {@link com.jiovan.spring.starter.exceptionhandler.resolver.FreeMarkerFilterExceptionResolver} with the configured order
         */
        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnBooleanProperty(prefix = "spring.freemarker", name = "enabled", matchIfMissing = true)
        @ConditionalOnClass(name = "freemarker.template.Template")
        @ConditionalOnBean(value = FreeMarkerViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver freeMarkerFilterExceptionResolver(@Nonnull ObjectProvider<FreeMarkerViewResolver> resolvers, @Nonnull ExceptionHandlerProperties properties) {
            FreeMarkerViewResolver resolver = resolvers.getIfAvailable();
            if (resolver == null) {
                return null;
            }
            AbstractFilterExceptionResolver result = new FreeMarkerFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        /**
         * Returns the {@link org.springframework.web.servlet.HandlerExceptionResolver} that renders errors through Groovy markup views
         *
         * @param resolvers the provider of the Groovy markup view resolver used to render the error view
         * @param properties the exception handler properties providing the resolver order
         *
         * @return a {@link com.jiovan.spring.starter.exceptionhandler.resolver.GroovyFilterExceptionResolver} with the configured order
         */
        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnBooleanProperty(prefix = "spring.groovy.template", name = "enabled", matchIfMissing = true)
        @ConditionalOnClass(name = {"groovy.text.Template", "org.codehaus.groovy.tools.GroovyClass"})
        @ConditionalOnBean(value = GroovyMarkupViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver groovyFilterExceptionResolver(@Nonnull ObjectProvider<GroovyMarkupViewResolver> resolvers, @Nonnull ExceptionHandlerProperties properties) {
            GroovyMarkupViewResolver resolver = resolvers.getIfAvailable();
            if (resolver == null) {
                return null;
            }
            AbstractFilterExceptionResolver result = new GroovyFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        /**
         * Returns the {@link org.springframework.web.servlet.HandlerExceptionResolver} that renders errors through Mustache views
         *
         * @param resolvers the provider of the Mustache view resolver used to render the error view
         * @param properties the exception handler properties providing the resolver order
         *
         * @return a {@link com.jiovan.spring.starter.exceptionhandler.resolver.MustacheFilterExceptionResolver} with the configured order
         */
        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnBooleanProperty(prefix = "spring.mustache", name = "enabled", matchIfMissing = true)
        @ConditionalOnClass(name = {"com.samskivert.mustache.Template", "org.springframework.boot.mustache.servlet.view.MustacheViewResolver"})
        @ConditionalOnBean(name = "mustacheViewResolver")
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver mustacheFilterExceptionResolver(@Nonnull ObjectProvider<MustacheViewResolver> resolvers, @Nonnull ExceptionHandlerProperties properties) {
            MustacheViewResolver resolver = resolvers.getIfAvailable();
            if (resolver == null) {
                return null;
            }
            AbstractFilterExceptionResolver result = new MustacheFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }

        /**
         * Returns the {@link org.springframework.web.servlet.HandlerExceptionResolver} that renders errors through internal resource views
         *
         * @param resolvers the provider of the internal resource view resolver used to render the error view
         * @param properties the exception handler properties providing the resolver order
         *
         * @return a {@link com.jiovan.spring.starter.exceptionhandler.resolver.InternalFilterExceptionResolver} with the configured order
         */
        @Bean(name = EXCEPTION_RESOLVER)
        @ConditionalOnProperty(prefix = "spring.mvc.view", name = "suffix")
        @ConditionalOnBean(value = InternalResourceViewResolver.class)
        @ConditionalOnMissingBean(name = EXCEPTION_RESOLVER)
        public HandlerExceptionResolver internalFilterExceptionResolver(@Nonnull ObjectProvider<InternalResourceViewResolver> resolvers, @Nonnull ExceptionHandlerProperties properties) {
            InternalResourceViewResolver resolver = resolvers.getIfAvailable();
            if (resolver == null) {
                return null;
            }
            AbstractFilterExceptionResolver result = new InternalFilterExceptionResolver(resolver);
            Optional.ofNullable(properties.getExceptionResolver().getResolverOrder()).ifPresent(result::setOrder);
            return result;
        }
    }


    @Order(value = 2)
    static class Filter {
        /**
         * Returns the {@link org.springframework.boot.web.servlet.FilterRegistrationBean} that registers the filter exception handler filter
         *
         * @param properties the exception handler properties providing filter order, URL patterns and init parameters
         * @param resolver the exception resolver used by the filter to resolve handled exceptions
         *
         * @return a {@link org.springframework.boot.web.servlet.FilterRegistrationBean} configured for {@link com.jiovan.spring.starter.exceptionhandler.filter.FilterExceptionHandlerFilter}
         */
        @Bean
        @ConditionalOnBean(name = EXCEPTION_RESOLVER)
        @ConditionalOnMissingBean(value = FilterExceptionHandlerFilter.class, parameterizedContainer = FilterRegistrationBean.class)
        public FilterRegistrationBean<FilterExceptionHandlerFilter> filterExceptionHandlerFilterRegistration(@Nonnull ExceptionHandlerProperties properties, @Qualifier(value = EXCEPTION_RESOLVER) HandlerExceptionResolver resolver) {
            FilterExceptionHandlerFilter filter = new FilterExceptionHandlerFilter(properties, resolver);
            FilterRegistrationBean<FilterExceptionHandlerFilter> result = new FilterRegistrationBean<>(filter);
            ExceptionHandlerProperties.ExceptionFilter props = properties.getExceptionFilter();
            Optional.ofNullable(props.getFilerOrder()).ifPresent(result::setOrder);
            CollectionPlainWraps.ifNotEmpty(props.getFilterPaths(), result::setUrlPatterns);
            MapPlainWraps.ifNotEmpty(props.getFilterParams(), result::setInitParameters);
            return result;
        }
    }


    @Order(value = 3)
    static class Listener {
        /**
         * Returns the {@link com.jiovan.spring.starter.exceptionhandler.processor.SimpleEventMulticasterProcessor} that adjusts event multicaster priority
         *
         * @param properties the exception handler properties providing the processor order
         *
         * @return a {@link com.jiovan.spring.starter.exceptionhandler.processor.SimpleEventMulticasterProcessor} with the configured order
         */
        @Bean
        @ConditionalOnBooleanProperty(prefix = PROPERTIES_PREFIX + ".event-listener", name = "handle-event-multicaster", matchIfMissing = true)
        @ConditionalOnMissingBean
        public SimpleEventMulticasterProcessor simpleEventMulticasterProcessor(@Nonnull ExceptionHandlerProperties properties) {
            SimpleEventMulticasterProcessor result = new SimpleEventMulticasterProcessor();
            Optional.ofNullable(properties.getEventListener().getProcessorOrder()).ifPresent(result::setOrder);
            return result;
        }
    }
}
