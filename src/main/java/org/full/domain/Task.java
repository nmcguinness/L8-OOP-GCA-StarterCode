package org.full.domain;

/**
 * Represents a task record in the system.
 * Standard bean-style getters and setters are provided for JSON serialization.
 */
public class Task
{
    // === Fields ===
    private int _id;
    private String _title;
    private String _status;

    // === Constructors ===
    /**
     * Creates an empty task.
     * This no-argument constructor is useful for frameworks such as Jackson.
     */
    public Task()
    {
        _id = 0;
        _title = "";
        _status = "";
    }

    /**
     * Creates a task with the supplied values.
     *
     * @param id The task id.
     * @param title The task title.
     * @param status The task status.
     */
    public Task(int id, String title, String status)
    {
        if (id < 0)
            throw new IllegalArgumentException("id must be >= 0");

        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title is required");

        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status is required");

        _id = id;
        _title = title.trim();
        _status = status.trim().toUpperCase();
    }

    // === Properties ===
    /**
     * Gets the task id.
     *
     * @return The id.
     */
    public int getId()
    {
        return _id;
    }

    /**
     * Sets the task id.
     *
     * @param id The id to store.
     */
    public void setId(int id)
    {
        _id = id;
    }

    /**
     * Gets the task title.
     *
     * @return The title.
     */
    public String getTitle()
    {
        return _title;
    }

    /**
     * Sets the task title.
     *
     * @param title The title to store.
     */
    public void setTitle(String title)
    {
        _title = title;
    }

    /**
     * Gets the task status.
     *
     * @return The status.
     */
    public String getStatus()
    {
        return _status;
    }

    /**
     * Sets the task status.
     *
     * @param status The status to store.
     */
    public void setStatus(String status)
    {
        _status = status;
    }

    // === Housekeeping Methods ===
    /**
     * Returns a readable string representation of the task.
     *
     * @return The task as a string.
     */
    @Override
    public String toString()
    {
        return "Task{id=" + _id + ", title='" + _title + "', status='" + _status + "'}";
    }
}