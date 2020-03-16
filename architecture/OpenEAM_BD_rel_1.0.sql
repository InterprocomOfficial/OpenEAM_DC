
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'WIN1251';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

CREATE TABLE public.asset (
    id bigint NOT NULL,
    assetnum text,
    creationdate timestamp without time zone,
    description text,
    modificationdate timestamp without time zone,
    ownership character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    classstructure_id bigint NOT NULL,
    parent_id bigint
);

ALTER TABLE public.asset OWNER TO OpenEAM;

CREATE TABLE public.attribute (
    id bigint NOT NULL,
    description text,
    hint text,
    name text,
    type character varying(255) NOT NULL,
);

ALTER TABLE public.attribute OWNER TO OpenEAM;

CREATE TABLE public.attribute_on_classstructure (
    id bigint NOT NULL,
    attributeposition integer,
    defaultvalue text,
    ownership character varying(255),
    required boolean,
    attribute_id bigint NOT NULL,
    classstructure_id bigint NOT NULL
);

ALTER TABLE public.attribute_on_classstructure OWNER TO OpenEAM;

CREATE TABLE public.attribute_value (
    id bigint NOT NULL,
    alnvalue text,
    comment text,
    datevalue bigint,
    numericvalue double precision,
    problem boolean,
    tablevalue bigint,
    yornvalue boolean,
    attribute_on_class_id bigint NOT NULL,
    bean_id bigint NOT NULL
);

ALTER TABLE public.attribute_value OWNER TO OpenEAM;

CREATE TABLE public.basicbeanwithclass (
    classstructure_id bigint NOT NULL
);

ALTER TABLE public.basicbeanwithclass OWNER TO OpenEAM;

CREATE TABLE public.class_usewith (
    classstructure_id bigint,
    usewith bytea
);

ALTER TABLE public.class_usewith OWNER TO OpenEAM;

CREATE TABLE public.classstructure (
    id bigint NOT NULL,
    classificationid text,
    classstructureid text,
    description text,
    parent_id bigint,
    status_id bigint
);

ALTER TABLE public.classstructure OWNER TO OpenEAM;

CREATE TABLE public.document (
    id bigint NOT NULL,
    creationdate timestamp without time zone,
    description text,
    documentnum text,
    modificationdate timestamp without time zone,
    status character varying(255) NOT NULL,
    classstructure_id bigint NOT NULL,
    parent_id bigint
);

ALTER TABLE public.document OWNER TO OpenEAM;

CREATE TABLE public.document_asset (
    assets_id bigint NOT NULL,
    documents_id bigint NOT NULL
);

ALTER TABLE public.document_asset OWNER TO OpenEAM;

CREATE TABLE public.location (
    id bigint NOT NULL,
    creationdate timestamp without time zone,
    description text,
    location text,
    modificationdate timestamp without time zone,
    status character varying(255) NOT NULL,
    classstructure_id bigint NOT NULL
);

ALTER TABLE public.location OWNER TO OpenEAM;

CREATE TABLE public.OpenEAM_sequence (
    name character varying(50) NOT NULL,
    seq numeric(38,0)
);

ALTER TABLE public.OpenEAM_sequence OWNER TO OpenEAM;

CREATE TABLE public.setting (
    name text NOT NULL,
    type character varying(255) NOT NULL,
    value text
);

ALTER TABLE public.setting OWNER TO OpenEAM;

CREATE TABLE public.users (
    id bigint NOT NULL,
    login character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    type character varying(255) NOT NULL,
    contact_id bigint
);

ALTER TABLE public.users OWNER TO OpenEAM;

COPY public.OpenEAM_sequence (name, seq) FROM stdin;
bean_check_hit	0
bean	15
user	0
bean_property	0
classstructure	0
attribute_on_classstructure	0
attribute	0
\.

COPY public.setting (name, type, value) FROM stdin;
metaversion	ALN	1.0.0
mode	ALN	WRITE
\.

ALTER TABLE ONLY public.asset
    ADD CONSTRAINT asset_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.attribute_on_classstructure
    ADD CONSTRAINT attribute_on_classstructure_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.attribute
    ADD CONSTRAINT attribute_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.attribute_value
    ADD CONSTRAINT attribute_value_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.classstructure
    ADD CONSTRAINT classstructure_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.document_asset
    ADD CONSTRAINT document_asset_pkey PRIMARY KEY (assets_id, documents_id);

ALTER TABLE ONLY public.document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.location
    ADD CONSTRAINT location_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.OpenEAM_sequence
    ADD CONSTRAINT OpenEAM_sequence_pkey PRIMARY KEY (name);

ALTER TABLE ONLY public.setting
    ADD CONSTRAINT setting_pkey PRIMARY KEY (name);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_login_key UNIQUE (login);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

CREATE UNIQUE INDEX attr_on_class_ndx ON public.attribute_on_classstructure USING btree (attribute_id, classstructure_id);

CREATE UNIQUE INDEX attr_value_ndx ON public.attribute_value USING btree (bean_id, attribute_on_class_id);

ALTER TABLE ONLY public.asset
    ADD CONSTRAINT fk_asset_classstructure_id FOREIGN KEY (classstructure_id) REFERENCES public.classstructure(id);

ALTER TABLE ONLY public.asset
    ADD CONSTRAINT fk_asset_parent_id FOREIGN KEY (parent_id) REFERENCES public.asset(id);

ALTER TABLE ONLY public.attribute_on_classstructure
    ADD CONSTRAINT fk_attribute_on_classstructure_attribute_id FOREIGN KEY (attribute_id) REFERENCES public.attribute(id);

ALTER TABLE ONLY public.attribute_on_classstructure
    ADD CONSTRAINT fk_attribute_on_classstructure_classstructure_id FOREIGN KEY (classstructure_id) REFERENCES public.classstructure(id);

ALTER TABLE ONLY public.attribute_value
    ADD CONSTRAINT fk_attribute_value_attribute_on_class_id FOREIGN KEY (attribute_on_class_id) REFERENCES public.attribute_on_classstructure(id);

ALTER TABLE ONLY public.basicbeanwithclass
    ADD CONSTRAINT fk_basicbeanwithclass_classstructure_id FOREIGN KEY (classstructure_id) REFERENCES public.classstructure(id);

ALTER TABLE ONLY public.class_usewith
    ADD CONSTRAINT fk_class_usewith_classstructure_id FOREIGN KEY (classstructure_id) REFERENCES public.classstructure(id);

ALTER TABLE ONLY public.classstructure
    ADD CONSTRAINT fk_classstructure_parent_id FOREIGN KEY (parent_id) REFERENCES public.classstructure(id);

ALTER TABLE ONLY public.contact
    ADD CONSTRAINT fk_contact_company_id FOREIGN KEY (company_id) REFERENCES public.company(id);

ALTER TABLE ONLY public.document
    ADD CONSTRAINT fk_document_classstructure_id FOREIGN KEY (classstructure_id) REFERENCES public.classstructure(id);

ALTER TABLE ONLY public.document
    ADD CONSTRAINT fk_document_parent_id FOREIGN KEY (parent_id) REFERENCES public.document(id);

ALTER TABLE ONLY public.location
    ADD CONSTRAINT fk_location_classstructure_id FOREIGN KEY (classstructure_id) REFERENCES public.classstructure(id);

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_users_contact_id FOREIGN KEY (contact_id) REFERENCES public.contact(id);


