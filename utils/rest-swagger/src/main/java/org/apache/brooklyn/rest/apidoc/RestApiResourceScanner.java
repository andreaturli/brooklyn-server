/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.brooklyn.rest.apidoc;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;

import io.swagger.annotations.Api;
import io.swagger.config.SwaggerConfig;
import io.swagger.jaxrs.config.AbstractScanner;
import io.swagger.jaxrs.config.JaxrsScanner;
import io.swagger.models.Swagger;


/**
 * Much like DefaultJaxrsScanner, but looks at annotations of ancestors as well.
 *
 * For instance, if a resource implementation exposes an annotated interface,
 * that interface will be added as well.
 *
 */
public class RestApiResourceScanner extends AbstractScanner implements JaxrsScanner, SwaggerConfig {

    private Set<Class<?>> apiClasses = null;

    public RestApiResourceScanner() {}

    public RestApiResourceScanner(Collection<Class<?>> resourceClasses) {
        this.apiClasses = new HashSet<>();
        addAnnotatedClasses(apiClasses, resourceClasses);
    }

    private void addAnnotatedClasses(Set<Class<?>> output, Collection<Class<?>> classes) {
        for (Class<?> clz : classes) {
            if (clz.getAnnotation(Api.class) != null) {
                output.add(clz);
            }
            addAnnotatedClasses(output, Arrays.asList(clz.getInterfaces()));
        }
    }

    private synchronized void buildApiClasses(Application app) {
        if (apiClasses == null) {
            apiClasses = new HashSet<>();
            if (app != null) {
                Set<Class<?>> classes = app.getClasses();
                if (classes != null) {
                    addAnnotatedClasses(apiClasses, classes);
                }
                Set<Object> singletons = app.getSingletons();
                if (singletons != null) {
                    for (Object o : singletons) {
                        addAnnotatedClasses(apiClasses, Arrays.<Class<?>>asList(o.getClass()));
                    }
                }
            }
        }
    }

    @Override
    public Set<Class<?>> classesFromContext(Application app, ServletConfig sc) {
        buildApiClasses(app);
        return apiClasses;
    }

    @Override
    public Set<Class<?>> classes() {
        return new HashSet<>();
    }

    @Override
    public Swagger configure(Swagger swagger) {
        swagger.setBasePath("/v1");
        return swagger;
    }

    @Override
    public String getFilterClass() {
        return null;
    }

}
