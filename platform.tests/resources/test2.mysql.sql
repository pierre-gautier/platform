--liquibase formatted sql

--changeset test:1510845926548-1
CREATE TABLE testliquibase.tablesandtables (COLUMN1 TEXT NULL);

--changeset test:1510845926548-2
CREATE TABLE testliquibase.tablesandtables2 (COLUMN2 TEXT NULL);

