package info.quantumflux.model.generate;


import info.quantumflux.model.annotation.Index;
import info.quantumflux.model.annotation.TableConstraint;
import info.quantumflux.model.util.QuantumFluxException;

import java.util.Arrays;
import java.util.Iterator;

/**
 * The java object will be converted to a table details object and the supplied information is then used to generate the
 * statements.
 */
public class TableGenerator {

    public static String generateTableDrop(TableDetails tableDetails, boolean prettyPrint) {
        StringBuilder tableQuery = new StringBuilder();

        prettyPrint(0, prettyPrint, tableQuery);
        tableQuery.append("DROP TABLE IF EXISTS ");
        tableQuery.append(tableDetails.getTableName());
        tableQuery.append(";");

        return tableQuery.toString();
    }

    public static String generateTableCreate(TableDetails tableDetails, boolean prettyPrint) {
        StringBuilder tableQuery = new StringBuilder();

        prettyPrint(0, prettyPrint, tableQuery);
        tableQuery.append("CREATE TABLE ");
        tableQuery.append(tableDetails.getTableName());

        prettyPrint(1, prettyPrint, tableQuery);
        tableQuery.append("(");
        prettyPrint(1, prettyPrint, tableQuery);

        Iterator<TableDetails.ColumnDetails> columnIterator = tableDetails.getColumns().iterator();
        while (columnIterator.hasNext()) {
            TableDetails.ColumnDetails columnDetails = columnIterator.next();

            prettyPrint(2, prettyPrint, tableQuery);
            tableQuery.append(createColumnDefinition(columnDetails));

            if (columnIterator.hasNext()) tableQuery.append(", ");
        }
        Iterator<TableConstraint> tableConstraintIterator = tableDetails.getConstraints().iterator();
        while (tableConstraintIterator.hasNext()) {
            TableConstraint tableConstraint = tableConstraintIterator.next();

            prettyPrint(3, prettyPrint, tableQuery);
            tableQuery.append(createTableConstraint(tableConstraint));

            if (tableConstraintIterator.hasNext()) tableQuery.append(",");
        }

        prettyPrint(1, prettyPrint, tableQuery);
        tableQuery.append(");");
        prettyPrint(1, prettyPrint, tableQuery);

        for (Index index : tableDetails.getIndices()) {

            prettyPrint(1, prettyPrint, tableQuery);
            tableQuery.append("CREATE INDEX ");
            tableQuery.append(index.indexName());
            tableQuery.append(" ON ");
            tableQuery.append(tableDetails.getTableName());
            tableQuery.append(" (");

            for (int i = 0; i < index.indexColumns().length; i++) {

                String column = index.indexColumns()[i];
                tableQuery.append(column);

                if ((i + 1) < index.indexColumns().length) tableQuery.append(", ");
            }
            tableQuery.append(");");
            prettyPrint(1, prettyPrint, tableQuery);
        }

        return tableQuery.toString();
    }

    private static void prettyPrint(int tabSpace, boolean prettyPrint, StringBuilder tableQuery) {
        if (prettyPrint) {
            tableQuery.append("\n");

            for (int i = 0; i < tabSpace; i++) {
                tableQuery.append("\t");
            }
        }
    }

    private static StringBuilder createColumnDefinition(TableDetails.ColumnDetails columnDetails) {

        StringBuilder columnDefinition = new StringBuilder();
        columnDefinition.append(columnDetails.getColumnName());
        columnDefinition.append(" ");
        columnDefinition.append(columnDetails.getColumnTypeMapping().getSqlColumnTypeName());

        if (columnDetails.isPrimaryKey()) {
            columnDefinition.append(" PRIMARY KEY");
            if (columnDetails.isAutoIncrement()) columnDefinition.append(" AUTOINCREMENT");
        } else if (columnDetails.isUnique()) columnDefinition.append(" UNIQUE");
        else if (columnDetails.isRequired()) columnDefinition.append(" NOT NULL");

        return columnDefinition;
    }


    private static StringBuilder createTableConstraint(TableConstraint constraint) {
        StringBuilder constraintDef = new StringBuilder();
        constraintDef.append("CONSTRAINT ");
        constraintDef.append(constraint.name());

        switch (constraint.constraintType()) {
            case PRIMARY_KEY:
                constraintDef.append(" PRIMARY KEY ");
                break;
            case UNIQUE:
                constraintDef.append(" UNIQUE ");
                break;
            default:
                throw new QuantumFluxException("Constraint Type not supported: " + constraint.constraintType());
        }

        Iterator<String> columnIterator = Arrays.asList(constraint.constraintColumns()).iterator();
        constraintDef.append("(");
        while (columnIterator.hasNext()) {
            String columnName = columnIterator.next();
            constraintDef.append(columnName);

            if (columnIterator.hasNext()) constraintDef.append(", ");
        }
        constraintDef.append(")");

        return constraintDef;
    }
}
