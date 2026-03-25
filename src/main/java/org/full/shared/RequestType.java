package org.full.shared;

/**
 * Enumerates the supported JSON protocol request types.
 */
public enum RequestType
{
    GET_ALL,
    GET_BY_ID,
    INSERT,
    UPDATE,
    DELETE,
    FILTER,
    DISCONNECT
}