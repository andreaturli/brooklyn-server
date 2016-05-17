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
@JsonDeserialize(builder = AutoValue_LoginCredentialsSummary.Builder.class)
public abstract class LoginCredentialsSummary {

    /**
     * @return the login user
     */
    @JsonProperty("loginUser")
    public abstract String loginUser();

    /**
     * @return the password of the login user or null
     */
    @Nullable
    @JsonProperty("password")
    public abstract String password();

    public static LoginCredentialsSummary create(
            String loginUser,
            String password
    ) {
        return builder().loginUser(loginUser).password(password).build();
    }

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        @JsonProperty("loginUser")
        public abstract Builder loginUser(String loginUser);
        @Nullable
        @JsonProperty("password")
        public abstract Builder password(String password);

        public abstract LoginCredentialsSummary build();
    }

    public static Builder builder() {
        return new AutoValue_LoginCredentialsSummary.Builder();
    }
}
