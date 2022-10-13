PRAGMA foreign_keys = ON;

CREATE TABLE CARRIERS (
    cid varchar(7) PRIMARY KEY,
    name varchar(83)
);

CREATE TABLE MONTHS (
    mid int PRIMARY KEY, 
    month varchar(9)
);

CREATE TABLE WEEKDAYS (
    did int PRIMARY KEY, 
    day_of_week varchar(9)
);

CREATE TABLE FLIGHTS (
    fid int PRIMARY KEY,
    month_id int REFERENCES MONTHS,
    day_of_month int,
    day_of_week_id int REFERENCES WEEKDAYS,
    carrier_id varchar(7) REFERENCES CARRIERS,
    flight_num int,
    origin_city varchar(34),
    origin_state varchar(47),
    dest_city varchar(34),
    dest_state varchar(46),
    departure_delay int,
    taxi_out int,
    arrival_delay int,
    canceled int,
    actual_time int,
    distance int,
    capacity int,
    price int
);

.mode csv
.import carriers.csv CARRIERS
.import months.csv MONTHS
.import weekdays.csv WEEKDAYS
.import flights-small.csv FLIGHTS



