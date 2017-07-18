CREATE TABLE sessionlistentries (
    id uuid NOT NULL,
    keyword VARCHAR(8) NOT NULL UNIQUE,
    PRIMARY KEY(id)
);
