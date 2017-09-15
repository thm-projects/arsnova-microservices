CREATE TABLE tokens (
  token VARCHAR(255) NOT NULL,
  user_id uuid NOT NULL,
  created VARCHAR(30) NOT NULL,
  modified VARCHAR(30),
  last_used VARCHAR(30) NOT NULL,
  PRIMARY KEY(token)
);
