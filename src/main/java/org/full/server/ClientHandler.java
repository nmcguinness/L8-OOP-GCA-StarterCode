package org.full.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.full.dao.TaskDao;
import org.full.shared.ClientRequest;
import org.full.shared.ServerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles one connected client.
 * It reads one JSON request per line, dispatches it, and writes back one JSON response per line.
 */
public class ClientHandler implements Runnable
{
    // === Fields ===
    private final Socket _socket;
    private final ObjectMapper _mapper;
    private final RequestDispatcher _dispatcher;

    // === Constructors ===
    /**
     * Creates a handler for the supplied client socket.
     *
     * @param socket The client socket.
     * @param dao The DAO used by the request dispatcher.
     */
    public ClientHandler(Socket socket, TaskDao dao)
    {
        if (socket == null)
            throw new IllegalArgumentException("socket is required");

        if (dao == null)
            throw new IllegalArgumentException("dao is required");

        _socket = socket;
        _mapper = new ObjectMapper();
        _dispatcher = new RequestDispatcher(dao);
    }

    // === Methods ===
    /**
     * Runs the client session until the client disconnects or the stream ends.
     */
    @Override
    public void run()
    {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
             PrintWriter out = new PrintWriter(_socket.getOutputStream(), true))
        {
            String line;

            while ((line = in.readLine()) != null)
            {
                String response = handle(line);
                out.println(response);
            }
        }
        catch (IOException e)
        {
            System.out.println("Client disconnected: " + e.getMessage());
        }
        finally
        {
            try
            {
                _socket.close();
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * Converts a raw JSON request into a {@link ClientRequest},
     * dispatches it, and converts the response back into JSON.
     *
     * @param rawJson The raw request line.
     * @return A JSON response line.
     */
    private String handle(String rawJson)
    {
        try
        {
            ClientRequest req = _mapper.readValue(rawJson, ClientRequest.class);
            ServerResponse<?> response = _dispatcher.dispatch(req);
            return _mapper.writeValueAsString(response);
        }
        catch (Exception e)
        {
            return toErrorJson("malformed request: " + e.getMessage());
        }
    }

    /**
     * Builds a fallback JSON error response when normal serialization cannot be used.
     *
     * @param message The error message.
     * @return A raw JSON error response string.
     */
    private String toErrorJson(String message)
    {
        String safeMessage = message == null ? "unknown error" : message.replace("\"", "\\\"");
        return "{\"status\":\"ERROR\",\"message\":\"" + safeMessage + "\",\"data\":null}";
    }
}