CREATE TABLE contentlist (
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
  content_id uuid NOT NULL,
  correct boolean NOT NULL,
  content TEXT NOT NULL,
  points INT NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT possible_answer_question_fk FOREIGN KEY (content_id) REFERENCES contentlist(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE freetext_answers (
  id uuid NOT NULL,
  content_id uuid NOT NULL,
  session_id uuid NOT NULL,
  subject TEXT NOT NULL,
  content TEXT NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT freetext_answer_content_fk FOREIGN KEY (content_id) REFERENCES contentlist(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE choice_answers (
  id uuid NOT NULL,
  content_id uuid NOT NULL,
  session_id uuid NOT NULL,
  answer_option_id uuid NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT choice_answer_content_fk FOREIGN KEY (content_id) REFERENCES contentlist(id) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT choice_answer_answer_option_fk FOREIGN KEY (answer_option_id) REFERENCES answer_options(id) ON UPDATE CASCADE ON DELETE CASCADE
);
