package info.quantumflux.model.map;


import info.quantumflux.model.map.types.BigDecimalType;
import info.quantumflux.model.map.types.DateType;
import info.quantumflux.model.map.types.FloatType;
import info.quantumflux.model.map.types.IntegerType;
import info.quantumflux.model.map.types.LongType;
import info.quantumflux.model.map.types.UUIDType;
import info.quantumflux.model.util.QuantumFluxException;

import java.util.ArrayList;
import java.util.List;

import info.quantumflux.model.map.types.BooleanType;
import info.quantumflux.model.map.types.CalendarType;
import info.quantumflux.model.map.types.DoubleType;
import info.quantumflux.model.map.types.ShortType;
import info.quantumflux.model.map.types.StringType;

/**
 * The factory that will contain all of the available column conversion for the system.
 * This class can be extended, and the class name provided as part of the meta information to load the
 * extended class instead.
 */
public class SqlColumnMappingFactory {

    private final List<SqlColumnMapping> mColumnMappings;

    public SqlColumnMappingFactory() {
        mColumnMappings = new ArrayList<SqlColumnMapping>();
        mColumnMappings.add(new BigDecimalType());
        mColumnMappings.add(new BooleanType());
        mColumnMappings.add(new CalendarType());
        mColumnMappings.add(new DateType());
        mColumnMappings.add(new DoubleType());
        mColumnMappings.add(new FloatType());
        mColumnMappings.add(new IntegerType());
        mColumnMappings.add(new LongType());
        mColumnMappings.add(new ShortType());
        mColumnMappings.add(new StringType());
        mColumnMappings.add(new UUIDType());
    }

    public void addColumnMapping(SqlColumnMapping mapping) {
        mColumnMappings.add(mapping);
    }

    public SqlColumnMapping findColumnMapping(Class<?> fieldType) {
        Class<?> fieldTypeWrapped = wrapPrimitives(fieldType);

        for (SqlColumnMapping columnMapping : mColumnMappings) {
            Class<?> columnType = columnMapping.getJavaType();
            if (columnType.equals(fieldTypeWrapped) || columnType.isAssignableFrom(fieldType))
                return columnMapping;
        }

        throw new QuantumFluxException("No valid SQL mapping found for type " + fieldType);
    }

    private Class<?> wrapPrimitives(Class fieldType) {

        if (!fieldType.isPrimitive()) return fieldType;

        if (long.class.equals(fieldType)) return Long.class;
        if (int.class.equals(fieldType)) return Integer.class;
        if (double.class.equals(fieldType)) return Double.class;
        if (float.class.equals(fieldType)) return Float.class;
        if (short.class.equals(fieldType)) return Short.class;
        if (boolean.class.equals(fieldType)) return Boolean.class;
        if (byte.class.equals(fieldType)) return Byte.class;
        if (void.class.equals(fieldType)) return Void.class;
        if (char.class.equals(fieldType)) return Character.class;

        throw new QuantumFluxException("No primitive type registered for type " + fieldType);
    }
}
