package me.himanshusoni.quantumflux.sample.data;

import me.himanshusoni.quantumflux.model.QuantumFluxRecord;
import me.himanshusoni.quantumflux.model.annotation.ChangeListeners;
import me.himanshusoni.quantumflux.sample.data.view.BookAuthor;

/**
 * Created by Himanshu on 8/5/2015.
 */
@ChangeListeners(changeListeners = BookAuthor.class)
public class Author extends QuantumFluxRecord<Author> {
    public String name;
}
