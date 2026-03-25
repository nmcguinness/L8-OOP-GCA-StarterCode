package org.full.dao;

import org.full.domain.Task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link TaskDao}.
 * Uses prepared statements for all SQL operations.
 */
public class JdbcTaskDao implements TaskDao
{
    // === Fields ===
    private final String _url;
    private final String _user;
    private final String _pass;

    // === Constructors ===
    /**
     * Creates a JDBC DAO using the supplied connection settings.
     *
     * @param url JDBC connection URL.
     * @param user Database username.
     * @param pass Database password.
     */
    public JdbcTaskDao(String url, String user, String pass)
    {
        if (url == null || url.isBlank())
            throw new IllegalArgumentException("url is required");

        _url = url.trim();
        _user = user;
        _pass = pass;
    }

    // === Methods ===
    /**
     * Opens a new JDBC connection.
     *
     * @return A live database connection.
     * @throws SQLException If the connection cannot be opened.
     */
    private Connection open() throws SQLException
    {
        return DriverManager.getConnection(_url, _user, _pass);
    }

    /**
     * Retrieves all tasks from the database ordered by id.
     *
     * @return A list of all tasks.
     * @throws Exception If the query fails.
     */
    @Override
    public List<Task> findAll() throws Exception
    {
        String sql = "SELECT id, title, status FROM tasks ORDER BY id";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            ArrayList<Task> out = new ArrayList<>();

            while (rs.next())
                out.add(mapRow(rs));

            return out;
        }
    }

    /**
     * Retrieves a single task by id.
     *
     * @param id The task id.
     * @return The matching task, or empty.
     * @throws Exception If the query fails.
     */
    @Override
    public Optional<Task> findById(int id) throws Exception
    {
        if (id <= 0)
            return Optional.empty();

        String sql = "SELECT id, title, status FROM tasks WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                    return Optional.empty();

                return Optional.of(mapRow(rs));
            }
        }
    }

    /**
     * Inserts a new task and returns its generated id.
     *
     * @param title The task title.
     * @param status The task status.
     * @return The generated id.
     * @throws Exception If the insert fails.
     */
    @Override
    public int insert(String title, String status) throws Exception
    {
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title is required");

        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status is required");

        String sql = "INSERT INTO tasks(title, status) VALUES (?, ?)";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, title.trim());
            ps.setString(2, status.trim().toUpperCase());

            int rows = ps.executeUpdate();

            if (rows != 1)
                throw new IllegalStateException("insert failed, rows=" + rows);

            try (ResultSet keys = ps.getGeneratedKeys())
            {
                if (!keys.next())
                    throw new IllegalStateException("no generated key returned");

                return keys.getInt(1);
            }
        }
    }

    /**
     * Updates an existing task.
     *
     * @param id The task id.
     * @param title The new title.
     * @param status The new status.
     * @return True if one row was updated.
     * @throws Exception If the update fails.
     */
    @Override
    public boolean update(int id, String title, String status) throws Exception
    {
        if (id <= 0)
            return false;

        if (title == null || title.isBlank())
            throw new IllegalArgumentException("title is required");

        if (status == null || status.isBlank())
            throw new IllegalArgumentException("status is required");

        String sql = "UPDATE tasks SET title = ?, status = ? WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, title.trim());
            ps.setString(2, status.trim().toUpperCase());
            ps.setInt(3, id);

            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Deletes a task by id.
     *
     * @param id The task id.
     * @return True if one row was deleted.
     * @throws Exception If the delete fails.
     */
    @Override
    public boolean deleteById(int id) throws Exception
    {
        if (id <= 0)
            return false;

        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Retrieves all tasks with the supplied status.
     *
     * @param status The status to filter by.
     * @return Matching tasks.
     * @throws Exception If the query fails.
     */
    @Override
    public List<Task> findByStatus(String status) throws Exception
    {
        if (status == null || status.isBlank())
            return List.of();

        String sql = "SELECT id, title, status FROM tasks WHERE status = ? ORDER BY id";

        try (Connection c = open();
             PreparedStatement ps = c.prepareStatement(sql))
        {
            ps.setString(1, status.trim().toUpperCase());

            try (ResultSet rs = ps.executeQuery())
            {
                ArrayList<Task> out = new ArrayList<>();

                while (rs.next())
                    out.add(mapRow(rs));

                return out;
            }
        }
    }

    /**
     * Maps the current result-set row to a {@link Task} object.
     *
     * @param rs The current result set row.
     * @return A populated task.
     * @throws SQLException If the row cannot be read.
     */
    private static Task mapRow(ResultSet rs) throws SQLException
    {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String status = rs.getString("status");
        return new Task(id, title, status);
    }
}