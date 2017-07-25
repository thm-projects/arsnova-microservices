CREATE TABLE users (
  id uuid NULL,
  username VARCHAR(255) NOT NULL UNIQUE,
  pwd VARCHAR(255) NOT NULL,
  PRIMARY KEY(id)
);

CREATE TABLE tokens (
  token VARCHAR(255) NOT NULL,
  user_id uuid NOT NULL,
  created VARCHAR(30) NOT NULL,
  modified VARCHAR(30),
  last_used VARCHAR(30) NOT NULL,
  PRIMARY KEY(token),
  CONSTRAINT token_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE session_roles (
  user_id uuid NOT NULL,
  session_id uuid NOT NULL,
  role varchar(255) NOT NULL,
  CONSTRAINT session_role_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE
);
