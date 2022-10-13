DROP TABLE IF EXISTS Truck;
DROP TABLE IF EXISTS Drives;
DROP TABLE IF EXISTS Car;
DROP TABLE IF EXISTS NonProfessionalDriver;
DROP TABLE IF EXISTS ProfessionalDriver;
DROP TABLE IF EXISTS Driver;
DROP TABLE IF EXISTS Vehicle;
DROP TABLE IF EXISTS Person;
DROP TABLE IF EXISTS InsuranceCo;


CREATE TABLE InsuranceCo (
    name VARCHAR(100) PRIMARY KEY,
    phone int 
);

CREATE TABLE Person (
    ssn int PRIMARY KEY,
    name VARCHAR(100) 
);

CREATE TABLE Vehicle (
    licensePlate VARCHAR(100) PRIMARY KEY,
    year int,
    maxLiability int,
    InsuranceCoName VARCHAR(100) REFERENCES InsuranceCo(name),
    OwnerSSN int REFERENCES Person(ssn)
);

CREATE TABLE Driver (
    ssn int PRIMARY KEY REFERENCES Person(ssn),
    DriverID VARCHAR(100) UNIQUE
);

CREATE TABLE NonProfessionalDriver (
    ssn int PRIMARY KEY REFERENCES Driver(ssn)
);

CREATE TABLE ProfessionalDriver (
    medicalHistory VARCHAR(100),
    ssn int PRIMARY KEY REFERENCES Driver(ssn)
);

CREATE TABLE Car (
    licensePlate VARCHAR(100) PRIMARY KEY REFERENCES Vehicle(licensePlate),
    make VARCHAR(100)

);

CREATE TABLE Drives (
    licensePlate VARCHAR(100) REFERENCES Car(licensePlate),
    ssn int REFERENCES NonProfessionalDriver(ssn),
    PRIMARY KEY (licensePlate, ssn)
);


CREATE TABLE Truck (
    capacity int,
    licensePlate VARCHAR(100) PRIMARY KEY REFERENCES Vehicle(licensePlate),
    ssn int REFERENCES ProfessionalDriver(ssn)
);

/*

b,
    Since the relationship between Vehicle and InsuranceCo is Many to One, 
    so I didn't make a table for "insures". I was using the attributes inside 
    the Vehicle "InsuranceCoName" and "maxLiability" to represent the "insures" 
    relationship. 


c,
    Drives is the relationship between the Car entity and the NonProfessionalDriver 
    entity, which is a many to many relationship. While Operates is the relationship 
    between the Truck entity and the ProfessionalDriver entity, which is a many to one 
    relationship. Therefore, I need to create a table for Drives because a car may have 
    more than one driver, but truck only has one driver.

*/
