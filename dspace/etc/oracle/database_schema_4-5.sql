--
-- database_schema_4-5.sql
--
-- The contents of this file are subject to the license and copyright
-- detailed in the LICENSE and NOTICE files at the root of the source
-- tree and available online at
--
-- http://www.dspace.org/license/
--

--
-- SQL commands to upgrade the database schema of a live DSpace 4.x
-- to the DSpace 5 database schema.
--
-- DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST.
-- DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST.
-- DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST. DUMP YOUR DATABASE FIRST.
--

---------------------------------------------------
-- Add support for immutable metadata namespaces --
---------------------------------------------------

ALTER TABLE MetadataSchemaRegistry
    ADD (immutable INTEGER NOT NULL DEFAULT 0 CHECK(immutable IN (0, 1)));
UPDATE MetadataSchemaRegistry SET immutable = 1 WHERE short_id = 'dc';
