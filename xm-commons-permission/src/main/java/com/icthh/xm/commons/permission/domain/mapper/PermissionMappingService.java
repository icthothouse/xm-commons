package com.icthh.xm.commons.permission.domain.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.icthh.xm.commons.permission.domain.Permission;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class PermissionMappingService {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final PermissionMsNameFilter permissionMsNameFilter;

    public Map<String, Permission> ymlToPermissions(String yml) {
        Map<String, Permission> result = new TreeMap<>();
        try {
            Map<String, Map<String, Set<Permission>>> map = mapper
                    .readValue(yml, new TypeReference<TreeMap<String, TreeMap<String, TreeSet<Permission>>>>() {
                    });
            map.entrySet().stream()
                    .filter(entry -> permissionMsNameFilter.filterPermission(entry.getKey()))
                    .filter(entry -> entry.getValue() != null)
                    .forEach(entry -> entry.getValue()
                            .forEach((roleKey, permissions) ->
                                    permissions.forEach(permission -> {
                                        permission.setMsName(entry.getKey());
                                        permission.setRoleKey(roleKey);
                                        result.put(entry.getKey() + ":" + roleKey + ":" + permission.getPrivilegeKey(), permission);
                                    })
                            ));
        } catch (Exception e) {
            log.error("Failed to create permissions collection from YML file, error: {}", e.getMessage(), e);
        }
        return Collections.unmodifiableMap(result);
    }

}
