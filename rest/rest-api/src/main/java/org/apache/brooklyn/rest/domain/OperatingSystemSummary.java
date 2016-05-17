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
@JsonDeserialize(builder = AutoValue_OperatingSystemSummary.Builder.class)
public abstract class OperatingSystemSummary {

    @Nullable
    @JsonProperty("family")
    public abstract String family();
    @Nullable
    @JsonProperty("name")
    public abstract String name();
    @Nullable
    @JsonProperty("arch")
    public abstract String arch();
    @Nullable
    @JsonProperty("version")
    public abstract String version();
    @JsonProperty("description")
    public abstract String description();
    @JsonProperty("is64Bit")
    public abstract boolean is64Bit();

    public static OperatingSystemSummary create(
            String family,
            String name,
            String arch,
            String version,
            String description,
            boolean is64Bit
    ) {
        return builder().family(family).name(name).arch(arch).version(version).description(description).is64Bit(is64Bit).build();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        @Nullable
        @JsonProperty("family")
        public abstract Builder family(String family);
        @Nullable
        @JsonProperty("name")
        public abstract Builder name(String name);
        @Nullable
        @JsonProperty("arch")
        public abstract Builder arch(String arch);
        @Nullable
        @JsonProperty("version")
        public abstract Builder version(String version);
        @JsonProperty("description")
        public abstract Builder description(String description);
        @JsonProperty("is64Bit")
        public abstract Builder is64Bit(boolean is64Bit);

        public abstract OperatingSystemSummary build();
    }

    public static Builder builder() {
        return new AutoValue_OperatingSystemSummary.Builder();
    }
}
