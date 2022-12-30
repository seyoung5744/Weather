package zerobase.weather.repository;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

@Repository
public class JdbcMemoRepository {

    private final JdbcTemplate jdbcTemplate;


    // application에 설정한 jdbc datasource 정보를 활용하여 JdbcTemplate에 할당
    @Autowired
    public JdbcMemoRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Memo save(Memo memo) {
        String sql = "insert into memo values(?, ?)";
        jdbcTemplate.update(sql, memo.getId(), memo.getText());
        return memo;
    }

    public List<Memo> findAll() {
        String sql = "select * from memo";
        return jdbcTemplate.query(sql, memoRowMapper());
    }

    public Optional<Memo> findById(int id) {
        String sql = "select * from memo where id = ?";
        return jdbcTemplate.query(sql, memoRowMapper(), id).stream().findFirst();
    }

    // RowMapper : jdbc 를 통해서 DB에서 데이터를 가져오면 ResultSet {id=1, text='this is memo'} 형식으로 가져옴.
    // 이러한 형식을 결국엔 Spring Boot 의 Memo클래스에 대입시켜야함.
    // 그래서 이때 ResultSet에 Memo라는 형식으로 Mapping해주는 것을 RowMapper라고 한다.
    private RowMapper<Memo> memoRowMapper() {
        // ResultSet
        // {id=1, text='this is memo'}
        return (rs, rowNum) -> new Memo(
            rs.getInt("id"),
            rs.getString("text")
        );
    }


}
