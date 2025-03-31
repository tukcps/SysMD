# Modeling date and time

## Time
Time can be presented in the following units: second (s), minute (min), hour (h), day (d) year (a)
- 1 minute = 60 seconds
- 1 hour = 60 minutes
- 1 day = 24 hours
- 1 year = 365 days

Examples:
```
   t1: Real(20..50)[s] = 10.0 [s].  
   t2: Real(20..50)[min] = 10.0 [min]. 
```

## Date
Date is represented in the representation from ISO 8601. This leads to the following representations:

| Format according to ISO 8601 | Value ranges                                 |
|------------------------------|----------------------------------------------|
| Year (Y)                     | YYYY, four-digit, abbreviated to two-digit   |
| Month (M)                    | MM, 01 to 12                                 |
| Week (W)                     | WW, 01 to 53                                 |
| Day (D)	                     | DD, day of the month, 1 to 31                |
| Hour (h)                     | hh, 00 to 23, 24:00:00 as the end time       |
| Minute (m)                   | mm, 00 to 59                                 |
| Second (s)                   | ss, 00 to 59                                 |
| Decimal fraction (f)         | Fractions of seconds, any degree of accuracy |

(see https://www.ionos.com/digitalguide/websites/web-development/iso-8601/)

The following date representations are supported:
 
| Description                    | Unit     | Format                    | Example                  |
|--------------------------------|----------|---------------------------|--------------------------|
| only dates without time        | Date     | YYYY-MM-DD                | 2021-12-31               |
| dates with only month and year | Month    | YYYY-MM                   | 2021-12                  |
| only year without date         | Year     | YYYY                      | 2021                     |
| dates with time                | DateTime | YYYY-MM-DDThh:mm:ss       | 2021-12-31T12:00:00      |
| dates with time and timezone   | DateTime | YYYY-MM-DDThh:mm:ss+hh:mm | 2021-12-31T12:00:00+3:00 |



Some notes: 
- If no timezone is defined, the UTC timezone is used
- The explicit notation of time zones always specifies the difference to UTC
