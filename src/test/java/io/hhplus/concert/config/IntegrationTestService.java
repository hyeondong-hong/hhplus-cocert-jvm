package io.hhplus.concert.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class IntegrationTestService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    public void setUp() {
        setUpSql(List.of(
                "concert.sql",
                "concert_schedule.sql",
                "concert_seat.sql",
                "service_entry.sql",
                "token.sql",
                "user.sql",
                "user_point.sql"
        ));
    }

    public void enrolledSetUp() {
        setUpSql(List.of(
                "concert.sql",
                "concert_schedule.sql",
                "concert_seat.sql",
                "service_entry_enrolled.sql",
                "token.sql",
                "user.sql",
                "user_point.sql"
        ));
    }

    private void setUpSql(List<String> sqlFiles) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        if (dataSource == null) {
            throw new RuntimeException("data source is null");
        }
        try (Connection conn = dataSource.getConnection()) {
            for (String sqlFile : sqlFiles) {
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("/sql/" + sqlFile));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void tearDown() {
        final String schemaName = extractSchemaName(datasourceUrl);
        entityManager.getMetamodel().getEntities().forEach(type -> {
            String entityName = type.getName();
            Table tableAnnotation = type.getJavaType().getAnnotation(Table.class);
            String tableName = (tableAnnotation != null && !tableAnnotation.name().isEmpty())
                    ? tableAnnotation.name()
                    : convertToSnakeCase(entityName);

            String truncateSql = "truncate table " + schemaName + "." + tableName + ";";
            jdbcTemplate.execute(truncateSql);
        });
    }

    private static String convertToSnakeCase(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    private String extractSchemaName(String url) {
        String pattern = "^jdbc:\\w+://\\w+:\\d+/([^/?]+)(?:\\?.+)?$";
        return url.replaceAll(pattern, "$1");
    }
}
