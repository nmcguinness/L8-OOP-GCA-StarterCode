package org.full.server;

import org.full.dao.TaskDao;
import org.full.domain.Task;
import org.full.shared.ClientRequest;
import org.full.shared.RequestType;
import org.full.shared.ServerResponse;

import java.util.List;
import java.util.Optional;

/**
 * Routes a deserialized {@link ClientRequest} to the correct DAO-backed operation.
 * This keeps protocol decision logic separate from socket handling.
 */
public class RequestDispatcher
{
    // === Fields ===
    private final TaskDao _dao;

    // === Constructors ===
    /**
     * Creates a dispatcher using the supplied DAO.
     *
     * @param dao The DAO used to perform task operations.
     */
    public RequestDispatcher(TaskDao dao)
    {
        if (dao == null)
            throw new IllegalArgumentException("dao is required");

        _dao = dao;
    }

    // === Methods ===
    /**
     * Dispatches a client request to the correct handler based on request type.
     *
     * @param req The client request.
     * @return A server response describing the outcome.
     */
    public ServerResponse<?> dispatch(ClientRequest req)
    {
        if (req == null)
            return ServerResponse.error("request is null");

        String typeText = req.getRequestType();

        if (typeText == null || typeText.isBlank())
            return ServerResponse.error("requestType is required");

        try
        {
            RequestType type = RequestType.valueOf(typeText.trim().toUpperCase());

            switch (type)
            {
                case GET_ALL:
                    return handleGetAll();

                case GET_BY_ID:
                    return handleGetById(req);

                case INSERT:
                    return handleInsert(req);

                case UPDATE:
                    return handleUpdate(req);

                case DELETE:
                    return handleDelete(req);

                case FILTER:
                    return handleFilter(req);

                case DISCONNECT:
                    return ServerResponse.ok("goodbye", null);

                default:
                    return ServerResponse.error("unknown request type: " + typeText);
            }
        }
        catch (IllegalArgumentException e)
        {
            return ServerResponse.error("unknown request type: " + typeText);
        }
        catch (Exception e)
        {
            return ServerResponse.error("server error: " + e.getMessage());
        }
    }

    /**
     * Handles a request to retrieve all tasks.
     *
     * @return A response containing all tasks.
     * @throws Exception If the DAO operation fails.
     */
    private ServerResponse<List<Task>> handleGetAll() throws Exception
    {
        List<Task> tasks = _dao.findAll();
        return ServerResponse.ok("retrieved " + tasks.size() + " tasks", tasks);
    }

    /**
     * Handles a request to retrieve a task by id.
     *
     * @param req The client request.
     * @return A response containing the matching task, if found.
     * @throws Exception If the DAO operation fails.
     */
    private ServerResponse<?> handleGetById(ClientRequest req) throws Exception
    {
        int id = req.getInt("id");

        if (id <= 0)
            return ServerResponse.error("valid id is required");

        Optional<Task> task = _dao.findById(id);

        if (task.isEmpty())
            return ServerResponse.error("no task with id=" + id);

        return ServerResponse.ok("task found", task.get());
    }

    /**
     * Handles a request to insert a new task.
     *
     * @param req The client request.
     * @return A response containing the created task.
     * @throws Exception If the DAO operation fails.
     */
    private ServerResponse<?> handleInsert(ClientRequest req) throws Exception
    {
        String title = req.getString("title");
        String status = req.getString("status");

        if (title == null || title.isBlank())
            return ServerResponse.error("title is required");

        if (status == null || status.isBlank())
            return ServerResponse.error("status is required");

        int newId = _dao.insert(title, status);
        Optional<Task> created = _dao.findById(newId);

        if (created.isEmpty())
            return ServerResponse.error("insert succeeded but task not found");

        return ServerResponse.ok("task created", created.get());
    }

    /**
     * Handles a request to update an existing task.
     *
     * @param req The client request.
     * @return A response containing the updated task.
     * @throws Exception If the DAO operation fails.
     */
    private ServerResponse<?> handleUpdate(ClientRequest req) throws Exception
    {
        int id = req.getInt("id");
        String title = req.getString("title");
        String status = req.getString("status");

        if (id <= 0)
            return ServerResponse.error("valid id is required");

        if (title == null || title.isBlank())
            return ServerResponse.error("title is required");

        if (status == null || status.isBlank())
            return ServerResponse.error("status is required");

        Optional<Task> existing = _dao.findById(id);

        if (existing.isEmpty())
            return ServerResponse.error("no task with id=" + id);

        boolean updated = _dao.update(id, title, status);

        if (!updated)
            return ServerResponse.error("update failed for id=" + id);

        Optional<Task> refreshed = _dao.findById(id);

        if (refreshed.isEmpty())
            return ServerResponse.error("update succeeded but task not found");

        return ServerResponse.ok("task updated", refreshed.get());
    }

    /**
     * Handles a request to delete a task by id.
     *
     * @param req The client request.
     * @return A response indicating whether deletion succeeded.
     * @throws Exception If the DAO operation fails.
     */
    private ServerResponse<Void> handleDelete(ClientRequest req) throws Exception
    {
        int id = req.getInt("id");

        if (id <= 0)
            return ServerResponse.error("valid id is required");

        boolean deleted = _dao.deleteById(id);

        if (!deleted)
            return ServerResponse.error("no task with id=" + id);

        return ServerResponse.ok("task deleted", null);
    }

    /**
     * Handles a request to retrieve tasks by status.
     *
     * @param req The client request.
     * @return A response containing matching tasks.
     * @throws Exception If the DAO operation fails.
     */
    private ServerResponse<List<Task>> handleFilter(ClientRequest req) throws Exception
    {
        String status = req.getString("status");

        if (status == null || status.isBlank())
            return ServerResponse.error("status is required");

        List<Task> tasks = _dao.findByStatus(status);
        return ServerResponse.ok("retrieved " + tasks.size() + " matching tasks", tasks);
    }
}