package info.quantumflux.sample.data;

import info.quantumflux.model.QuantumFluxRecord;
import info.quantumflux.model.annotation.ChangeListeners;
import info.quantumflux.sample.data.view.BookAuthor;

/**
 * Created by Himanshu on 8/5/2015.
 */
@ChangeListeners(changeListeners = BookAuthor.class)
public class Author extends QuantumFluxRecord<Author> {
    public String name;
}
