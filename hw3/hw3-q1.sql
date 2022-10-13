/*
(334 rows affected) 
Total execution time: 00:00:14.596

Aberdeen SD	Minneapolis MN	106
Abilene TX	Dallas/Fort Worth TX	111
Adak Island AK	Anchorage AK	471
Aguadilla PR	New York NY	368
Akron OH	Atlanta GA	408
Albany GA	Atlanta GA	243
Albany NY	Atlanta GA	390
Albuquerque NM	Houston TX	492
Alexandria LA	Atlanta GA	391
Allentown/Bethlehem/Easton PA	Atlanta GA	456
Alpena MI	Detroit MI	80
Amarillo TX	Houston TX	390
Anchorage AK	Barrow AK	490
Appleton WI	Atlanta GA	405
Arcata/Eureka CA	San Francisco CA	476
Asheville NC	Chicago IL	279
Ashland WV	Cincinnati OH	84
Aspen CO	Los Angeles CA	304
Atlanta GA	Honolulu HI	649
Atlantic City NJ	Fort Lauderdale FL	212
*/

WITH MaxTime AS 
    (SELECT MAX(f.actual_time) AS time, f.origin_city as city
     FROM FLIGHTS AS f
     GROUP BY f.origin_city
     )
SELECT DISTINCT f.origin_city AS origin_city, f.dest_city AS dest_city, MaxTime.time AS time
FROM FLIGHTS AS f, MaxTime
WHERE MaxTime.time = f.actual_time AND MaxTime.city = f.origin_city
ORDER BY f.origin_city, f.dest_city;



