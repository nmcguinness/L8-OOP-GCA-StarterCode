package org.full.domain;

import org.full.dao.TaskDao;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Provides a small service layer over the {@link TaskDao}.
 * This allows business rules to be applied before DAO operations.
 */
public class TaskService
{
    // === Static Fields ===
    private static final Set<String> ALLOWED = Set.of("TODO", "DOING", "DONE");

    // === Fields ===
    private final TaskDao _dao;

    // === Constructors ===
    /**
     * Creates a service using the supplied DAO.
     *
     * @param dao The DAO used for persistence.
     */
    public TaskService(TaskDao dao)
    {
        if (dao == null)
            throw new IllegalArgumentException("dao is null");

        _dao = dao;
    }

    // === Methods ===
    /**
     * Creates a new task using a default TODO status.
     *
     * @param title The task title.
     * @return The generated task id.
     * @throws Exception If creation fails.
     */
    public int createTask(String title) throws Exception
    {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title is required");

        return _dao.insert(title, "TODO");
    }

    /**
     * Updates the status of an existing task after validating it.
     *
     * @param id The task id.
     * @param status The new status.
     * @return True when the status was updated successfully.
     * @throws Exception If DAO operations fail.
     */
    public boolean setStatus(int id, String status) throws Exception
    {
        if (id <= 0)
            return false;

        if (status == null || status.isBlank())
            return false;

        String s = status.trim().toUpperCase();

        if (!ALLOWED.contains(s))
            return false;

        Optional<Task> existing = _dao.findById(id);

        if (existing.isEmpty())
            return false;

        Task task = existing.get();
        return _dao.update(id, task.getTitle(), s);
    }

    /**
     * Retrieves one task by id.
     *
     * @param id The task id.
     * @return The matching task, or empty.
     * @throws Exception If DAO access fails.
     */
    public Optional<Task> get(int id) throws Exception
    {
        return _dao.findById(id);
    }

    /**
     * Retrieves all tasks.
     *
     * @return A list of all tasks.
     * @throws Exception If DAO access fails.
     */
    public List<Task> list() throws Exception
    {
        return _dao.findAll();
    }
}