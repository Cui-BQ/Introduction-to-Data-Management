/*
(327 rows affected)
Total execution time: 00:00:17.630

Guam TT	NULL
Pago Pago TT	NULL
Aguadilla PR	29.4339622641000
Anchorage AK	32.1460373998000
San Juan PR	33.8903607091000
Charlotte Amalie VI	40.0000000000000
Ponce PR	41.9354838709000
Fairbanks AK	50.6912442396000
Kahului HI	53.6649985281000
Honolulu HI	54.9088086922000
San Francisco CA	56.3076568265000
Los Angeles CA	56.6041076487000
Seattle WA	57.7554165533000
Long Beach CA	62.4541164132000
Kona HI	63.2821075740000
New York NY	63.4815197725000
Las Vegas NV	65.1630092883000
Christiansted VI	65.3333333333000
Newark NJ	67.1373555840000
Worcester MA	67.7419354838000
*/

WITH CityFlights AS (
    SELECT f.origin_city AS city, Count(f.origin_city) AS number
    FROM FLIGHTS AS f 
    WHERE f.actual_time < 180
    GROUP BY f.origin_city
)
SELECT DISTINCT f.origin_city AS city, 
                (c.number*1.0 / COUNT(f.origin_city)*1.0)*100 AS percentage
FROM FLIGHTS AS f LEFT OUTER JOIN CityFlights AS c 
ON f.origin_city = c.city
GROUP BY f.origin_city, c.number
ORDER BY (c.number*1.0 / COUNT(f.origin_city)*1.0)*100;

