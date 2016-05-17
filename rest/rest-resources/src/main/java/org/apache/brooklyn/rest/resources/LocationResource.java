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
package org.apache.brooklyn.rest.resources;

import static org.apache.brooklyn.rest.util.WebResourceUtils.serviceAbsoluteUriBuilder;

import java.net.URI;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

import org.apache.brooklyn.api.location.Location;
import org.apache.brooklyn.api.location.LocationDefinition;
import org.apache.brooklyn.api.typereg.RegisteredType;
import org.apache.brooklyn.core.location.LocationConfigKeys;
import org.apache.brooklyn.core.mgmt.internal.LocalManagementContext;
import org.apache.brooklyn.location.jclouds.JcloudsLocation;
import org.apache.brooklyn.rest.api.LocationApi;
import org.apache.brooklyn.rest.domain.HardwareSummary;
import org.apache.brooklyn.rest.domain.LocationSpec;
import org.apache.brooklyn.rest.domain.LocationSummary;
import org.apache.brooklyn.rest.domain.LoginCredentialsSummary;
import org.apache.brooklyn.rest.domain.NodeMetadataSummary;
import org.apache.brooklyn.rest.domain.OperatingSystemSummary;
import org.apache.brooklyn.rest.filter.HaHotStateRequired;
import org.apache.brooklyn.rest.transform.LocationTransformer;
import org.apache.brooklyn.rest.transform.LocationTransformer.LocationDetailLevel;
import org.apache.brooklyn.rest.util.EntityLocationUtils;
import org.apache.brooklyn.rest.util.WebResourceUtils;
import org.apache.brooklyn.util.collections.MutableMap;
import org.apache.brooklyn.util.exceptions.Exceptions;
import org.apache.brooklyn.util.text.NaturalOrderComparator;
import org.apache.brooklyn.util.text.Strings;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OperatingSystem;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

@SuppressWarnings("deprecation")
@HaHotStateRequired
public class LocationResource extends AbstractBrooklynRestResource implements LocationApi {

    private static final Logger log = LoggerFactory.getLogger(LocationResource.class);

    private final Set<String> specsWarnedOnException = Sets.newConcurrentHashSet();

    @Override
    public List<LocationSummary> list() {
        Function<LocationDefinition, LocationSummary> transformer = new Function<LocationDefinition, LocationSummary>() {
            @Override
            public LocationSummary apply(LocationDefinition l) {
                try {
                    return LocationTransformer.newInstance(mgmt(), l, LocationDetailLevel.LOCAL_EXCLUDING_SECRET, ui.getBaseUriBuilder());
                } catch (Exception e) {
                    Exceptions.propagateIfFatal(e);
                    String spec = l.getSpec();
                    if (spec == null || specsWarnedOnException.add(spec)) {
                        log.warn("Unable to find details of location {} in REST call to list (ignoring location): {}", l, e);
                        if (log.isDebugEnabled()) log.debug("Error details for location " + l, e);
                    } else {
                        if (log.isTraceEnabled())
                            log.trace("Unable again to find details of location {} in REST call to list (ignoring location): {}", l, e);
                    }
                    return null;
                }
            }
        };
        return FluentIterable.from(brooklyn().getLocationRegistry().getDefinedLocations().values())
                .transform(transformer)
                .filter(LocationSummary.class)
                .toSortedList(nameOrSpecComparator());
    }

    private static NaturalOrderComparator COMPARATOR = new NaturalOrderComparator();
    private static Comparator<LocationSummary> nameOrSpecComparator() {
        return new Comparator<LocationSummary>() {
            @Override
            public int compare(LocationSummary o1, LocationSummary o2) {
                return COMPARATOR.compare(getNameOrSpec(o1).toLowerCase(), getNameOrSpec(o2).toLowerCase());
            }
        };
    }
    private static String getNameOrSpec(LocationSummary o) {
        if (Strings.isNonBlank(o.getName())) return o.getName();
        if (Strings.isNonBlank(o.getSpec())) return o.getSpec();
        return o.getId();
    }

    // this is here to support the web GUI's circles
    @Override
    public Map<String,Map<String,Object>> getLocatedLocations() {
      Map<String,Map<String,Object>> result = new LinkedHashMap<String,Map<String,Object>>();
      Map<Location, Integer> counts = new EntityLocationUtils(mgmt()).countLeafEntitiesByLocatedLocations();
      for (Map.Entry<Location,Integer> count: counts.entrySet()) {
          Location l = count.getKey();
          Map<String,Object> m = MutableMap.<String,Object>of(
                  "id", l.getId(),
                  "name", l.getDisplayName(),
                  "leafEntityCount", count.getValue(),
                  "latitude", l.getConfig(LocationConfigKeys.LATITUDE),
                  "longitude", l.getConfig(LocationConfigKeys.LONGITUDE)
              );
          result.put(l.getId(), m);
      }
      return result;
    }

    /** @deprecated since 0.7.0; REST call now handled by below (optional query parameter added) */
    @Deprecated
    public LocationSummary get(String locationId) {
        return get(locationId, false);
    }

    @Override
    public LocationSummary get(String locationId, String fullConfig) {
        return get(locationId, Boolean.valueOf(fullConfig));
    }

    public LocationSummary get(String locationId, boolean fullConfig) {
        LocationDetailLevel configLevel = fullConfig ? LocationDetailLevel.FULL_EXCLUDING_SECRET : LocationDetailLevel.LOCAL_EXCLUDING_SECRET;
        Location l1 = mgmt().getLocationManager().getLocation(locationId);
        if (l1!=null) {
            return LocationTransformer.newInstance(mgmt(), l1, configLevel, ui.getBaseUriBuilder());
        }

        LocationDefinition l2 = brooklyn().getLocationRegistry().getDefinedLocationById(locationId);
        if (l2==null) throw WebResourceUtils.notFound("No location matching %s", locationId);
        return LocationTransformer.newInstance(mgmt(), l2, configLevel, ui.getBaseUriBuilder());
    }

    @Override
    public Response create(LocationSpec locationSpec) {
        String name = locationSpec.getName();
        
        ImmutableList.Builder<String> yaml = ImmutableList.<String>builder().add(
                "brooklyn.catalog:",
                "  id: " + name,
                "  itemType: location",
                "  item:",
                "    type: "+locationSpec.getSpec());
        if (locationSpec.getConfig().size() > 0) {
            yaml.add("    brooklyn.config:");
            for (Map.Entry<String, ?> entry : locationSpec.getConfig().entrySet()) {
                yaml.add("      " + entry.getKey() + ": " + entry.getValue());
            }
        }

        String locationBlueprint = Joiner.on("\n").join(yaml.build());
        brooklyn().getCatalog().addItems(locationBlueprint);
        LocationDefinition l = brooklyn().getLocationRegistry().getDefinedLocationByName(name);
        URI ref = serviceAbsoluteUriBuilder(ui.getBaseUriBuilder(), LocationApi.class, "get").build(name);
        return Response.created(ref)
                .entity(LocationTransformer.newInstance(mgmt(), l, LocationDetailLevel.LOCAL_EXCLUDING_SECRET, ui.getBaseUriBuilder()))
                .build();
    }

    @Override
    @Deprecated
    public void delete(String locationId) {
        // TODO make all locations be part of the catalog, then flip the JS GUI to use catalog api
        if (deleteAllVersions(locationId)>0) return;
        throw WebResourceUtils.notFound("No catalog item location matching %s; only catalog item locations can be deleted", locationId);
    }

    @Override
    public List<NodeMetadataSummary> listNodes(String providerName) {
        final LocalManagementContext mgmt = new LocalManagementContext();
        try {

            List<LocationDefinition> locationDefinitions = ImmutableList.of(mgmt.getLocationRegistry().getDefinedLocationByName(providerName));
            //List<LocationDefinition> locationDefinitions = mgmt.getLocationRegistry().getDefinedLocations().values()
            return FluentIterable.from(locationDefinitions)
                    .transform(new Function<LocationDefinition, Location>() {
                        @Override
                        public Location apply(@Nullable LocationDefinition locationDef) {
                            return mgmt.getLocationManager().createLocation(mgmt.getLocationRegistry().getLocationSpec(locationDef).get());
                        }
                    })
                    .filter(Predicates.instanceOf(JcloudsLocation.class))
                    .transformAndConcat(new Function<Location, Iterable<NodeMetadata>>() {
                        @Override
                        public Iterable<NodeMetadata> apply(Location location) {
                            try {
                                return (Iterable<NodeMetadata>) ((JcloudsLocation) location).getComputeService().listNodes();
                            }catch (HttpResponseException e) {
                                log.debug("Can't access the cloud provider", e);
                                throw Throwables.propagate(e);
                            }
                        }
                    })
                    .transform(new Function<NodeMetadata, NodeMetadataSummary>() {
                    @Override
                    public NodeMetadataSummary apply(NodeMetadata input) {
                        HardwareSummary hardwareSummary = new HardwareToSummary().apply(input.getHardware());
                        NodeMetadataSummary.Builder builder = NodeMetadataSummary.builder()
                                .id(input.getId())
                                .name(input.getName())
                                .providerId(input.getProviderId())
                                .hostname(input.getHostname())
                                .imageId(input.getImageId())
                                .status(input.getStatus().name())
                                .hardwareSummary(hardwareSummary)
                                .loginPort(input.getLoginPort())
                                .privateAddresses(input.getPrivateAddresses())
                                .publicAddresses(input.getPublicAddresses())
                                .tags(input.getTags());

                        if (input.getCredentials() != null) {
                            LoginCredentialsSummary loginCredentialsSummary = new LoginCredentialsToSummary().apply(input.getCredentials());
                            builder.credentialsSummary(loginCredentialsSummary);
                        }
                        if (input.getOperatingSystem() != null) {
                            OperatingSystemSummary operatingSystemSummary = new OperatingSystemToSummary().apply(input.getOperatingSystem());
                            builder.operatingSystemSummary(operatingSystemSummary);
                        }
                        return builder.build();
                    }
                })
                .toList();
        } finally {
            mgmt.terminate();
        }
    }

    private int deleteAllVersions(String locationId) {
        RegisteredType item = mgmt().getTypeRegistry().get(locationId);
        if (item==null) return 0;
        brooklyn().getCatalog().deleteCatalogItem(item.getSymbolicName(), item.getVersion());
        return 1 + deleteAllVersions(locationId);
    }

    private static class HardwareToSummary implements Function<Hardware, HardwareSummary> {
        @Override
        public HardwareSummary apply(@Nullable Hardware input) {
            return HardwareSummary.builder()
                    .id(input.getId())
                    .name(input.getName())
                    .processors(input.getProcessors().size())
                    .ram(input.getRam())
                    .hypervisor(input.getHypervisor())
                    .isDeprecated(input.isDeprecated())
                    .build();
        }
    }

    private static class LoginCredentialsToSummary implements Function<LoginCredentials, LoginCredentialsSummary> {
        @Override
        public LoginCredentialsSummary apply(LoginCredentials input) {
            return LoginCredentialsSummary.builder()
                    .loginUser(input.getUser())
                    .password(input.getPassword())
                    .build();
        }
    }

    private static class OperatingSystemToSummary implements Function<OperatingSystem, OperatingSystemSummary> {
        @Override
        public OperatingSystemSummary apply(OperatingSystem input) {
            return OperatingSystemSummary.builder()
                    .name(input.getName())
                    .version(input.getVersion())
                    .description(input.getDescription())
                    .arch(input.getArch())
                    .is64Bit(input.is64Bit())
                    .family(input.getFamily().value())
                    .build();
        }
    }

}
