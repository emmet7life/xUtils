/*
 * Copyright (c) 2013. wyouflf (wyouflf@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lidroid.xutils.db.sqlite;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.table.*;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlBuilder {

    private SqlBuilder() {
    }

    //*********************************************** insert sql ***********************************************

    public static SqlInfo buildInsertSqlInfo(DbUtils db, Object entity) throws DbException {

        List<KeyValue> keyValueList = entity2KeyValueList(db, entity);

        StringBuffer sqlSb = null;
        SqlInfo sqlInfo = null;
        int size = keyValueList.size();
        if (keyValueList != null && size > 0) {

            sqlInfo = new SqlInfo();
            sqlSb = new StringBuffer();

            sqlSb.append("INSERT INTO ");
            sqlSb.append(Table.get(entity.getClass()).getTableName());
            sqlSb.append(" (");
            for (KeyValue kv : keyValueList) {
                sqlSb.append(kv.getKey()).append(",");
                sqlInfo.addValue(kv.getValue());
            }
            sqlSb.deleteCharAt(sqlSb.length() - 1);
            sqlSb.append(") VALUES ( ");

            int length = keyValueList.size();
            for (int i = 0; i < length; i++) {
                sqlSb.append("?,");
            }
            sqlSb.deleteCharAt(sqlSb.length() - 1);
            sqlSb.append(")");

            sqlInfo.setSql(sqlSb.toString());
        }

        return sqlInfo;
    }

    //*********************************************** delete sql ***********************************************

    private static String buildDeleteSqlByTableName(String tableName) {
        return "DELETE FROM " + tableName;
    }

    public static SqlInfo buildDeleteSqlInfo(Object entity) throws DbException {
        Table table = Table.get(entity.getClass());

        Id id = table.getId();
        Object idValue = id.getColumnValue(entity);

        if (idValue == null) {
            throw new DbException(entity.getClass() + " id value is null");
        }
        StringBuffer sqlSb = new StringBuffer(buildDeleteSqlByTableName(table.getTableName()));
        sqlSb.append(" WHERE ").append(id.getColumnName()).append("=?");

        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSql(sqlSb.toString());
        sqlInfo.addValue(idValue);

        return sqlInfo;
    }

    public static SqlInfo buildDeleteSqlInfo(Class<?> entityType, Object idValue) throws DbException {
        Table table = Table.get(entityType);
        Id id = table.getId();

        if (null == idValue) {
            throw new DbException("idValue is null");
        }

        StringBuffer sqlSb = new StringBuffer(buildDeleteSqlByTableName(table.getTableName()));
        sqlSb.append(" WHERE ").append(id.getColumnName()).append("=?");

        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSql(sqlSb.toString());
        sqlInfo.addValue(idValue);

        return sqlInfo;
    }

    public static SqlInfo buildDeleteSqlInfo(Class<?> entityType, WhereBuilder whereBuilder) throws DbException {
        Table table = Table.get(entityType);
        StringBuffer sqlSb = new StringBuffer(buildDeleteSqlByTableName(table.getTableName()));

        if (whereBuilder != null) {
            sqlSb.append(" WHERE ").append(whereBuilder.toString());
        }

        return new SqlInfo(sqlSb.toString());
    }

    //*********************************************** update sql ***********************************************

    public static SqlInfo buildUpdateSqlInfo(Object entity) throws DbException {

        Table table = Table.get(entity.getClass());
        Object idValue = table.getId().getColumnValue(entity);

        if (null == idValue) {//主键值不能为null，否则不能更新
            throw new DbException("this entity[" + entity.getClass() + "]'s id value is null");
        }

        List<KeyValue> keyValueList = new ArrayList<KeyValue>();
        //添加属性
        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            KeyValue kv = column2KeyValue(entity, column);
            if (kv != null)
                keyValueList.add(kv);
        }

        if (keyValueList == null || keyValueList.size() == 0) return null;

        SqlInfo sqlInfo = new SqlInfo();
        StringBuffer sqlSb = new StringBuffer("UPDATE ");
        sqlSb.append(table.getTableName());
        sqlSb.append(" SET ");
        for (KeyValue kv : keyValueList) {
            sqlSb.append(kv.getKey()).append("=?,");
            sqlInfo.addValue(kv.getValue());
        }
        sqlSb.deleteCharAt(sqlSb.length() - 1);
        sqlSb.append(" WHERE ").append(table.getId().getColumnName()).append("=?");
        sqlInfo.addValue(idValue);
        sqlInfo.setSql(sqlSb.toString());
        return sqlInfo;
    }

    public static SqlInfo buildUpdateSqlInfo(Object entity, WhereBuilder whereBuilder) throws DbException {

        Table table = Table.get(entity.getClass());

        List<KeyValue> keyValueList = new ArrayList<KeyValue>();

        //添加属性
        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            KeyValue kv = column2KeyValue(entity, column);
            if (kv != null) keyValueList.add(kv);
        }

        if (keyValueList == null || keyValueList.size() == 0) {
            throw new DbException("this entity[" + entity.getClass() + "] has no column");
        }

        SqlInfo sqlInfo = new SqlInfo();
        StringBuffer sqlSb = new StringBuffer("UPDATE ");
        sqlSb.append(table.getTableName());
        sqlSb.append(" SET ");
        for (KeyValue kv : keyValueList) {
            sqlSb.append(kv.getKey()).append("=?,");
            sqlInfo.addValue(kv.getValue());
        }
        sqlSb.deleteCharAt(sqlSb.length() - 1);
        if (whereBuilder != null) {
            sqlSb.append(" WHERE ").append(whereBuilder.toString());
        }
        sqlInfo.setSql(sqlSb.toString());
        return sqlInfo;
    }

    //*********************************************** select sql ***********************************************

    private static String buildSelectSqlByTableName(String tableName) {
        return new StringBuffer("SELECT * FROM ").append(tableName).toString();
    }

    public static SqlInfo buildSelectSqlInfo(Class<?> entityType, Object idValue) throws DbException {
        Table table = Table.get(entityType);

        StringBuffer sqlSb = new StringBuffer(buildSelectSqlByTableName(table.getTableName()));
        sqlSb.append(" WHERE ").append(table.getId().getColumnName()).append("=?");

        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setSql(sqlSb.toString());
        sqlInfo.addValue(idValue);

        return sqlInfo;
    }

    public static SqlInfo buildSelectSqlInfo(Class<?> clazz) throws DbException {
        return new SqlInfo(buildSelectSqlByTableName(Table.get(clazz).getTableName()));
    }

    public static SqlInfo buildSelectSqlInfo(Class<?> clazz, WhereBuilder whereBuilder) throws DbException {
        Table table = Table.get(clazz);

        StringBuffer sqlSb = new StringBuffer(buildSelectSqlByTableName(table.getTableName()));

        if (whereBuilder != null) {
            sqlSb.append(" WHERE ").append(whereBuilder.toString());
        }

        return new SqlInfo(sqlSb.toString());
    }


    //*********************************************** others ***********************************************

    public static SqlInfo buildCreateTableSqlInfo(Class<?> entityType) throws DbException {
        Table table = Table.get(entityType);

        Id id = table.getId();
        StringBuffer sqlSb = new StringBuffer();
        sqlSb.append("CREATE TABLE IF NOT EXISTS ");
        sqlSb.append(table.getTableName());
        sqlSb.append(" ( ");

        if (id.isAutoIncreaseType()) {
            sqlSb.append("\"").append(id.getColumnName()).append("\"  ").append("INTEGER PRIMARY KEY AUTOINCREMENT,");
        } else {
            sqlSb.append("\"").append(id.getColumnName()).append("\"  ").append("TEXT PRIMARY KEY,");
        }

        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            sqlSb.append("\"").append(column.getColumnName());
            sqlSb.append("\",");
        }

        sqlSb.deleteCharAt(sqlSb.length() - 1);
        sqlSb.append(" )");
        return new SqlInfo(sqlSb.toString());
    }

    private static KeyValue column2KeyValue(Object entity, Column column) {
        KeyValue kv = null;
        String key = column.getColumnName();
        Object value = column.getColumnValue(entity);
        value = value == null ? column.getDefaultValue() : value;
        if (key != null && value != null) {
            kv = new KeyValue(key, value);
        }
        return kv;
    }

    public static List<KeyValue> entity2KeyValueList(DbUtils db, Object entity) throws DbException {

        List<KeyValue> keyValueList = new ArrayList<KeyValue>();

        Table table = Table.get(entity.getClass());
        Id id = table.getId();

        if (id != null) {
            Object idValue = id.getColumnValue(entity);
            if (idValue != null && !id.isAutoIncreaseType()) { //用了非自增长,添加id , 采用自增长就不需要添加id了
                KeyValue kv = new KeyValue(table.getId().getColumnName(), idValue);
                keyValueList.add(kv);
            }
        }

        Collection<Column> columns = table.columnMap.values();
        for (Column column : columns) {
            if (column instanceof Foreign) {
                ((Foreign) column).db = db;
            }
            KeyValue kv = column2KeyValue(entity, column);
            if (kv != null) {
                keyValueList.add(kv);
            }
        }

        return keyValueList;
    }

}
