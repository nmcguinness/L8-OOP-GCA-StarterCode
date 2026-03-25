package org.full.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a standard JSON response sent from the server to a client.
 *
 * @param <T> The type of the response data payload.
 */
public class ServerResponse<T>
{
    // === Fields ===
    private String _status;
    private String _message;
    private T _data;

    // === Constructors ===
    /**
     * Creates an empty response.
     * Required for Jackson deserialization.
     */
    public ServerResponse()
    {
    }

    /**
     * Creates a response with a status, message, and data payload.
     *
     * @param status Typically "OK" or "ERROR".
     * @param message Human-readable response message.
     * @param data Optional payload data.
     */
    public ServerResponse(String status, String message, T data)
    {
        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status is required");

        _status = status;
        _message = message;
        _data = data;
    }

    // === Properties ===
    public String getStatus()
    {
        return _status;
    }

    public void setStatus(String status)
    {
        _status = status;
    }

    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    public T getData()
    {
        return _data;
    }

    public void setData(T data)
    {
        _data = data;
    }

    // === Methods ===
    public static <T> ServerResponse<T> ok(String message, T data)
    {
        return new ServerResponse<>("OK", message, data);
    }

    public static <T> ServerResponse<T> error(String message)
    {
        return new ServerResponse<>("ERROR", message, null);
    }

    /**
     * Convenience helper for Java code only.
     * It should not appear in JSON.
     *
     * @return True when the status is OK.
     */
    @JsonIgnore
    public boolean isOk()
    {
        return "OK".equals(_status);
    }
}