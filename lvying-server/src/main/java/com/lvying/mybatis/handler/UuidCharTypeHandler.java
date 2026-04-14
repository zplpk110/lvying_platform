package com.lvying.mybatis.handler;

import com.lvying.util.UuidStrings;
import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

/**
 * UUID 与 JDBC 互转：写入为32 位无连字符（{@code CHAR(32)}）；读取兼容该格式、36 位标准串及 {@code BINARY(16)}。
 */
@MappedTypes(UUID.class)
@MappedJdbcTypes({
  JdbcType.CHAR,
  JdbcType.VARCHAR,
  JdbcType.BINARY,
  JdbcType.VARBINARY,
  JdbcType.OTHER
})
public class UuidCharTypeHandler extends BaseTypeHandler<UUID> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, UuidStrings.compact(parameter));
  }

  @Override
  public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return read(rs, columnName);
  }

  @Override
  public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    byte[] bytes = rs.getBytes(columnIndex);
    if (bytes != null && bytes.length == 16) {
      return uuidFrom16Bytes(bytes);
    }
    String s = rs.getString(columnIndex);
    return parseUuidString(s);
  }

  @Override
  public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    byte[] bytes = cs.getBytes(columnIndex);
    if (bytes != null && bytes.length == 16) {
      return uuidFrom16Bytes(bytes);
    }
    String s = cs.getString(columnIndex);
    return parseUuidString(s);
  }

  private static UUID read(ResultSet rs, String columnName) throws SQLException {
    byte[] bytes = rs.getBytes(columnName);
    if (bytes != null && bytes.length == 16) {
      return uuidFrom16Bytes(bytes);
    }
    return parseUuidString(rs.getString(columnName));
  }

  private static UUID uuidFrom16Bytes(byte[] bytes) {
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    return new UUID(bb.getLong(), bb.getLong());
  }

  private static UUID parseUuidString(String s) {
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.isEmpty()) {
      return null;
    }
    return UuidStrings.parseLenient(s);
  }
}
