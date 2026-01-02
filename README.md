# Clay Casto - Java Spring Boot bond portfolio risk analyser API
Please find my submission for the assessment here.
Here, you will find the spring boot application requested, screenshots of various REST calls and events, a database DDL and test datasets, test scripts, architecture documents, AI chat transcript, and notes.

I was able to leverage my existing knowledge of Spring Boot and PostgreSQL to set up. I used Spring's official project starter as an entrypoint. I designed the database architecture and implemented the tables, then created the API's objects, controllers, and services to match. I utilized Gemini AI mostly for algorithmic code specific to bond metrics.

I spent around 5 hours spread across two evenings to arrive at this completed solution.

This was quite a fun project, and I'm excited if this position holds more of the same!

API Endpoints:
- Bond
  -   localhost:8080/bond/create-bond (POST, body: string JSON)
  -   localhost:8080/bond/create-portfolio (POST, body: string JSON)
  -   localhost:8080/bond/get-portfolio/{id} (GET, path variable: int ID)
- Metrics
  -   localhost:8080/metrics/get-ytm-by-bond-id/{id} (GET, path variable: int ID)
  -   localhost:8080/metrics/get-duration-by-bond-id/{id} (GET, path variable: int ID)
  -   localhost:8080/metrics/get-modded-duration-by-bond-id/{id} (GET, path variable: int ID)
  -   localhost:8080/metrics/get-portfolio-weighted-avg-duration/{id} (GET, path variable: int ID)
 
## Quick Notes:
- leveraged my existing knowledge of springboot/PostgreSQL to set up
- start.spring.io (Spring's official project starter)
- designed/coded bond/portfolio controller/service/model/dao independently
- implemented DB per my design (based off of requirement doc)
- utilized AI to
  - learn more about Bonds
  - quickly get powerful metrics gathering algorithms for them
  - generate sample datasets with realistic attributes
  - tweak algorithm to take advantage of available data (coupon dates)
- A couple youtube videos on bonds that I watched while coding as supplemental learning:
  - https://www.youtube.com/watch?v=vAdn7aLHpO0
  - https://www.youtube.com/watch?v=7d9Lz0D0uzA

## Potential Improvements:
- bond description/title inclusion instead of just ISIN
- support for other currencies
- add swagger ui for faster prototyping/testing
- spring cloud profiles for multi-environment deployment
- dockerization for quick deployment
- new tables for historical retention of metrics
