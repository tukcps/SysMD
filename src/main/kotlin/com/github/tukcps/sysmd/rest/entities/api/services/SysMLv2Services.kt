package com.github.tukcps.sysmd.rest.entities.api.services


/**
 * Interface of the SysML v2 API, following standard.
 * Lacks the services for relationship and query.
 */
interface SysMLv2Services:
    ProjectService,
    ElementNavigationService,
    ProjectDataVersioningService,
    ProjectUsageService