package info.quantumflux.model.generate;

import android.text.TextUtils;

import info.quantumflux.model.annotation.Index;
import info.quantumflux.model.annotation.TableConstraint;
import info.quantumflux.model.map.SqlColumnMapping;
import info.quantumflux.model.util.QuantumFluxException;

import java.lang.reflect.Field;
import java.util.*;

/**
 * This class will contain all of the information retrieved from the reflection of a java object.
 * The goal is to do the reflection once, and then use this object from there on, onwards.  This should help us a bit with performance
 * and it contains use full quick shortcuts for manipulating java objects to and from sql.
 */
public class TableDetails {

    private final String mTableName;
    private final String mAuthority;
    private final Class mTableClass;
    private final List<ColumnDetails> mColumns = new LinkedList<ColumnDetails>();
    private final List<Index> mIndices = new LinkedList<Index>();
    private final List<TableConstraint> mConstraints = new LinkedList<TableConstraint>();
    private final List<Class<?>> mChangeListener = new LinkedList<Class<?>>();

    public TableDetails(String tableName, String authority, Class tableClass) {
        this.mTableName = tableName;
        this.mAuthority = authority;
        this.mTableClass = tableClass;
    }

    public String getTableName() {
        return mTableName;
    }

    public String getAuthority() {
        return mAuthority;
    }

    public Class getTableClass() {
        return mTableClass;
    }

    public ColumnDetails findPrimaryKeyColumn() {
        for (int i = 0; i < mColumns.size(); i++) {
            ColumnDetails column = mColumns.get(i);
            if (column.isPrimaryKey()) return column;
        }
        return null;
    }

    public String[] getColumnNames() {
        String[] columnNames = new String[mColumns.size()];
        for (int i = 0; i < mColumns.size(); i++) {
            ColumnDetails columnDetails = mColumns.get(i);
            columnNames[i] = columnDetails.getColumnName();
        }
        return columnNames;
    }

    public List<ColumnDetails> getColumns() {
        return Collections.unmodifiableList(mColumns);
    }

    public ColumnDetails findColumn(String name) {
        for (int i = 0; i < mColumns.size(); i++) {
            ColumnDetails column = mColumns.get(i);
            if (column.getColumnName().equalsIgnoreCase(name))
                return column;
        }
        return null;
    }

    public void addColumn(ColumnDetails column) {
        mColumns.add(column);

        boolean hasPrimaryKey = false;
        for (ColumnDetails columnDetails : mColumns) {
            if (hasPrimaryKey && columnDetails.isPrimaryKey())
                throw new QuantumFluxException("Table may only have one primary key constraint on column definition, is a table mConstraints to specify more than one");
            hasPrimaryKey = hasPrimaryKey || columnDetails.isPrimaryKey();
        }
    }

    public List<Index> getIndices() {
        return Collections.unmodifiableList(mIndices);
    }

    public void addIndex(Index index) {
        mIndices.add(index);
    }

    public List<Class<?>> getChangeListeners() {
        return Collections.unmodifiableList(mChangeListener);
    }

    public void addChangeListener(Class<?> clazz) {
        mChangeListener.add(clazz);
    }

    public Collection<TableConstraint> getConstraints() {
        return mConstraints;
    }

    public void addConstraint(TableConstraint contConstraint) {
        mConstraints.add(contConstraint);
    }

    /**
     * Contains all of the column information supplied by the {@link info.quantumflux.model.annotation.Column} and other annotations
     * on the java fields.  It also contains a column mapping that will be used to convert java objects to and from SQL.
     */
    public static class ColumnDetails {
        private final String mColumnName;
        private final Field mColumnField;
        private final SqlColumnMapping mColumnMapping;
        //        private final Class<?> mReference;
        private final boolean isPrimaryKey;
        private final boolean isUnique;
        private final boolean isRequired;
        private final boolean isAutoIncrement;
        private final boolean mNotifyChanges;

        public ColumnDetails(String columnName, Field columnField, SqlColumnMapping columnTypeMapping,
                             // Class<?> references,
                             boolean primaryKey, boolean unique, boolean required, boolean autoIncrement,
                             boolean notifyChanges) {
            this.mColumnName = columnName;
            this.mColumnField = columnField;
            this.mColumnMapping = columnTypeMapping;
//            this.mReference = references;
            this.isPrimaryKey = primaryKey || autoIncrement;
            this.isUnique = unique;
            this.isRequired = required;
            this.isAutoIncrement = autoIncrement;
            this.mNotifyChanges = notifyChanges;

            if (primaryKey && !required) {
                throw new QuantumFluxException("Column must be not isRequired if primary key is set");
            }

            if (TextUtils.isEmpty(columnName)) {
                throw new QuantumFluxException("A valid column name needs to be provided");
            }

            columnField.setAccessible(true);
        }

        public String getColumnName() {
            return mColumnName;
        }

        public SqlColumnMapping getColumnTypeMapping() {
            return mColumnMapping;
        }

        public Field getColumnField() {
            return mColumnField;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        public boolean isUnique() {
            return isUnique;
        }

        public boolean isRequired() {
            return isRequired;
        }

        public boolean isAutoIncrement() {
            return isAutoIncrement;
        }

        public boolean notifyChanges() {
            return mNotifyChanges;
        }

//        public Class<?> getReference() {
//            return mReference;
//        }
    }
}
