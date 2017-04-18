CREATE TABLE comments (
  id uuid NOT NULL,
  user_id uuid NOT NULL,
  session_id uuid NOT NULL,
  is_read boolean NOT NULL DEFAULT false,
  subject VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  created_at VARCHAR(30) NOT NULL,
  PRIMARY KEY(id)
);