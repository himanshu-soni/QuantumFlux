package info.quantumflux.sample.data;

import info.quantumflux.model.annotation.Column.Column;
import info.quantumflux.model.annotation.Column.PrimaryKey;
import info.quantumflux.model.annotation.Table;

/**
 * Created by Himanshu on 8/5/2015.
 */

@Table(tableName = "publishers")
public class Publisher {
    @PrimaryKey
    public long id;

    @Column(required = true)
    public String name;
}
