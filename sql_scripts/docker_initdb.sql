DROP SEQUENCE forum_id_seq;
DROP SEQUENCE post_id_seq;
DROP SEQUENCE thread_id_seq;
DROP SEQUENCE users_id_seq;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS forum;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS thread;
DROP TABLE IF EXISTS vote;

CREATE EXTENSION IF NOT EXISTS citext;

CREATE SEQUENCE forum_id_seq;
CREATE SEQUENCE post_id_seq;
CREATE SEQUENCE thread_id_seq;
CREATE SEQUENCE users_id_seq;

CREATE TABLE users (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('users_id_seq'),
  nickname CITEXT COLLATE ucs_basic NOT NULL UNIQUE,
  fullname VARCHAR(40) NOT NULL,
  about TEXT,
  email CITEXT NOT NULL UNIQUE
);

CREATE TABLE forum (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('forum_id_seq'),
  title TEXT NOT NULL,
  "user" TEXT NOT NULL,
  slug TEXT,
  posts INTEGER,
  threads INTEGER
);

CREATE TABLE post (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('post_id_seq'),
  parent INTEGER DEFAULT 0,
  author VARCHAR(255),
  message TEXT,
  isedited BOOLEAN,
  forum VARCHAR(255),
  created TIMESTAMP WITH TIME ZONE DEFAULT now(),
  thread INTEGER,
  path VARCHAR(255)
);

CREATE TABLE thread (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('thread_id_seq'),
  title TEXT NOT NULL,
  forum TEXT NOT NULL,
  message TEXT,
  votes INTEGER,
  slug TEXT,
  created TIMESTAMP WITH TIME ZONE,
  author TEXT
);

CREATE TABLE vote (
  id INTEGER,
  slug VARCHAR(255),
  nickname VARCHAR(255),
  voice INTEGER
);