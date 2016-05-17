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
package org.apache.brooklyn.rest.domain;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

@AutoValue
@JsonDeserialize(builder = AutoValue_HardwareSummary.Builder.class)
public abstract class HardwareSummary {

    /**
     * @return user defined name of the operating system.
     */
    @Nullable
    @JsonProperty("name")
    public abstract String name();

    /**
     *
     * @return unique id within your account on the provider
     */
    @JsonProperty("id")
    public abstract String id();

    /**
     * Amount of virtual or physical processors provided
     */
    @JsonProperty("processors")
    public abstract int processors();

    /**
     * Amount of RAM provided in MB (256M, 1740)
     */
    @JsonProperty("ram")
    public abstract int ram();

    /**
     * @return hypervisor type, if this is a virtual machine and the type is known
     */
    @Nullable
    @JsonProperty("hypervisor")
    public abstract String hypervisor();

    /**
     * True if usage of the hardware profile is now discouraged.
     */
    @JsonProperty("isDeprecated")
    public abstract boolean isDeprecated();

    public static HardwareSummary create(
            String name,
            String id,
            int processors,
            int ram,
            String hypervisor,
            boolean isDeprecated) {
        return builder().name(name).id(id).processors(processors).ram(ram).hypervisor(hypervisor).isDeprecated(isDeprecated).build();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        @Nullable
        @JsonProperty("name")
        public abstract Builder name(String name);
        @JsonProperty("id")
        public abstract Builder id(String id);
        @JsonProperty("processors")
        public abstract Builder processors(int processors);
        @JsonProperty("ram")
        public abstract Builder ram(int ram);
        @Nullable
        @JsonProperty("hypervisor")
        public abstract Builder hypervisor(String hypervisor);
        @JsonProperty("isDeprecated")
        public abstract Builder isDeprecated(boolean isDeprecated);

        public abstract HardwareSummary build();
    }

    public static Builder builder() {
        return new AutoValue_HardwareSummary.Builder();
    }
}
