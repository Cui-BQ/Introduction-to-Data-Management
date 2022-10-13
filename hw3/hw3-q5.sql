/*
(3 rows affected)
Total execution time: 00:00:03.953

Devils Lake ND
Hattiesburg/Laurel MS
St. Augustine FL
*/

(SELECT DISTINCT f.dest_city AS city
FROM FLIGHTS AS f)
EXCEPT
(SELECT DISTINCT f2.dest_city AS city
FROM FLIGHTS AS f1, FLIGHTS AS f2
WHERE f1.origin_city = 'Seattle WA' AND 
      f1.dest_city = f2.origin_city
UNION
SELECT DISTINCT f.dest_city AS city
FROM FLIGHTS AS f
WHERE f.origin_city = 'Seattle WA')
ORDER BY city;