package org.full.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.full.shared.ClientRequest;
import org.full.shared.RequestType;
import org.full.shared.ServerResponse;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskClientTest
{
    private static final String HOST = "localhost";
    private static final int PORT = 9001;

    private ObjectMapper mapper;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private int createdId;

    @BeforeEach
    void setUp() throws Exception
    {
        mapper = new ObjectMapper();
        socket = new Socket(HOST, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        createdId = -1;
    }

    @AfterEach
    void tearDown() throws Exception
    {
        try
        {
            // Send a DISCONNECT request so the server can end the session cleanly.
            ClientRequest request = new ClientRequest();
            request.setRequestType(String.valueOf(RequestType.DISCONNECT));
            request.setPayload(null);

            out.println(mapper.writeValueAsString(request));
            in.readLine();
        }
        catch (Exception ignored)
        {
        }

        // Close the client socket resources.
        if (socket != null && !socket.isClosed())
            socket.close();
    }

    @Test
    void testInsertSuccess() throws Exception
    {
        // Build an INSERT request with a title and status.
        ClientRequest request = new ClientRequest();
        request.setRequestType(String.valueOf(RequestType.INSERT));

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "JUnit Insert Test");
        payload.put("status", "TODO");
        request.setPayload(payload);

        // Send the request and read the raw JSON response.
        out.println(mapper.writeValueAsString(request));
        String rawJson = in.readLine();

        // Convert the JSON response back into a ServerResponse object.
        ServerResponse response = mapper.readValue(rawJson, ServerResponse.class);

        // Check the top-level response fields.
        assertEquals("OK", response.getStatus());
        assertEquals("task created", response.getMessage());
        assertNotNull(response.getData());

        // Extract the created task data and verify the returned values.
        Map<?, ?> taskData = (Map<?, ?>) response.getData();
        createdId = ((Number) taskData.get("id")).intValue();

        assertTrue(createdId > 0);
        assertEquals("JUnit Insert Test", taskData.get("title"));
        assertEquals("TODO", taskData.get("status"));
    }

    @Test
    void testGetAllSuccess() throws Exception
    {
        // Build a GET_ALL request with no payload.
        ClientRequest request = new ClientRequest();
        request.setRequestType(String.valueOf(RequestType.GET_ALL));
        request.setPayload(null);

        // Send the request and read the raw JSON response.
        out.println(mapper.writeValueAsString(request));
        String rawJson = in.readLine();

        // Convert the JSON response back into a ServerResponse object.
        ServerResponse response = mapper.readValue(rawJson, ServerResponse.class);

        // Check the top-level response fields.
        assertEquals("OK", response.getStatus());
        assertNotNull(response.getData());

        // Extract the returned task list and verify it is present.
        List<?> tasks = (List<?>) response.getData();
        assertTrue(tasks.size() >= 0);
    }

    @Test
    void testGetByIdSuccess() throws Exception
    {
        // First create a task so that there is a known id to read back.
        createdId = createTask("JUnit Read Test", "TODO");

        // Build a GET_BY_ID request using the created id.
        ClientRequest request = new ClientRequest();
        request.setRequestType(String.valueOf(RequestType.GET_BY_ID));

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", createdId);
        request.setPayload(payload);

        // Send the request and read the raw JSON response.
        out.println(mapper.writeValueAsString(request));
        String rawJson = in.readLine();

        // Convert the JSON response back into a ServerResponse object.
        ServerResponse response = mapper.readValue(rawJson, ServerResponse.class);

        // Check the top-level response fields.
        assertEquals("OK", response.getStatus());
        assertEquals("task found", response.getMessage());

        // Extract the returned task and verify its field values.
        Map<?, ?> taskData = (Map<?, ?>) response.getData();
        assertEquals(createdId, ((Number) taskData.get("id")).intValue());
        assertEquals("JUnit Read Test", taskData.get("title"));
        assertEquals("TODO", taskData.get("status"));
    }

    private int createTask(String title, String status) throws Exception
    {
        // Build an INSERT request for a helper-created task.
        ClientRequest request = new ClientRequest();
        request.setRequestType(String.valueOf(RequestType.INSERT));

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", title);
        payload.put("status", status);
        request.setPayload(payload);

        // Send the request and read the raw JSON response.
        out.println(mapper.writeValueAsString(request));
        String rawJson = in.readLine();

        // Convert the JSON response back into a ServerResponse object.
        ServerResponse response = mapper.readValue(rawJson, ServerResponse.class);

        // Extract the returned task id so tests can use it.
        Map<?, ?> taskData = (Map<?, ?>) response.getData();
        return ((Number) taskData.get("id")).intValue();
    }
}