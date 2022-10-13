/*
(4 rows affected)
Total execution time: 00:00:00.084

Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America
*/

SELECT DISTINCT c.name AS carrier
FROM CARRIERS AS c 
WHERE c.cid IN (SELECT DISTINCT f.carrier_id
                FROM FLIGHTS AS f
                WHERE f.origin_city = 'Seattle WA' AND
                      f.dest_city = 'San Francisco CA')
ORDER BY c.name;
