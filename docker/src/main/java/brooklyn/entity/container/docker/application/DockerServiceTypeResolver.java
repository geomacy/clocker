/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package brooklyn.entity.container.docker.application;

import io.brooklyn.camp.brooklyn.spi.creation.BrooklynComponentTemplateResolver;
import io.brooklyn.camp.brooklyn.spi.creation.service.BrooklynServiceTypeResolver;
import io.brooklyn.camp.brooklyn.spi.creation.service.ServiceTypeResolver;
import io.brooklyn.camp.spi.PlatformComponentTemplate;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import brooklyn.catalog.CatalogItem;
import brooklyn.entity.Entity;
import brooklyn.entity.container.DockerAttributes;
import brooklyn.entity.proxying.EntitySpec;
import brooklyn.util.text.Strings;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * This converts {@link PlatformComponentTemplate} instances whose type is prefixed {@code docker:}
 * to Brooklyn {@link EntitySpec} instances.
 */
public class DockerServiceTypeResolver extends BrooklynServiceTypeResolver {

    private static final Logger log = LoggerFactory.getLogger(ServiceTypeResolver.class);

    public static final String PREFIX = "docker";

    @Override
    public String getTypePrefix() { return PREFIX; }

    @Override
    public String getBrooklynType(String serviceType) {
        return VanillaDockerApplication.class.getName();
    }

    // docker: items are not in catalog
    @Override
    public CatalogItem<Entity, EntitySpec<?>> getCatalogItem(BrooklynComponentTemplateResolver resolver, String serviceType) {
        return null;
    }

    @Override
    public <T extends Entity> void decorateSpec(BrooklynComponentTemplateResolver resolver, EntitySpec<T> spec) {
        String dockerServiceType = Strings.removeFromStart(resolver.getDeclaredType(), PREFIX + ":");
        List<String> parts = Splitter.on(":").splitToList(dockerServiceType);
        if (parts.isEmpty() || parts.size() > 2) {
            throw new IllegalArgumentException("Docker serviceType cannot be parsed: " + dockerServiceType);
        }
        String imageName = Iterables.get(parts, 0);
        String imageTag = Iterables.get(parts, 1, "latest");
        log.debug("Creating Docker service entity with image {} and tag {}", imageName, imageTag);
        spec.configure(DockerAttributes.DOCKER_IMAGE_NAME, imageName);
        if (parts.size() == 2) {
            spec.configure(DockerAttributes.DOCKER_IMAGE_TAG, imageTag);
        }
        super.decorateSpec(resolver, spec);
    }
    
}
