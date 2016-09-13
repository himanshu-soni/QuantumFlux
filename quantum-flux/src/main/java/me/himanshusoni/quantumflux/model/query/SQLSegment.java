package me.himanshusoni.quantumflux.model.query;

import android.text.TextUtils;

import me.himanshusoni.quantumflux.model.map.SqlColumnMappingFactory;
import me.himanshusoni.quantumflux.model.util.QuantumFluxException;

public class SQLSegment implements DataFilterClause {

    private final String mSqlSegment;
    private final Object[] mArgs;

    public SQLSegment(String sqlSegment, Object... args) {
        this.mSqlSegment = sqlSegment;
        this.mArgs = args;
    }

    @Override
    public QueryBuilder buildWhereClause(SqlColumnMappingFactory columnMappingFactory) {
        for (int i = 0; i < mArgs.length; i++) {
            Object argObject = mArgs[i];
            mArgs[i] = columnMappingFactory.findColumnMapping(argObject.getClass()).toSqlType(argObject);
        }
        return new QueryBuilder(mSqlSegment, mArgs);
    }


    @Override
    public String getWhereClause() {
        return mSqlSegment;
    }

    @Override
    public SQLSegment addClause(DataFilterClause clause, DataFilterConjunction conjunction) {
        throw new QuantumFluxException("Clauses cannot be added to a data filter criterion");
    }

    @Override
    public boolean hasFilterValue() {
        return !TextUtils.isEmpty(mSqlSegment);
    }
}
