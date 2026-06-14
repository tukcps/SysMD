package com.github.tukcps.sysmd.rest.entities.api.entities.responseModels;

/**
 * @author Khushnood Adil Rafique
 */
@SuppressWarnings("unused")
public enum EntityOperationName {

    // Session controller
    CREATE_SESSION, DELETE_SESSION, POST_SYSMD, CREATE_ELEMENT, GET_ELEMENT, DELETE_ELEMENT, UPDATE_ELEMENT,

    // Project controller
    CREATE_PROJECT, GET_PROJECTS, UPDATE_PROJECT, DELETE_PROJECT,

    // Commit controller
    CREATE_COMMIT, GET_ALL_COMMITS, GET_COMMIT,

    // Element controller
    GET_ELEMENTS, GET_ELEMENT_BY_ID, GET_ELEMENT_BY_NAME,

    // User controller
    CREATE_USER_GROUP, CREATE_COMPANY, ADD_USER_TO_GROUP, ADD_DOMAIN_TO_GROUP, RECOMMENDATION,

    // Relationship controller
    CREATE_RELATIONSHIP, DELETE_RELATIONSHIP,

    // Query controller
    TREEVIEW_INFO
}

