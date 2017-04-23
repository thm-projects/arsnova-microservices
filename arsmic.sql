--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.6
-- Dumped by pg_dump version 9.5.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: answer_options; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE answer_options (
    id uuid NOT NULL,
    question_id uuid NOT NULL,
    correct boolean NOT NULL,
    content text NOT NULL,
    points integer NOT NULL
);


ALTER TABLE answer_options OWNER TO arsnova3;

--
-- Name: choice_answers; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE choice_answers (
    id uuid NOT NULL,
    question_id uuid NOT NULL,
    session_id uuid NOT NULL,
    answer_option_id uuid NOT NULL
);


ALTER TABLE choice_answers OWNER TO arsnova3;

--
-- Name: comments; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE comments (
    id uuid NOT NULL,
    user_id uuid NOT NULL,
    session_id uuid NOT NULL,
    is_read boolean DEFAULT false NOT NULL,
    subject character varying(255) NOT NULL,
    content text NOT NULL,
    created_at character varying(30) NOT NULL
);


ALTER TABLE comments OWNER TO arsnova3;

--
-- Name: freetext_answers; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE freetext_answers (
    id uuid NOT NULL,
    question_id uuid NOT NULL,
    session_id uuid NOT NULL,
    subject text NOT NULL,
    content text NOT NULL
);


ALTER TABLE freetext_answers OWNER TO arsnova3;

--
-- Name: questions; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE questions (
    id uuid NOT NULL,
    session_id uuid NOT NULL,
    subject character varying(255) NOT NULL,
    content text NOT NULL,
    variant character varying(255) NOT NULL,
    format character varying(255) NOT NULL,
    hint text,
    solution text,
    active boolean DEFAULT false NOT NULL,
    voting_disabled boolean DEFAULT true NOT NULL,
    show_statistic boolean DEFAULT false NOT NULL,
    show_answer boolean DEFAULT false NOT NULL,
    abstention_allowed boolean DEFAULT true NOT NULL,
    format_attributes text
);


ALTER TABLE questions OWNER TO arsnova3;

--
-- Name: sessions; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE sessions (
    id uuid NOT NULL,
    keyword character varying(8) NOT NULL,
    user_id uuid NOT NULL,
    title character varying(255) NOT NULL,
    short_name character varying(255) NOT NULL,
    last_owner_activity character varying(30) NOT NULL,
    creation_time character varying(30) NOT NULL,
    active boolean DEFAULT false NOT NULL,
    feedback_lock boolean DEFAULT true NOT NULL,
    flip_flashcards boolean DEFAULT true NOT NULL
);


ALTER TABLE sessions OWNER TO arsnova3;

--
-- Name: tokens; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE tokens (
    token character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    created character varying(30) NOT NULL,
    modified character varying(30),
    last_used character varying(30) NOT NULL
);


ALTER TABLE tokens OWNER TO arsnova3;

--
-- Name: users; Type: TABLE; Schema: public; Owner: arsnova3
--

CREATE TABLE users (
    id uuid NOT NULL,
    username character varying(255) NOT NULL,
    pwd character varying(255) NOT NULL
);


ALTER TABLE users OWNER TO arsnova3;

--
-- Data for Name: answer_options; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY answer_options (id, question_id, correct, content, points) FROM stdin;
f4ba953e-1a99-43aa-95a4-f0f3bfbe26d4	fa705322-16fa-4987-99a6-2abe767ce832	t	this is a correct answer option	10
8831b53f-881d-45c1-a54f-a6a63c6b32ff	fa705322-16fa-4987-99a6-2abe767ce832	f	this is an incorrect answer option	-10
c26f258d-9ec8-495f-b28e-cf061616e25c	fa705322-16fa-4987-99a6-2abe767ce832	t	this is another correct answer option	10
e7b00678-6177-47bf-85fe-5e030951a24a	d53b8af4-2551-4b3b-b125-3f7e851aa6d2	t	this is a correct answer option	10
2b6e4473-757b-4eef-8263-9dbb07dd27a1	d53b8af4-2551-4b3b-b125-3f7e851aa6d2	f	this is an incorrect answer option	-10
b80f73db-0710-4788-8930-708b566d67d3	d53b8af4-2551-4b3b-b125-3f7e851aa6d2	t	this is another correct answer option	10
34593b32-f3eb-4bcd-99f6-7ce9c30df1de	6a0d84d9-662f-4373-b897-e55f9707d40a	t	this is a correct answer option	10
08b1c7fc-f411-4ce2-9ae1-111b6569d7c5	6a0d84d9-662f-4373-b897-e55f9707d40a	t	this is another correct answer option	10
f01e4e6c-8d57-4a5a-af1a-fa3d9bd767e3	6a0d84d9-662f-4373-b897-e55f9707d40a	f	this is an incorrect answer option	-10
\.


--
-- Data for Name: choice_answers; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY choice_answers (id, question_id, session_id, answer_option_id) FROM stdin;
\.


--
-- Data for Name: comments; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY comments (id, user_id, session_id, is_read, subject, content, created_at) FROM stdin;
\.


--
-- Data for Name: freetext_answers; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY freetext_answers (id, question_id, session_id, subject, content) FROM stdin;
\.


--
-- Data for Name: questions; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY questions (id, session_id, subject, content, variant, format, hint, solution, active, voting_disabled, show_statistic, show_answer, abstention_allowed, format_attributes) FROM stdin;
fa705322-16fa-4987-99a6-2abe767ce832	42664be0-35d1-45c7-a87d-d2ed9cc9cad7	new	this is an mc question.	preparation	mc	a hint	a solution	t	f	t	f	f	null
d53b8af4-2551-4b3b-b125-3f7e851aa6d2	42664be0-35d1-45c7-a87d-d2ed9cc9cad7	new	this is an mc question.	preparation	mc	a hint	a solution	t	f	t	f	f	null
6a0d84d9-662f-4373-b897-e55f9707d40a	42664be0-35d1-45c7-a87d-d2ed9cc9cad7	new	this is an mc question.	preparation	mc	a hint	a solution	t	f	t	f	f	null
8aef9798-5465-4ecb-b5da-b37cf04a5bc6	42664be0-35d1-45c7-a87d-d2ed9cc9cad7	1. Flashcard	this is a flashcard.	preparation	flashcard	a hint	a solution	t	f	t	f	f	{"backside":"a flashcard backside"}
4a6e0dd7-1b7e-4717-9315-133fea97e7bc	42664be0-35d1-45c7-a87d-d2ed9cc9cad7	1. Flashcard	this is a flashcard.	preparation	flashcard	a hint	a solution	t	f	t	f	f	{"backside":"a flashcard backside"}
da12be65-7708-4a28-a040-3ef0a7bd20aa	42664be0-35d1-45c7-a87d-d2ed9cc9cad7	1. Flashcard	this is a flashcard.	preparation	flashcard	a hint	a solution	t	f	t	f	f	{"backside":"a flashcard backside"}
3d7336d9-2ef7-453f-b27d-ccc657210b32	42664be0-35d1-45c7-a87d-d2ed9cc9cad7	1. Flashcard	this is a flashcard.	preparation	flashcard	a hint	a solution	t	f	t	f	f	{"backside":"a flashcard backside"}
\.


--
-- Data for Name: sessions; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY sessions (id, keyword, user_id, title, short_name, last_owner_activity, creation_time, active, feedback_lock, flip_flashcards) FROM stdin;
42664be0-35d1-45c7-a87d-d2ed9cc9cad7	11111111	b055f5d8-1f8c-11e7-93ae-92361f002671	neuer Title	neuer shortname	yowuddup	yowuddup2	t	f	f
f30cba91-e974-4c9d-a39d-9e699d71f424	22222222	b055f5d8-1f8c-11e7-93ae-92361f002671	neuer Title	neuer shortname	yowuddup	yowuddup2	t	f	f
\.


--
-- Data for Name: tokens; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY tokens (token, user_id, created, modified, last_used) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: arsnova3
--

COPY users (id, username, pwd) FROM stdin;
\.


--
-- Name: answer_options_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY answer_options
    ADD CONSTRAINT answer_options_pkey PRIMARY KEY (id);


--
-- Name: choice_answers_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY choice_answers
    ADD CONSTRAINT choice_answers_pkey PRIMARY KEY (id);


--
-- Name: comments_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);


--
-- Name: freetext_answers_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY freetext_answers
    ADD CONSTRAINT freetext_answers_pkey PRIMARY KEY (id);


--
-- Name: questions_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY questions
    ADD CONSTRAINT questions_pkey PRIMARY KEY (id);


--
-- Name: sessions_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY sessions
    ADD CONSTRAINT sessions_pkey PRIMARY KEY (id);


--
-- Name: tokens_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY tokens
    ADD CONSTRAINT tokens_pkey PRIMARY KEY (token);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users_username_key; Type: CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: choice_answer_answer_option_fk; Type: FK CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY choice_answers
    ADD CONSTRAINT choice_answer_answer_option_fk FOREIGN KEY (answer_option_id) REFERENCES answer_options(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: choice_answer_question_fk; Type: FK CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY choice_answers
    ADD CONSTRAINT choice_answer_question_fk FOREIGN KEY (question_id) REFERENCES questions(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: freetext_answer_question_fk; Type: FK CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY freetext_answers
    ADD CONSTRAINT freetext_answer_question_fk FOREIGN KEY (question_id) REFERENCES questions(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: possible_answer_question_fk; Type: FK CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY answer_options
    ADD CONSTRAINT possible_answer_question_fk FOREIGN KEY (question_id) REFERENCES questions(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: token_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: arsnova3
--

ALTER TABLE ONLY tokens
    ADD CONSTRAINT token_user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

