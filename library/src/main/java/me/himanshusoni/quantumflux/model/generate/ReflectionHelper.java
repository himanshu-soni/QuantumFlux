package me.himanshusoni.quantumflux.model.generate;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import dalvik.system.DexFile;
import me.himanshusoni.quantumflux.QuantumFlux;
import me.himanshusoni.quantumflux.model.QuantumFluxRecord;
import me.himanshusoni.quantumflux.model.annotation.Authority;
import me.himanshusoni.quantumflux.model.annotation.ChangeListeners;
import me.himanshusoni.quantumflux.model.annotation.Column.Column;
import me.himanshusoni.quantumflux.model.annotation.Column.PrimaryKey;
import me.himanshusoni.quantumflux.model.annotation.Column.Unique;
import me.himanshusoni.quantumflux.model.annotation.Index;
import me.himanshusoni.quantumflux.model.annotation.Indices;
import me.himanshusoni.quantumflux.model.annotation.Table;
import me.himanshusoni.quantumflux.model.annotation.TableConstraint;
import me.himanshusoni.quantumflux.model.map.SqlColumnMapping;
import me.himanshusoni.quantumflux.model.map.SqlColumnMappingFactory;
import me.himanshusoni.quantumflux.model.util.ManifestHelper;
import me.himanshusoni.quantumflux.model.util.NamingUtils;

/**
 * This class will convert any valid Java Object marked with the {@link Table} annotation
 * to a valid {@link TableDetails} object.
 */
public class ReflectionHelper {

    /**
     * Creates a {@link TableDetails} object containing the reflection information retrieved from
     * the supplied java object.
     *
     * @param context         The context that can be used to get meta information
     * @param dataModelObject The object to analyse
     * @return The {@link TableDetails} containing the reflection information
     */
    public static TableDetails getTableDetails(Context context, Class<?> dataModelObject) {
        Table table = dataModelObject.getAnnotation(Table.class);
        String tableName = null;
        if (table == null) {
            tableName = NamingUtils.getSQLName(dataModelObject.getSimpleName());
        } else {
            tableName = TextUtils.isEmpty(table.tableName()) ? NamingUtils.getSQLName(dataModelObject.getSimpleName()) : table.tableName();
        }

        Authority authority = dataModelObject.getAnnotation(Authority.class);
        String authorityName = authority == null ? ManifestHelper.getAuthority(context) : authority.value();

        TableDetails tableDetails = new TableDetails(tableName, authorityName, dataModelObject);
        SqlColumnMappingFactory columnMappingFactory = QuantumFlux.getColumnMappingFactory();

        for (Field field : getAllObjectFields(dataModelObject)) {
            if (!isValidField(field)) continue;

            boolean autoIncrement = false;
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                autoIncrement = field.getAnnotation(PrimaryKey.class).autoIncrement();
            }

            Column column = null;

            String columnName = null;
            boolean required = false;
            boolean notifyChanges = true;
            if (field.isAnnotationPresent(Column.class)) {
                column = field.getAnnotation(Column.class);
                columnName = column.columnName();
                required = column.required();
                notifyChanges = column.notifyChanges();
            }
            if (TextUtils.isEmpty(columnName)) {
                columnName = NamingUtils.getSQLName(field.getName());
            }

            boolean primaryKey = field.isAnnotationPresent(PrimaryKey.class);
            boolean unique = field.isAnnotationPresent(Unique.class);

//            Class<?> reference = null;
//            if (field.isAnnotationPresent(References.class)) {
//                if (!field.isAccessible()) {
//                    field.setAccessible(true);
//                }
//                Reference referenceTo = (Reference) field.getAnnotation(References.class);
//                reference = referenceTo.getClass();
//            }

            SqlColumnMapping columnMapping = columnMappingFactory.findColumnMapping(field.getType());

            tableDetails.addColumn(
                    new TableDetails.ColumnDetails(
                            columnName,
                            field,
                            columnMapping,
//                            reference,
                            primaryKey,
                            unique,
                            required | primaryKey,
                            autoIncrement,
                            notifyChanges
                    ));

        }

//        if (tableDetails.getColumns().isEmpty()) {
//            throw new QuantumFluxException("No columns are defined for table " + tableDetails.getTableName());
//        }
//        if (tableDetails.findPrimaryKeyColumn() == null && !TableView.class.isAssignableFrom(dataModelObject)) {
//            throw new QuantumFluxException("No primary key column defined for table " + tableDetails.getTableName());
//        }

        for (Indices indices : inspectObjectAnnotations(Indices.class, dataModelObject)) {
            for (Index index : indices.indices()) {
                tableDetails.addIndex(index);
            }
        }

        if (dataModelObject.isAnnotationPresent(ChangeListeners.class)) {
            for (Class<?> changeListener : dataModelObject.getAnnotation(ChangeListeners.class).changeListeners()) {
                tableDetails.addChangeListener(changeListener);
            }
        }

        if (table != null && table.constraints().length > 0) {
            for (TableConstraint tableConstraint : table.constraints()) {
                tableDetails.addConstraint(tableConstraint);
            }
        }

        return tableDetails;
    }

    private static boolean isValidField(Field field) {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        boolean isTransient = Modifier.isTransient(field.getModifiers());

        return !isStatic && !isTransient;
    }

//    public static Map<Field, Column> getColumns(Class<?> dataModelObject) {
//        Map<Field, Column> columns = new LinkedHashMap<Field, Column>();
//
//        for (Field field : getAllObjectFields(dataModelObject)) {
//            if (field.isAnnotationPresent(Column.class) && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
//                columns.put(field, field.getAnnotation(Column.class));
//            }
//        }
//
//        return columns;
//    }

    public static List<Field> getAllObjectFields(Class<?> object) {
        if (object.isInterface() && object.isEnum()) return new LinkedList<Field>();

        List<Field> objectFields = new LinkedList<Field>();
        Collections.addAll(objectFields, object.getDeclaredFields());

        if (object.getSuperclass() != null) {
            objectFields.addAll(getAllObjectFields(object.getSuperclass()));
        }

        return objectFields;
    }

    public static <T extends Annotation> Collection<T> inspectObjectAnnotations(Class<T> annotation, Class<?> object) {
        List<T> annotations = new LinkedList<T>();

        if (object.isAnnotationPresent(annotation)) {
            annotations.add(object.getAnnotation(annotation));
        }

        Class<?> superclass = object.getSuperclass();
        if (superclass != null) {
            annotations.addAll(inspectObjectAnnotations(annotation, superclass));
        }

        return annotations;
    }

    public static List<Class<?>> getDomainClasses(Context context, TableType type) {
        List<Class<?>> domainClasses = new ArrayList<Class<?>>();
        try {
            for (String className : getAllClasses(context)) {
                Class domainClass =
                        type == TableType.TABLE_VIEW
                                ?
                                getTableViewClass(className, context)
                                :
                                getTableClass(className, context);
                if (domainClass != null) {
                    domainClasses.add(domainClass);
                }
            }
        } catch (IOException e) {
            Log.e("QuantumFlux", e.getMessage());
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("QuantumFlux", e.getMessage());
        }

        return domainClasses;
    }

    private static Class getTableClass(String className, Context context) {
        Class<?> discoveredClass = null;
        try {
            discoveredClass = Class.forName(className, true, context.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            Log.e("QuantumFlux", e.getMessage());
        }

        if (isValidQuantumFluxTable(discoveredClass)) {
            Log.i("QuantumFlux", "domain class : " + discoveredClass.getSimpleName());
            return discoveredClass;
        } else {
            return null;
        }
    }

    private static Class getTableViewClass(String className, Context context) {
        Class<?> discoveredClass = null;
        try {
            discoveredClass = Class.forName(className, true, context.getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            Log.e("QuantumFlux", e.getMessage());
        }

        if (isValidQuantumFluxTableView(discoveredClass)) {
            Log.i("QuantumFlux", "domain class : " + discoveredClass.getSimpleName());
            return discoveredClass;
        } else {
            return null;
        }
    }

    private static boolean isValidQuantumFluxTable(Class<?> discoveredClass) {
        if (discoveredClass == null) return false;

        boolean isSubClassOfQuantumFluxRecord = QuantumFluxRecord.class.isAssignableFrom(discoveredClass);
        boolean isItselfQuantumFluxRecord = QuantumFluxRecord.class.equals(discoveredClass);
        boolean isAnnotatedQuantumFluxClass = discoveredClass.isAnnotationPresent(Table.class);

        boolean isTable = (isSubClassOfQuantumFluxRecord && !isItselfQuantumFluxRecord) || isAnnotatedQuantumFluxClass;

        boolean isAbstractClass = Modifier.isAbstract(discoveredClass.getModifiers());

        boolean isValid = isTable && !isAbstractClass;

        return isValid;
    }

    private static boolean isValidQuantumFluxTableView(Class<?> discoveredClass) {
        if (discoveredClass == null) return false;

        boolean isSubClassOfTableView = TableView.class.isAssignableFrom(discoveredClass);
        boolean isItselfTableView = TableView.class.equals(discoveredClass);

        boolean isTableView = isSubClassOfTableView && !isItselfTableView;

        boolean isAbstractClass = Modifier.isAbstract(discoveredClass.getModifiers());

        boolean isValid = isTableView && !isAbstractClass;

        return isValid;
    }

    private static List<String> getAllClasses(Context context) throws PackageManager.NameNotFoundException, IOException {
        String packageName = ManifestHelper.getPackageName(context);
        String path = getSourcePath(context);
        List<String> classNames = new ArrayList<String>();
        DexFile dexfile = null;
        try {
            dexfile = new DexFile(path);
            Enumeration<String> dexEntries = dexfile.entries();
            while (dexEntries.hasMoreElements()) {
                String className = dexEntries.nextElement();
                if (className.startsWith(packageName)) classNames.add(className);
            }
        } catch (NullPointerException e) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> urls = classLoader.getResources("");
            while (urls.hasMoreElements()) {
                List<String> fileNames = new ArrayList<String>();
                String classDirectoryName = urls.nextElement().getFile();
                if (classDirectoryName.contains("bin") || classDirectoryName.contains("classes")) {
                    File classDirectory = new File(classDirectoryName);
                    for (File filePath : classDirectory.listFiles()) {
                        populateFiles(filePath, fileNames, "");
                    }
                    for (String fileName : fileNames) {
                        if (fileName.startsWith(packageName)) classNames.add(fileName);
                    }
                }
            }
        } finally {
            if (null != dexfile) dexfile.close();
        }
        return classNames;
    }

    private static void populateFiles(File path, List<String> fileNames, String parent) {
        if (path.isDirectory()) {
            for (File newPath : path.listFiles()) {
                if ("".equals(parent)) {
                    populateFiles(newPath, fileNames, path.getName());
                } else {
                    populateFiles(newPath, fileNames, parent + "." + path.getName());
                }
            }
        } else {
            String pathName = path.getName();
            String classSuffix = ".class";
            pathName = pathName.endsWith(classSuffix) ?
                    pathName.substring(0, pathName.length() - classSuffix.length()) : pathName;
            if ("".equals(parent)) {
                fileNames.add(pathName);
            } else {
                fileNames.add(parent + "." + pathName);
            }
        }
    }

    private static String getSourcePath(Context context) throws PackageManager.NameNotFoundException {
        return context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
    }

    public static enum TableType {
        TABLE, TABLE_VIEW
    }
}
