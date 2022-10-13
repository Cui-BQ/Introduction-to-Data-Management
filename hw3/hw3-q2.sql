/*
(109 rows affected)
Total execution time: 00:00:07.272

Aberdeen SD
Abilene TX
Alpena MI
Ashland WV
Augusta GA
Barrow AK
Beaumont/Port Arthur TX
Bemidji MN
Bethel AK
Binghamton NY
Brainerd MN
Bristol/Johnson City/Kingsport TN
Butte MT
Carlsbad CA
Casper WY
Cedar City UT
Chico CA
College Station/Bryan TX
Columbia MO
Columbus GA
*/

SELECT DISTINCT f.origin_city AS city
FROM FLIGHTS AS f
GROUP BY f.origin_city
HAVING MAX(f.actual_time) < 180 OR MAX(f.actual_time) IS NULL
ORDER BY f.origin_city;