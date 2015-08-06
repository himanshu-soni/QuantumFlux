package info.quantumflux.sample.data;

import info.quantumflux.model.annotation.ChangeListeners;
import info.quantumflux.model.annotation.Column.PrimaryKey;
import info.quantumflux.model.annotation.References;
import info.quantumflux.model.annotation.Table;
import info.quantumflux.sample.data.view.BookAuthor;

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
