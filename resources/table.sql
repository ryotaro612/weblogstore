DROP DATABASE IF EXISTS ranceworks;
CREATE DATABASE ranceworks;
USE ranceworks;

CREATE TABLE access_log(
  date_gmt DATE,
  time_gmt TIME,
  city VARCHAR(64),
  country_code CHAR(3),
  status_code CHAR(3),
  uri VARCHAR(255),
  remote_addr VARCHAR(39),
  http_referer varchar(512),
  http_user_agent VARCHAR(255),
  store_time TIMESTAMP
);
