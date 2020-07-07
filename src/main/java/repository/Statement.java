package repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;

/**
 * Декор.
 * Получаем sql-запрос и параметры - params.
 * На основании этого решаем, что использовать
 * Statement, либо PreparedStatement.
 */
@RequiredArgsConstructor
class Statement {
    private final Session session;
    private final String query;
    private final Object[] params;

    static boolean execute(Session session, String query, Object... params) {
        return new Statement(session, query, params).execute();
    }

    static void executeQuery(Session session, String query, ResultProcess consumer, Object... params) {
        new Statement(session, query, params).executeQuery(consumer);
    }

    void executeQuery(ResultProcess consumer) {
        session.flush();
        session.doWork(connection -> {
            try (java.sql.Statement statement = createStatement(connection);
                    ResultSet resultSet = statement instanceof PreparedStatement ?
                            ((PreparedStatement) statement).executeQuery() : statement.executeQuery(query)) {
                while (!resultSet.isClosed() && resultSet.next()) {
                    consumer.process(resultSet);
                }
            }
        });
    }

    boolean execute() {
        session.flush();
        return session.doReturningWork(connection -> {
            try(java.sql.Statement statement = createStatement(connection)) {
                return statement instanceof PreparedStatement ?
                        ((PreparedStatement)statement).execute() : statement.execute(query);
            }
        });
    }

    private java.sql.Statement createStatement(Connection connection) throws SQLException {
        if (params != null && params.length > 0) {
            java.sql.PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (int iParam = 0; iParam < params.length; ++iParam) {
                preparedStatement.setObject(iParam + 1, params[iParam]);
            }
            return preparedStatement;
        } else {
            return connection.createStatement();
        }
    }

    public interface ResultProcess {
        void process(ResultSet resultSet) throws SQLException;
    }
}
