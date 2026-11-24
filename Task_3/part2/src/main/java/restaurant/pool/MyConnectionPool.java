package restaurant.pool;

import restaurant.db.ConnectionProvider;
import restaurant.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MyConnectionPool implements ConnectionProvider, AutoCloseable {
    private static final Logger LOG = Logger.getLogger(MyConnectionPool.class.getName());

    private final DatabaseConfig config;
    private final int capacity;
    private final long waitMillis;
    private final ArrayDeque<Connection> idle = new ArrayDeque<>();

    // Includes connections currently being opened. Guarded by this pool's monitor.
    private int size;
    private boolean closed;

    public MyConnectionPool(DatabaseConfig config, int capacity, long waitMillis) {
        if (capacity < 1 || waitMillis < 1) {
            throw new IllegalArgumentException("Размер пула и время ожидания должны быть положительными.");
        }
        this.config = config;
        this.capacity = capacity;
        this.waitMillis = waitMillis;
    }

    @Override
    public Lease borrow() throws SQLException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitMillis);
        while (true) {
            Connection connection = takeIdleOrReserve(deadline);
            if (connection == null) {
                connection = openReservedConnection();
            } else if (!isUsable(connection)) {
                discard(connection);
                continue;
            }
            synchronized (this) {
                if (!closed) {
                    return new PooledLease(connection);
                }
            }
            discard(connection);
            throw new SQLException("Пул соединений закрыт.");
        }
    }

    private synchronized Connection takeIdleOrReserve(long deadline) throws SQLException {
        while (!closed && idle.isEmpty() && size == capacity) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                throw new SQLTransientConnectionException("Истекло время ожидания соединения из пула.");
            }
            try {
                TimeUnit.NANOSECONDS.timedWait(this, remaining);
            } catch (InterruptedException failure) {
                Thread.currentThread().interrupt();
                throw new SQLTransientConnectionException("Ожидание соединения прервано.", failure);
            }
        }
        if (closed) {
            throw new SQLException("Пул соединений закрыт.");
        }
        Connection connection = idle.pollFirst();
        if (connection == null) {
            size++;
        }
        return connection;
    }

    private Connection openReservedConnection() throws SQLException {
        try {
            return DriverManager.getConnection(config.url(), config.user(), config.password());
        } catch (SQLException | RuntimeException failure) {
            freeSlot();
            throw failure;
        }
    }

    private boolean isUsable(Connection connection) {
        try {
            if (connection.isValid(1)) {
                return true;
            }
            LOG.warning("Пул обнаружил недоступное соединение.");
        } catch (SQLException failure) {
            LOG.log(Level.WARNING, "Не удалось проверить соединение.", failure);
        }
        return false;
    }

    private void release(Connection connection) {
        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
            if (connection.isReadOnly()) {
                connection.setReadOnly(false);
            }
            connection.clearWarnings();
        } catch (SQLException failure) {
            LOG.log(Level.WARNING, "Соединение удалено из пула после ошибки очистки.", failure);
            discard(connection);
            return;
        }

        synchronized (this) {
            if (!closed) {
                idle.addLast(connection);
                notifyAll();
                return;
            }
        }
        discard(connection);
    }

    private void discard(Connection connection) {
        try {
            closePhysical(connection);
        } finally {
            freeSlot();
        }
    }

    private synchronized void freeSlot() {
        size--;
        notifyAll();
    }

    @Override
    public void close() {
        ArrayList<Connection> toClose;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            toClose = new ArrayList<>(idle);
            idle.clear();
            notifyAll();
        }
        toClose.forEach(this::discard);
    }

    private void closePhysical(Connection connection) {
        try {
            connection.close();
        } catch (SQLException failure) {
            LOG.log(Level.WARNING, "Не удалось закрыть соединение.", failure);
        }
    }

    private final class PooledLease implements Lease {
        private final Connection physical;
        private final AtomicBoolean returned = new AtomicBoolean();

        private PooledLease(Connection physical) {
            this.physical = physical;
        }

        @Override
        public Connection connection() {
            if (returned.get()) {
                throw new IllegalStateException("Соединение уже возвращено в пул.");
            }
            return physical;
        }

        @Override
        public void close() {
            if (returned.compareAndSet(false, true)) {
                release(physical);
            }
        }
    }
}
