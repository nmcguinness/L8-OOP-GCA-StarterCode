package org.full.dao;

import org.full.domain.Task;

import java.util.List;
import java.util.Optional;

/**
 * Defines DAO operations for working with {@link Task} records.
 */
public interface TaskDao
{
    /**
     * Retrieves all tasks.
     *
     * @return All tasks.
     * @throws Exception If the DAO operation fails.
     */
    List<Task> findAll() throws Exception;

    /**
     * Retrieves a task by id.
     *
     * @param id The task id.
     * @return The matching task, or empty if not found.
     * @throws Exception If the DAO operation fails.
     */
    Optional<Task> findById(int id) throws Exception;

    /**
     * Inserts a new task.
     *
     * @param title The task title.
     * @param status The task status.
     * @return The generated id.
     * @throws Exception If the DAO operation fails.
     */
    int insert(String title, String status) throws Exception;

    /**
     * Updates an existing task.
     *
     * @param id The task id.
     * @param title The new title.
     * @param status The new status.
     * @return True if exactly one row was updated.
     * @throws Exception If the DAO operation fails.
     */
    boolean update(int id, String title, String status) throws Exception;

    /**
     * Deletes a task by id.
     *
     * @param id The task id.
     * @return True if exactly one row was deleted.
     * @throws Exception If the DAO operation fails.
     */
    boolean deleteById(int id) throws Exception;

    /**
     * Retrieves all tasks with the supplied status.
     *
     * @param status The status to filter by.
     * @return Matching tasks.
     * @throws Exception If the DAO operation fails.
     */
    List<Task> findByStatus(String status) throws Exception;
}