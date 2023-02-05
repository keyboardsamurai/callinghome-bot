package com.keyboardsamurais.apps.db;

import com.keyboardsamurais.apps.db.model.ClassifiedItem;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class ClassifiedItemMapper implements ResultSetMapper<ClassifiedItem> {
    public ClassifiedItem map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return ClassifiedItem.builder()
                .id(r.getInt("id"))
                .title(r.getString("title"))
                .content(r.getString("content"))
                .url(r.getString("url"))
                .imageUrl(r.getString("imageUrl"))
                .created(Instant.ofEpochMilli(r.getLong("created")))
                .build();
    }
}
