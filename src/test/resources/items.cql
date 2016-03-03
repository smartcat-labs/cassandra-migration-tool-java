/*
  Create keyspace with Network Topology. This is more simular with keyspaces created on production servers.
  Also using lightweight transaction (ifNotExists) does not seem to work together with SimpleStrategy and
  consistency level EACH_QUORUM.
 */
CREATE KEYSPACE migration_test_items WITH replication = { 'class': 'NetworkTopologyStrategy', 'DC1': '1' };
USE migration_test_items;

/* Items by id */
CREATE TABLE items_by_id (
	id uuid,
	number text,
	external_id uuid,
	PRIMARY KEY (id)
) WITH COMMENT='Items by item ID';
