package api.database;

import api.configs.Config;
import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class DBRequest {
    private RequestType requestType;
    private Table table;
    private List<Condition> conditions;
    private Class<?> extractAsClass;
    private String orderByColumn;   // имя колонки для сортировки
    private String orderDirection;  // "ASC" или "DESC"

    public enum RequestType {
        SELECT, INSERT, UPDATE, DELETE
    }

    // Возвращает один объект (первая строка)
    private <T> T mapSingleRow(ResultSet rs, Class<T> clazz) throws SQLException {
        if (rs.next()) {
            return mapRow(rs, clazz);
        }
        return null;
    }

    // Возвращает список объектов (все строки)
    private <T> List<T> mapRows(ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(mapRow(rs, clazz));
        }
        return list;
    }

    // Маппинг одной строки ResultSet в объект класса T
    private <T> T mapRow(ResultSet rs, Class<T> clazz) throws SQLException {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                String fieldName = field.getName();
                // Преобразование camelCase → snake_case
                String columnName = fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
                Object value;

                // Определение типа поля и безопасное извлечение значения из ResultSet
                if (field.getType() == Long.class || field.getType() == long.class) {
                    value = rs.getLong(columnName);
                    if (rs.wasNull() && field.getType() == Long.class) value = null;
                } else if (field.getType() == Double.class || field.getType() == double.class) {
                    value = rs.getDouble(columnName);
                    if (rs.wasNull() && field.getType() == Double.class) value = null;
                } else if (field.getType() == String.class) {
                    value = rs.getString(columnName);
                } else if (field.getType() == Integer.class || field.getType() == int.class) {
                    value = rs.getInt(columnName);
                    if (rs.wasNull() && field.getType() == Integer.class) value = null;
                } else if (field.getType() == java.util.Date.class) {
                    value = rs.getTimestamp(columnName);
                } else {
                    // fallback – попытка получить как Object
                    value = rs.getObject(columnName);
                }

                if (value != null) {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to map row to " + clazz.getSimpleName(), e);
        }
    }

    public <T> T extractAs(Class<T> clazz) {
        this.extractAsClass = clazz;
        return executeQuery(clazz);
    }

    public <T> List<T> extractListAs(Class<T> clazz) {
        this.extractAsClass = clazz;
        return executeQueryList(clazz);
    }

    // Вспомогательный метод для установки параметров
    private void setParameters(PreparedStatement stmt) throws SQLException {
        if (conditions != null) {
            for (int i = 0; i < conditions.size(); i++) {
                stmt.setObject(i + 1, conditions.get(i).getValue());
            }
        }
    }

    private <T> List<T> executeQueryList(Class<T> clazz) {
        String sql = buildSQL();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapRows(rs, clazz);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }

    private <T> T executeQuery(Class<T> clazz) {
        String sql = buildSQL();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapSingleRow(rs, clazz);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed", e);
        }
    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();

        switch (requestType) {
            case SELECT:
                sql.append("SELECT * FROM ").append(table);
                if (conditions != null && !conditions.isEmpty()) {
                    sql.append(" WHERE ");
                    for (int i = 0; i < conditions.size(); i++) {
                        if (i > 0) sql.append(" AND ");
                        sql.append(conditions.get(i).getColumn()).append(" ").append(conditions.get(i).getOperator()).append(" ?");
                    }
                }
                if (orderByColumn != null && !orderByColumn.isEmpty()) {
                    sql.append(" ORDER BY ").append(orderByColumn);
                    if (orderDirection != null && !orderDirection.isEmpty()) {
                        sql.append(" ").append(orderDirection);
                    } else {
                        sql.append(" ASC"); // по умолчанию
                    }
                }
                break;
            default:
                throw new UnsupportedOperationException("Request type " + requestType + " not implemented");
        }

        return sql.toString();
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(
                Config.getProperty("db.url"),
                Config.getProperty("db.username"),
                Config.getProperty("db.password")
        );
        return connection;
    }

    public static DBRequestBuilder builder() {
        return new DBRequestBuilder();
    }

    public static class DBRequestBuilder {
        private RequestType requestType;
        private Table table;
        private List<Condition> conditions = new ArrayList<>();
        private Class<?> extractAsClass;
        private String orderByColumn;
        private String orderDirection;

        public DBRequestBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public DBRequestBuilder where(Condition condition) {
            this.conditions.add(condition);
            return this;
        }

        public DBRequestBuilder table(Table table) {
            this.table = table;
            return this;
        }

        public DBRequestBuilder orderBy(String column) {
            this.orderByColumn = column;
            this.orderDirection = "ASC";
            return this;
        }

        public DBRequestBuilder orderByDesc(String column) {
            this.orderByColumn = column;
            this.orderDirection = "DESC";
            return this;
        }

        public <T> T extractAs(Class<T> clazz) {
            this.extractAsClass = clazz;
            DBRequest request = DBRequest.builder()
                    .requestType(requestType)
                    .table(table)
                    .conditions(conditions)
                    .extractAsClass(extractAsClass)
                    .orderByColumn(orderByColumn)
                    .orderDirection(orderDirection)
                    .build();
            return request.extractAs(clazz);
        }

        public <T> List<T> extractListAs(Class<T> clazz) {
            this.extractAsClass = clazz;
            DBRequest request = DBRequest.builder()
                    .requestType(requestType)
                    .table(table)
                    .conditions(conditions)
                    .extractAsClass(extractAsClass)
                    .orderByColumn(orderByColumn)
                    .orderDirection(orderDirection)
                    .build();
            return request.extractListAs(clazz);
        }
    }
}