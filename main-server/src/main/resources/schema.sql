DROP TABLE IF EXISTS compilations CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS events_compilations CASCADE;
DROP TABLE IF EXISTS requests CASCADE;

-- подборка событий
CREATE TABLE IF NOT EXISTS compilations (
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    pinned BOOLEAN,
    title  VARCHAR(128) NOT NULL
);

-- категория события
CREATE TABLE IF NOT EXISTS categories (
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name  VARCHAR(250) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE
);

-- локация события появляется в /events/{id}
CREATE TABLE IF NOT EXISTS locations (
    id  BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	annotation         VARCHAR(2000),
    category_id        BIGINT REFERENCES categories (id) ON DELETE RESTRICT,
	created_on         TIMESTAMP WITHOUT TIME ZONE,
	description        text,
	event_date         TIMESTAMP WITHOUT TIME ZONE,
    initiator_id       BIGINT REFERENCES users (id) ON DELETE CASCADE,
    location_id        BIGINT REFERENCES locations (id) ON DELETE CASCADE,
    paid               BOOLEAN,
    participant_limit  INTEGER,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    request_moderation BOOLEAN,
    state              VARCHAR(16),
    title              VARCHAR(120)
);

CREATE TABLE IF NOT EXISTS events_compilations (
    event          BIGINT REFERENCES events (id) ON DELETE CASCADE,
    compilation    BIGINT REFERENCES compilations (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS requests (
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    requester_id BIGINT REFERENCES users (id) ON DELETE CASCADE,
    event_id     BIGINT REFERENCES events (id) ON DELETE CASCADE,
    created      TIMESTAMP WITHOUT TIME ZONE,
    status       VARCHAR(16) NOT NULL
);
