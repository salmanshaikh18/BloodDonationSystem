# Blood Donation Management System

A small web app that keeps blood donor details and helps find suitable donors for a patient.

## Features

| Feature | Where |
|---|---|
| Register donors and view their details | Register donor, Donors, donor page |
| Search donors by blood group | Blood group filter on the Donors page |
| Find suitable donors for a patient: same and compatible groups, who can donate today, by city | Find donors |
| Record contact details and last donation date, plus a "Record donation today" button | Register / Edit, donor page |
| Update donor availability | "Mark as available / not available" on the donor page |
| Edit and delete donors | Donor page |
| Totals and donors per blood group | Home page |

## Tech stack

| Part | Technology |
|---|---|
| Language | Java 17 |
| Backend | Spring Boot 4 (Spring MVC, Spring Data JPA, Bean Validation) |
| Pages | JSP + JSTL, packaged as a WAR file |
| Styling | Tailwind CSS 4 (source files in `src/main/frontend/styles`) |
| Database | H2 on your computer, PostgreSQL (Neon) on the live site |
| Database changes | Flyway SQL migrations |
| Build | Maven (with the Maven Wrapper `mvnw`) |
| Hosting | Vercel (Docker container) |

## Run it on your computer

You only need **Java 17 or newer** (for example from [adoptium.net](https://adoptium.net)).
Maven, Node.js and Tailwind are downloaded automatically the first time.

```bash
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. Then open http://localhost:8080.

- The first run takes a few minutes while everything downloads.
- Data is saved in an H2 database in the `data` folder. On first start it is filled with
  12 made-up donors. Delete the `data` folder to start again from scratch.
- To see the tables, open http://localhost:8080/h2-console, paste the JDBC URL from
  `src/main/resources/application-local.properties`, user `sa`, no password.

Other commands:

```bash
./mvnw test            # run the tests
./mvnw package         # build target/app.war
java -jar target/app.war
```

**Changing the styling:** edit the files in `src/main/frontend/styles`. With Node.js
installed, run `npm run watch:css` in a second terminal and refresh the browser after each save.
Without Node.js, restart `./mvnw spring-boot:run` to rebuild the CSS.

## Project structure

```
src/main/java/com/group2/blooddonation/
├── BloodDonationApplication.java   starts the app
├── config/       SampleDataLoader (made-up donors), WebConfig
├── controller/   HomeController, DonorController, SearchController
├── dto/          DonorForm (form + validation), DonorSearchCriteria, DashboardStats
├── exception/    DonorNotFoundException, GlobalExceptionHandler (404 page)
├── model/        Donor (database table), BloodGroup, Gender, BloodGroupConverter
├── repository/   DonorRepository (Spring Data JPA)
└── service/      DonorService (business logic)

src/main/resources/
├── application.properties          shared settings
├── application-local.properties    your computer (H2)
├── application-prod.properties     live site (Neon PostgreSQL)
├── db/migration/                   SQL that creates the tables (Flyway)
└── static/                         JavaScript and images

src/main/frontend/styles/           Tailwind CSS: base, layout, components, pages
src/main/webapp/WEB-INF/
├── jsp/                            the pages
└── tags/                           reusable JSP pieces (page layout, badges, dates)
```

A request goes **Controller → Service → Repository → database**, and the controller then
chooses a JSP page to show the result.

## Where the syllabus topics are used

- **Collections:** `BloodGroup` keeps the compatibility rules in an `EnumMap<BloodGroup, Set<BloodGroup>>`
  built from `EnumSet`s. `DonorService` filters and sorts a `List<Donor>` with streams and a
  `Comparator`, and counts donors per blood group in an `EnumMap`.
- **Spring Boot:** controllers, a service and a repository, form validation, and profiles
  (`local` / `prod`) that switch the database.
- **Database connectivity:** Spring Data JPA and Hibernate talk to the database through JDBC.
  Flyway creates the `donors` table from `V1__create_donors_table.sql`.

## Donation rules used

- Donors must be 18 to 65 years old.
- Minimum gap between whole-blood donations: 90 days for men and 120 days for women
  (India's NBTC guidelines). "Other" uses the safer 120 days.
- Who can receive from whom:

| Patient | Can receive from |
|---|---|
| A+ | A+, A-, O+, O- |
| A- | A-, O- |
| B+ | B+, B-, O+, O- |
| B- | B-, O- |
| AB+ | everyone |
| AB- | AB-, A-, B-, O- |
| O+ | O+, O- |
| O- | O- |

## Put it online for free (GitHub + Vercel + Neon)

None of these steps need a credit card.

1. **GitHub:** create a free account and an empty repository, then push this project to it.
2. **Vercel:** sign up at [vercel.com](https://vercel.com) with your GitHub account (Hobby plan).
3. **New project:** Add New → Project → import the repository. Check that **Framework Preset**
   says **Container** (pick it from the list if not), so Vercel builds the `Dockerfile`.
   The first deploy fails because there is no database yet. That is expected.
4. **Database:** in the project, open **Storage** → **Neon** → choose the **Free** plan → connect
   it to the project. Vercel adds the database settings (`PGHOST`, `PGUSER`, `PGPASSWORD`, ...)
   by itself.
5. **Deploy again:** Deployments → ⋯ → Redeploy. Open the link Vercel gives you.

Tips:

- **Region:** `vercel.json` runs the app in Singapore (`sin1`), so create the Neon database in
  Singapore too (AWS ap-southeast-1). If they are far apart, every page waits on the database
  and the app may not start within Vercel's time limit.
- **Sample donors on the live site:** add the environment variable `APP_SAMPLE_DATA` = `true`
  in Vercel and redeploy. They are only added while the database is empty.
- **Sleeping:** the free site shuts down after a few minutes without visitors. The next visit
  takes a few seconds while it starts again, so open it once before a demo.
- **Updates:** every `git push` to GitHub deploys the new version automatically.
- **Public site:** anyone with the link can add or change donors, so don't enter real
  people's phone numbers.
