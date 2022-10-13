DROP TABLE IF EXISTS Reservations;
DROP TABLE IF EXISTS Users;

CREATE TABLE Users (
    username varchar(20) PRIMARY KEY,
    password varbinary(20) NOT NULL,
    passwordSalt varbinary(20) NOT NULL,
    balance int NOT NULL
);

CREATE TABLE Reservations (
    reservationID int PRIMARY KEY,       
    username varchar(20) REFERENCES Users(username),
    paid int NOT NULL,                                      -- 1 for paid, 0 for unpaid.
    cancelled int NOT NULL,                                 -- 1 for cancelled, 0 for uncancelled.
    day int NOT NULL,    
    price int NOT NULL,                             
    fid1 int REFERENCES FLIGHTS(fid) NOT NULL,
    fid2 int                                                -- 0 for direct flight
);



