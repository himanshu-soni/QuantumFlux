package me.himanshusoni.quantumflux.sample.data.view;

import me.himanshusoni.quantumflux.model.annotation.Column.PrimaryKey;
import me.himanshusoni.quantumflux.model.annotation.Table;
import me.himanshusoni.quantumflux.model.generate.TableView;

/**
 * Created by Himanshu on 8/6/2015.
 */

@Table
public class BookAuthor implements TableView {

    @Override
    public String getTableViewSql() {
        return "SELECT b._ID, b.NAME, a.NAME FROM book b INNER JOIN author a ON b.author_id = a._id";
    }

    @PrimaryKey
    private int _id;

    private String bookName;
    private String authorName;


}

