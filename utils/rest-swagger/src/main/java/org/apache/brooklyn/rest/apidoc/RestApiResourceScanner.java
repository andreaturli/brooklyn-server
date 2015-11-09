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

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import io.swagger.jaxrs.config.AbstractScanner;
import io.swagger.jaxrs.config.JaxrsScanner;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.ws.rs.core.Application;
import org.apache.brooklyn.util.collections.MutableSet;

/**
 * Much like DefaultJaxrsScanner, but looks at annotations of ancestors as well.
 *
 * For instance, if a resource implementation exposes an annotated interface,
 * that interface will be added as well.
 *
 * @author Ciprian Ciubotariu <cheepeero@gmx.net>
 */
public class RestApiResourceScanner extends AbstractScanner implements JaxrsScanner {

    private Set<Class<?>> apiClasses = null;


    private void addAnnotatedClasses(Set<Class<?>> output, Set<Class<?>> classes) {
        for (Class<?> clz : classes) {
            if (clz.getAnnotation(Api.class) != null) {
                output.add(clz);
            }
            addAnnotatedClasses(output, Sets.newHashSet(clz.getInterfaces()));
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
                        addAnnotatedClasses(apiClasses, (MutableSet<Class<?>>) MutableSet.of(o.getClass()));
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

}
