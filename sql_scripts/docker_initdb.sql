SET SYNCHRONOUS_COMMIT = OFF;

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

CREATE INDEX index_user__nickname ON users (LOWER(nickname));
CREATE INDEX index_user__email ON users (LOWER(email));

CREATE TABLE forum (
  id INTEGER PRIMARY KEY DEFAULT NEXTVAL('forum_id_seq'),
  title TEXT NOT NULL,
  "user" TEXT NOT NULL,
  slug TEXT UNIQUE,
  posts INTEGER,
  threads INTEGER
);

CREATE INDEX index_forum__slug ON forum (LOWER(slug));

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

CREATE INDEX index_thread__slug ON thread (LOWER(slug));
CREATE INDEX index_thread__forum ON thread (LOWER(forum));
CREATE INDEX index_thread__forum_created ON thread (LOWER(forum), created);

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

CREATE INDEX index_post__parent_thread ON post (parent ASC, thread ASC);
CREATE INDEX index_post__thread ON post (thread ASC);
CREATE INDEX index_post__thread_path ON post (thread, path ASC);

CREATE TABLE vote (
  id INTEGER,
  slug VARCHAR(255),
  nickname VARCHAR(255),
  voice INTEGER
);

CREATE INDEX index_vote__id_nickname ON vote (id, LOWER(nickname));

CREATE TABLE link_user_forum (
  id SERIAL PRIMARY KEY ,
  user_nickname CITEXT COLLATE "ucs_basic",
  forum_slug CITEXT COLLATE "ucs_basic",
  UNIQUE (user_nickname, forum_slug)
);
CREATE INDEX index_link_user_forum ON link_user_forum (user_nickname, forum_slug);

-- CREATE OR REPLACE FUNCTION postInsert() RETURNS TRIGGER AS
-- $BODY$
-- /*BEGIN
--   IF substring(new.path,1,1)='*' THEN
--     UPDATE post SET path=concat(substring(new.path, 2, char_length(new.path) - 1), '.', lpad(to_hex(new.id), 6, '0')) WHERE id=new.id;
--   END IF;
--   RETURN new;
-- END;*/
-- $BODY$
-- LANGUAGE plpgsql;
--
-- CREATE TRIGGER postInsert AFTER INSERT ON post FOR EACH ROW EXECUTE PROCEDURE postInsert();