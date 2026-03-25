package org.full.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.full.shared.ClientRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Smarter console client that captures the id of an inserted task
 * and reuses it for later requests.
 */
public class TaskClient
{
    // === Static Fields ===
    private static final String HOST = "localhost";
    private static final int PORT = 9000;

    // === Methods ===
    /**
     * Entry point for the smart client test program.
     *
     * @param args Command-line arguments.
     * @throws Exception If communication fails.
     */
    public static void main(String[] args) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
        {
            System.out.println("Connected to server at " + HOST + ":" + PORT);

            sendAndPrint(out, in, mapper, createRequest("GET_ALL", null));

            Map<String, Object> insertPayload = new HashMap<>();
            insertPayload.put("title", "Smart client inserted task");
            insertPayload.put("status", "TODO");

            String insertResponse = sendAndReceive(out, in, mapper, createRequest("INSERT", insertPayload));

            int insertedId = extractTaskId(insertResponse, mapper);
            System.out.println("Inserted task id = " + insertedId);

            if (insertedId > 0)
            {
                Map<String, Object> byIdPayload = new HashMap<>();
                byIdPayload.put("id", insertedId);
                sendAndPrint(out, in, mapper, createRequest("GET_BY_ID", byIdPayload));

                Map<String, Object> updatePayload = new HashMap<>();
                updatePayload.put("id", insertedId);
                updatePayload.put("title", "Smart client updated task");
                updatePayload.put("status", "DONE");
                sendAndPrint(out, in, mapper, createRequest("UPDATE", updatePayload));

                Map<String, Object> filterPayload = new HashMap<>();
                filterPayload.put("status", "DONE");
                sendAndPrint(out, in, mapper, createRequest("FILTER", filterPayload));

                Map<String, Object> deletePayload = new HashMap<>();
                deletePayload.put("id", insertedId);
                sendAndPrint(out, in, mapper, createRequest("DELETE", deletePayload));
            }

            sendAndPrint(out, in, mapper, createRequest("DISCONNECT", null));
        }
    }

    /**
     * Creates a client request with the supplied request type and payload.
     *
     * @param requestType The protocol request type.
     * @param payload Optional payload map.
     * @return A populated ClientRequest object.
     */
    private static ClientRequest createRequest(String requestType, Map<String, Object> payload)
    {
        ClientRequest request = new ClientRequest();
        request.setRequestType(requestType);
        request.setPayload(payload);
        return request;
    }

    /**
     * Sends a request and prints the response.
     *
     * @param out Writer for socket output.
     * @param in Reader for socket input.
     * @param mapper Jackson mapper.
     * @param request The request to send.
     * @throws Exception If communication fails.
     */
    private static void sendAndPrint(PrintWriter out, BufferedReader in, ObjectMapper mapper, ClientRequest request) throws Exception
    {
        sendAndReceive(out, in, mapper, request);
    }

    /**
     * Sends a request and returns the response string.
     *
     * @param out Writer for socket output.
     * @param in Reader for socket input.
     * @param mapper Jackson mapper.
     * @param request The request to send.
     * @return The raw JSON response string.
     * @throws Exception If communication fails.
     */
    private static String sendAndReceive(PrintWriter out, BufferedReader in, ObjectMapper mapper, ClientRequest request) throws Exception
    {
        String json = mapper.writeValueAsString(request);

        System.out.println();
        System.out.println(">>> Sending:");
        System.out.println(json);

        out.println(json);

        String response = in.readLine();

        System.out.println("<<< Response:");
        System.out.println(response);

        return response;
    }

    /**
     * Extracts a task id from a server JSON response.
     * Expects the inserted task to appear in the "data" object with an "id" field.
     *
     * @param json The raw JSON response.
     * @param mapper Jackson mapper.
     * @return The task id, or -1 if not found.
     */
    private static int extractTaskId(String json, ObjectMapper mapper)
    {
        try
        {
            JsonNode root = mapper.readTree(json);
            JsonNode data = root.get("data");

            if (data != null && data.has("id"))
                return data.get("id").asInt(-1);
        }
        catch (Exception ignored)
        {
        }

        return -1;
    }
}