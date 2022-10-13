/*
(256 rows affected)
Total execution time: 00:00:11.320

Aberdeen SD
Abilene TX
Adak Island AK
Aguadilla PR
Akron OH
Albany GA
Albany NY
Alexandria LA
Allentown/Bethlehem/Easton PA
Alpena MI
Amarillo TX
Appleton WI
Arcata/Eureka CA
Asheville NC
Ashland WV
Aspen CO
Atlantic City NJ
Augusta GA
Bakersfield CA
Bangor ME
*/

SELECT DISTINCT f2.dest_city AS city
FROM FLIGHTS AS f1, FLIGHTS AS f2
WHERE f1.origin_city = 'Seattle WA' AND 
      f1.dest_city = f2.origin_city AND
      f2.dest_city != 'Seattle WA'
EXCEPT
SELECT DISTINCT f.dest_city AS city
FROM FLIGHTS AS f
WHERE f.origin_city = 'Seattle WA'
ORDER BY city;
