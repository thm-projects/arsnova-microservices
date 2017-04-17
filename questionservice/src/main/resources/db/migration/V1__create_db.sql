CREATE TABLE questions (
  id uuid NOT NULL,
  session_id uuid NOT NULL,
  subject VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  variant VARCHAR(255) NOT NULL,
  format VARCHAR(255) NOT NULL,
  hint TEXT,
  solution TEXT,
  active boolean NOT NULL DEFAULT false,
  voting_disabled boolean NOT NULL DEFAULT true,
  show_statistic boolean NOT NULL DEFAULT false,
  show_answer boolean NOT NULL DEFAULT false,
  abstention_allowed boolean NOT NULL DEFAULT true,
  format_attributes TEXT,
  PRIMARY KEY(id)
);

CREATE TABLE answer_options (
  id uuid NOT NULL,
  question_id uuid NOT NULL,
  correct boolean NOT NULL,
  content TEXT NOT NULL,
  points INT NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT possible_answer_question_fk FOREIGN KEY (question_id) REFERENCES questions(id) ON UPDATE CASCADE ON DELETE CASCADE
);