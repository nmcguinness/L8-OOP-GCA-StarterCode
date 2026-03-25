package org.full.shared;

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
    /**
     * Gets the response status.
     *
     * @return The response status string.
     */
    public String getStatus()
    {
        return _status;
    }

    /**
     * Gets the response message.
     *
     * @return The message explaining the result.
     */
    public String getMessage()
    {
        return _message;
    }

    /**
     * Gets the response payload data.
     *
     * @return The payload data, or null.
     */
    public T getData()
    {
        return _data;
    }

    // === Methods ===
    /**
     * Creates a successful response.
     *
     * @param message Success message.
     * @param data Optional payload.
     * @param <T> Payload type.
     * @return An OK response.
     */
    public static <T> ServerResponse<T> ok(String message, T data)
    {
        return new ServerResponse<>("OK", message, data);
    }

    /**
     * Creates an error response.
     *
     * @param message Error message.
     * @param <T> Payload type.
     * @return An ERROR response.
     */
    public static <T> ServerResponse<T> error(String message)
    {
        return new ServerResponse<>("ERROR", message, null);
    }

    /**
     * Indicates whether the response represents success.
     *
     * @return True when the status is OK.
     */
    public boolean isOk()
    {
        return "OK".equals(_status);
    }
}