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
    ADD (immutable BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE MetadataSchemaRegistry SET immutable = TRUE WHERE short_id = 'dc';
