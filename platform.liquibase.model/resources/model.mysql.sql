--liquibase formatted sql

-- ---------------------------------------- node

--changeset pierre:1496931668037-1
CREATE TABLE node (id VARCHAR(45) NOT NULL, type VARCHAR(45) NOT NULL);

--changeset pierre:1496931668037-5
ALTER TABLE node ADD PRIMARY KEY (id);

--changeset pierre:1496931668037-9
ALTER TABLE node ADD CONSTRAINT node_id_UNIQUE UNIQUE (id);

-- ---------------------------------------- relation

--changeset pierre:1496931668037-3
CREATE TABLE relation (id VARCHAR(45) NOT NULL, type VARCHAR(45) NOT NULL, source_id VARCHAR(45) NOT NULL, target_id VARCHAR(45) NOT NULL);

--changeset pierre:1496931668037-7
ALTER TABLE relation ADD PRIMARY KEY (id);

--changeset pierre:1496931668037-10
ALTER TABLE relation ADD CONSTRAINT relation_id_UNIQUE UNIQUE (id);

--changeset pierre:1496931668037-15
CREATE INDEX source_id_idx ON relation(source_id);

--changeset pierre:1496931668037-16
CREATE INDEX target_id_idx ON relation(target_id);

--changeset pierre:1496931668037-19
ALTER TABLE relation ADD CONSTRAINT source_id FOREIGN KEY (source_id) REFERENCES node (id) ON UPDATE CASCADE ON DELETE CASCADE;

--changeset pierre:1496931668037-20
ALTER TABLE relation ADD CONSTRAINT target_id FOREIGN KEY (target_id) REFERENCES node (id) ON UPDATE CASCADE ON DELETE CASCADE;

-- ---------------------------------------- node attribute

--changeset pierre:1496931668037-2
CREATE TABLE node_attribute (node_id VARCHAR(45) NOT NULL, name VARCHAR(45) NOT NULL, value VARCHAR(255) NOT NULL);

--changeset pierre:1496931668037-6
ALTER TABLE node_attribute ADD PRIMARY KEY (node_id, name);

--changeset pierre:1496931668037-11
ALTER TABLE node_attribute ADD CONSTRAINT node_id_name_UNIQUE UNIQUE (node_id, name);

--changeset pierre:1496931668037-13
CREATE INDEX node_id_idx ON node_attribute(node_id);

--changeset pierre:1496931668037-17
ALTER TABLE node_attribute ADD CONSTRAINT node_id FOREIGN KEY (node_id) REFERENCES node (id) ON UPDATE CASCADE ON DELETE CASCADE;

-- ---------------------------------------- relation attribute

--changeset pierre:1496931668037-4
CREATE TABLE relation_attribute (relation_id VARCHAR(45) NOT NULL, name VARCHAR(45) NOT NULL, value VARCHAR(255) NOT NULL);

--changeset pierre:1496931668037-8
ALTER TABLE relation_attribute ADD PRIMARY KEY (relation_id, name);

--changeset pierre:1496931668037-12
ALTER TABLE relation_attribute ADD CONSTRAINT relation_id_name_UNIQUE UNIQUE (relation_id, name);

--changeset pierre:1496931668037-14
CREATE INDEX relation_id_idx ON relation_attribute(relation_id);

--changeset pierre:1496931668037-18
ALTER TABLE relation_attribute ADD CONSTRAINT relation_id FOREIGN KEY (relation_id) REFERENCES relation (id) ON UPDATE CASCADE ON DELETE CASCADE;
