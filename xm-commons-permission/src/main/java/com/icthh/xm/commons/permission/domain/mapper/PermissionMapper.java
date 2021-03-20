package com.icthh.xm.commons.permission.domain.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.icthh.xm.commons.permission.domain.Permission;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@UtilityClass
@Slf4j
public class PermissionMapper {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    /**
     * Convert permissions collection to yml string.
     *
     * @param permissions collection
     * @return yml string
     */
    public String permissionsToYml(Collection<Permission> permissions) {
        try {
            Map<String, Map<String, Set<Permission>>> map = new TreeMap<>();
            permissions.forEach(permission -> {
                map.putIfAbsent(permission.getMsName(), new TreeMap<>());
                map.get(permission.getMsName()).putIfAbsent(permission.getRoleKey(), new TreeSet<>());
                map.get(permission.getMsName()).get(permission.getRoleKey()).add(permission);
            });
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Failed to create permissions YML file from collection, error: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Convert permissions yml string to collection.
     *
     * @param yml string
     * @return permissions map
     */
    public Map<String, Permission> ymlToPermissions(String yml) {
        return ymlToPermissions(yml, null);
    }

    /**
     * Convert permissions yml string to map with role and privilege keys.
     * Return map fo specific service or all.
     *
     * @param yml    string
     * @param msName service name
     * @return permissions map
     *
     * @deprecated use PermissionMappingService.ymlToPermissions instead
     *
     */
    @Deprecated(forRemoval = true)
    public Map<String, Permission> ymlToPermissions(String yml, String msName) {
        Map<String, Permission> result = new TreeMap<>();
        try {
            Map<String, Map<String, Set<Permission>>> map = mapper
                .readValue(yml, new TypeReference<TreeMap<String, TreeMap<String, TreeSet<Permission>>>>() {
                });
            map.entrySet().stream()
                .filter(entry -> StringUtils.isBlank(msName) || StringUtils.startsWithIgnoreCase(entry.getKey(), msName))
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> entry.getValue()
                    .forEach((roleKey, permissions) ->
                                 permissions.forEach(permission -> {
                                     permission.setMsName(entry.getKey());
                                     permission.setRoleKey(roleKey);
                                     result.put(roleKey + ":" + permission.getPrivilegeKey(), permission);
                                 })
                    ));
        } catch (Exception e) {
            log.error("Failed to create permissions collection from YML file, error: {}", e.getMessage(), e);
        }
        return Collections.unmodifiableMap(result);
    }
}
