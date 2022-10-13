/*
(4 rows affected)
Total execution time: 00:00:00.316

Alaska Airlines Inc.
SkyWest Airlines Inc.
United Air Lines Inc.
Virgin America
*/

SELECT DISTINCT c.name AS carrier
FROM CARRIERS AS c, FLIGHTS AS f
WHERE c.cid = f.carrier_id AND 
      f.origin_city = 'Seattle WA' AND
      f.dest_city = 'San Francisco CA'
ORDER BY c.name;
