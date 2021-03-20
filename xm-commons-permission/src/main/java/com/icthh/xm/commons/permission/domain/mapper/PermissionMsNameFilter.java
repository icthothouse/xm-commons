package com.icthh.xm.commons.permission.domain.mapper;

@FunctionalInterface
public interface PermissionMsNameFilter {
    boolean filterPermission(String permissionMsName);
}
