package me.himanshusoni.quantumflux.sample.data;

import me.himanshusoni.quantumflux.model.annotation.ChangeListeners;
import me.himanshusoni.quantumflux.model.annotation.Column.PrimaryKey;
import me.himanshusoni.quantumflux.model.annotation.Table;
import me.himanshusoni.quantumflux.sample.data.view.BookAuthor;

/**
 * Created by Himanshu on 8/5/2015.
 */

@Table
@ChangeListeners(changeListeners = BookAuthor.class)
public class Book {

    @PrimaryKey
    public long id;
    public String name;
    public String isbn;

    public long authorId;

    public byte[] bookCover;
}
