package org.full.server;

import org.full.dao.JdbcTaskDao;
import org.full.dao.TaskDao;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Starts the JSON protocol server and listens for incoming TCP client connections.
 * Each client is handed to a {@link ClientHandler} using an {@link ExecutorService}.
 */
public class Server
{
    // === Fields ===
    private final int _port;
    private final TaskDao _dao;
    private final ExecutorService _pool;

    // === Constructors ===
    /**
     * Creates a server on the given port using the supplied DAO.
     *
     * @param port The TCP port to listen on.
     * @param dao The DAO used by request handlers.
     */
    public Server(int port, TaskDao dao)
    {
        if (port < 1_024 || port > 65_535)
            throw new IllegalArgumentException("port must be 1024-65535");

        if (dao == null)
            throw new IllegalArgumentException("dao is required");

        _port = port;
        _dao = dao;
        _pool = Executors.newCachedThreadPool();
    }

    // === Methods ===
    /**
     * Starts the server accept loop.
     * Each accepted socket is processed by a pooled {@link ClientHandler}.
     *
     * @throws IOException If the server socket cannot be opened or used.
     */
    public void start() throws IOException
    {
        System.out.println("Server listening on port " + _port);

        try (ServerSocket serverSocket = new ServerSocket(_port))
        {
            while (!Thread.currentThread().isInterrupted())
            {
                Socket client = serverSocket.accept();
                System.out.println("Client connected: " + client.getInetAddress());
                _pool.submit(new ClientHandler(client, _dao));
            }
        }
    }

    /**
     * Example entry point for launching the server.
     *
     * @param args Command-line arguments.
     * @throws Exception If startup fails.
     */
    public static void main(String[] args) throws Exception
    {
        String url = "jdbc:mysql://localhost:3306/taskhub?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        String user = "root";
        String pass = "";

        TaskDao dao = new JdbcTaskDao(url, user, pass);
        new Server(9000, dao).start();
    }
}