package info.quantumflux.model.generate;

import info.quantumflux.model.util.QuantumFluxException;

/**
 * Generates the statements required to drop and create views
 */
public class TableViewGenerator {

    public static String createDropViewStatement(TableDetails tableDetails) {
        return "DROP VIEW IF EXISTS " + tableDetails.getTableName();
    }

    public static String createViewStatement(TableDetails tableDetails, Class<? extends TableView> view) {
        TableView tableView;
        try {
            tableView = view.getConstructor().newInstance();
        } catch (Exception e) {
            throw new QuantumFluxException("Failed to instantiate view " + view.getSimpleName(), e);
        }

        return "CREATE VIEW IF NOT EXISTS " + tableDetails.getTableName() + " AS " + tableView.getTableViewSql();
    }
}
