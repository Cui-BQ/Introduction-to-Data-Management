------------------------------ part 1 ------------------------------
DROP TABLE IF EXISTS Sales;
CREATE TABLE Sales (
    name VARCHAR(100),
    discount VARCHAR(100),
    month VARCHAR(100),
    price int
);

/*
PRAGMA foreign_keys = ON;
.separator "\t"
.import data.tsv Sales
SELECT COUNT(*)
FROM Sales;
*/


------------------------------ part 2 ------------------------------

-- name → discount and name → month, not nontrivial functional dependencies
-- bar2	15%	aug	59
-- bar2 33% dec 59

-- name → price
SELECT *
FROM Sales AS s1, Sales AS s2
WHERE s1.name = s2.name AND s1.price != s2.price;

-- month → price, not nontrivial functional dependency
-- bar2	15%	aug	59
-- bar6	15%	aug	99

-- month -> discount
SELECT *
FROM Sales AS s1, Sales AS s2
WHERE s1.month = s2.month AND s1.discount != s2.discount;

-- discount → price, not nontrivial functional dependency
-- bar3	10%	oct	59
-- bar6	10%	feb	99

-- name, discount -> price, month
SELECT *
FROM Sales AS s1, Sales AS s2
WHERE s1.name = s2.name AND s1.discount = s2.discount AND
      s1.price != s2.price AND s1.month != s2.month;

-- month, price -> discount, name
SELECT *
FROM Sales AS s1, Sales AS s2
WHERE s1.month = s2.month AND s1.price = s2.price AND
      s1.discount != s2.discount AND s1.discount != s2.discount;


------------------------------ part 3 ------------------------------
/*
Sales(name, discount, month, price) with functional dependencies 
(name → price); (month -> discount); (name, discount -> price, month); 
and (month, price -> discount, name).

Since {name}+ = {name, price}, which violates the BCNF condition. 
So split Sales to Sales1(name, price), and Sales2(name, month, discount).
Since {month}+ = {month, discount}, so Sales2 violates the BCNF condition.
Split Sales2 to Sales21(month, discount), and Sales22(name, month).
Therefore, Sales1(name, price), Sales21(month, discount), and Sales22(name, month).
*/

DROP TABLE IF EXISTS Sales22;
DROP TABLE IF EXISTS Sales21;
DROP TABLE IF EXISTS Sales1;

CREATE TABLE Sales1 (
    name VARCHAR(100) PRIMARY KEY,
    price int
);

CREATE TABLE Sales21 (
    month VARCHAR(100) PRIMARY KEY,
    discount VARCHAR(100)
);

CREATE TABLE Sales22 (
    name VARCHAR(100) REFERENCES Sales1(name),
    month VARCHAR(100) REFERENCES Sales21(month),
    PRIMARY KEY (name, month)
);


------------------------------ part 4 ------------------------------

INSERT INTO Sales1
SELECT DISTINCT name, price 
From Sales;

SELECT COUNT(*) 
From Sales1;
-- 36 rows

INSERT INTO Sales21
SELECT DISTINCT month, discount
From Sales;

SELECT COUNT(*) 
From Sales21;
-- 12 rows

INSERT INTO Sales22
SELECT DISTINCT name, month
From Sales;

SELECT COUNT(*) 
From Sales22;
-- 426 rows