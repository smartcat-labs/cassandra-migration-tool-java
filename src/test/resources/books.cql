/*
  Create keyspace with Network Topology. This is more simular with keyspaces created on production servers.
  Also using lightweight transaction (ifNotExists) does not seem to work together with SimpleStrategy and
  consistency level EACH_QUORUM.
 */
CREATE KEYSPACE migration_test_books WITH replication = { 'class': 'NetworkTopologyStrategy', 'DC1': '1' };
USE migration_test_books;

/* Books */ 
CREATE TABLE books (
	name text,
	author text,
	PRIMARY KEY (author, name)
) WITH COMMENT='Books by author';
