DROP SEQUENCE forum_id_seq CASCADE;
DROP SEQUENCE post_id_seq CASCADE;
DROP SEQUENCE thread_id_seq CASCADE;
DROP SEQUENCE users_id_seq CASCADE;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS forum CASCADE ;
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
  slug TEXT UNIQUE,
  posts INTEGER,
  threads INTEGER
);

CREATE TABLE thread (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('thread_id_seq'),
  title TEXT NOT NULL,
  forum TEXT NOT NULL references forum(slug),
  message TEXT,
  votes INTEGER,
  slug TEXT,
  created TIMESTAMP WITH TIME ZONE,
  author TEXT
);

CREATE TABLE post (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('post_id_seq'),
  parent INTEGER DEFAULT 0,
  author VARCHAR(255),
  message TEXT,
  isedited BOOLEAN,
  forum VARCHAR(255),
  created TIMESTAMP WITH TIME ZONE DEFAULT now(),
  thread INTEGER references thread(id),
  path VARCHAR(255)
);

CREATE TABLE vote (
  id INTEGER,
  slug VARCHAR(255),
  nickname VARCHAR(255),
  voice INTEGER
);

CREATE INDEX index_post__parent_thread ON post (parent ASC, thread ASC);
CREATE INDEX index_post__thread ON post (thread ASC);

CREATE INDEX index_thread__slug ON thread (slug);
CREATE INDEX index_vote__id_nickname ON vote (id, LOWER(nickname));

CREATE INDEX index_user__nickname ON users (LOWER(nickname));
CREATE INDEX index_user__email ON users (LOWER(email));

CREATE INDEX index_forum__slug ON forum (slug);

CREATE TRIGGER post_update_path AFTER INSERT ON post FOR EACH ROW EXECUTE PROCEDURE postUpdatePath();

CREATE OR REPLACE FUNCTION postUpdatePath() RETURNS TRIGGER AS
$BODY$
BEGIN
  IF substring(new.path,1,1)='*' THEN
    UPDATE post SET path=concat(substring(new.path, 2, char_length(new.path) - 1), '.', lpad(to_hex(new.id), 6, '0')) WHERE id=new.id;
  END IF;
  RETURN new;
END;
$BODY$
LANGUAGE plpgsql;
