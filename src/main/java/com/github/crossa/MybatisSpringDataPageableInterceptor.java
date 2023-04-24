package com.github.crossa;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class MybatisSpringDataPageableInterceptor implements Interceptor {

    private static final String PAGEABLE = "pageable";
    private static final String MAPPED_STATEMENT = "delegate.mappedStatement";
    private static final String BOUND_SQL = "delegate.boundSql.sql";
    private static final String PARAMETER_HANDLER = "delegate.parameterHandler";
    private static final String SELECT = "SELECT";
    private static final String LIMIT_U = "LIMIT ";
    private static final String LIMIT_L = "limit ";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(handler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPED_STATEMENT);
        String sqlStr = handler.getBoundSql().getSql().replaceAll("\n", "").replaceAll("  ", " ");
        StringBuffer sql = new StringBuffer(handler.getBoundSql().getSql());
        if (SELECT.equals(mappedStatement.getSqlCommandType().name()) && !sqlStr.contains(LIMIT_U)
                && !sqlStr.contains(LIMIT_L)) {
            ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue(PARAMETER_HANDLER);
            Object parameter = parameterHandler.getParameterObject();
            if (parameter instanceof Map) {
                Map<String, Object> params = (Map<String, Object>) parameter;
                searchPageable(params).ifPresent(pageable -> {
                    sql.append(" limit " + (pageable.getOffset()) + "," + pageable.getPageSize());
                });
            }
            metaObject.setValue(BOUND_SQL, sql.toString());
        }
        return invocation.proceed();
    }

    public Optional<Pageable> searchPageable(Map<String, Object> params) {
        if (params.containsKey(PAGEABLE) && params.get(PAGEABLE) instanceof Pageable) {
            return Optional.ofNullable((Pageable) params.get(PAGEABLE));
        }
        Iterator<String> key = params.keySet().iterator();
        while (key.hasNext()) {
            Object obj = params.get(key.next());
            if (obj instanceof Pageable) {
                return Optional.ofNullable((Pageable) obj);
            }
        }
        return Optional.empty();
    }
}
