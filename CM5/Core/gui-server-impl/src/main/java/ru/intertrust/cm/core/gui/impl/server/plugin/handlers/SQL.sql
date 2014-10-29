DROP TABLE perf_test1;

CREATE TABLE perf_test1
(
  id BIGINT PRIMARY KEY NOT NULL,
  status VARCHAR(64),
  status_num BIGINT
);

INSERT INTO perf_test1
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
    SELECT GENERATE_SERIES::bigint id, (random() * 10)::integer status_num
    FROM GENERATE_SERIES(1, 1100000)
  ) t;

-- drop statuses with data qty 2 times less than average
DELETE FROM perf_test1 WHERE status='status0' or status = 'status10';

CREATE TABLE perf_test2
(
  id BIGINT PRIMARY KEY NOT NULL,
  status VARCHAR(64),
  status_num BIGINT
);

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 1 status_num
         FROM GENERATE_SERIES(1, 100000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 2 status_num
         FROM GENERATE_SERIES(100001, 200000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 3 status_num
         FROM GENERATE_SERIES(200001, 300000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 4 status_num
         FROM GENERATE_SERIES(400001, 500000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 5 status_num
         FROM GENERATE_SERIES(500001, 600000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 6 status_num
         FROM GENERATE_SERIES(600001, 700000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 7 status_num
         FROM GENERATE_SERIES(700001, 800000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 8 status_num
         FROM GENERATE_SERIES(800001, 900000)
       ) t;

INSERT INTO perf_test2
  SELECT id, 'status' || status_num status, status_num status_num
  FROM (
         SELECT GENERATE_SERIES::bigint id, 9 status_num
         FROM GENERATE_SERIES(900001, 1000000)
       ) t;

CREATE INDEX status_index1 ON perf_test1 (status); --USING hash (status);
CREATE INDEX status_num_index1 ON perf_test1 (status_num); -- USING hash (status_num);
CREATE INDEX status_index2 ON perf_test2 (status); --USING hash (status);
CREATE INDEX status_num_index2 ON perf_test2 (status_num); -- USING hash (status_num);

ANALYZE VERBOSE perf_test1;
ANALYZE VERBOSE perf_test2;
