# Project Details

How the Blood Donation Management System is built: the technologies, the folder structure,
and how each part of the code works. For what the app *does*, see [FEATURES.md](FEATURES.md).
For putting it online, see [DEPLOYMENT.md](DEPLOYMENT.md). For the UML and DFD diagrams,
see [DIAGRAMS.md](DIAGRAMS.md).

## Contents

1. [Tech stack](#1-tech-stack)
2. [Architecture](#2-architecture)
3. [Folder structure](#3-folder-structure)
4. [How a request travels through the code](#4-how-a-request-travels-through-the-code)
5. [The Java code, class by class](#5-the-java-code-class-by-class)
6. [Pages and URLs](#6-pages-and-urls)
7. [Database](#7-database)
8. [Settings, profiles and environment variables](#8-settings-profiles-and-environment-variables)
9. [Front end: JSP pages and Tailwind CSS](#9-front-end-jsp-pages-and-tailwind-css)
10. [Build: Maven, WAR and the Maven Wrapper](#10-build-maven-war-and-the-maven-wrapper)
11. [Docker and Vercel](#11-docker-and-vercel)
12. [Tests](#12-tests)
13. [Where the syllabus topics are used](#13-where-the-syllabus-topics-are-used)
14. [Good practices followed](#14-good-practices-followed)
15. [Viva questions and answers](#15-viva-questions-and-answers)

---

## 1. Tech stack

| Part | Technology | Version | Why |
|---|---|---|---|
| Language | Java | 17 (runs on 17 or newer) | Syllabus language; 17 is a long-term-support version found on most lab PCs |
| Framework | Spring Boot | 4.1.2 | Sets up the web server, database and settings with little code |
| Web layer | Spring MVC | 7.0 | Controllers that receive requests and choose a page |
| Pages | JSP + JSTL 3 (Jakarta) | Tomcat 11 Jasper | Server-side HTML templates |
| Form checks | Bean Validation (Hibernate Validator) | - | `@NotBlank`, `@Min`, `@Pattern` rules on form fields |
| Database access | Spring Data JPA + Hibernate | Hibernate 7.4 | Saves Java objects as table rows and writes SQL for us |
| Connection pool | HikariCP | 7.0 | Reuses database connections |
| Table creation | Flyway | 12.4 | Runs the SQL file that creates the `donors` table |
| Database (computer) | H2 | 2.4 | Small database saved in a file, nothing to install |
| Database (live) | PostgreSQL on Neon | 18 | Free hosted database |
| Styling | Tailwind CSS | 4.3.5 | Utility-first CSS, built into one small file |
| JavaScript | Plain JavaScript | - | Only a "are you sure?" pop-up before deleting |
| Build tool | Maven + Maven Wrapper | 3.10 | Downloads libraries, builds and tests the app |
| CSS build | frontend-maven-plugin + Node.js | Node 24 | Maven downloads Node and runs Tailwind automatically |
| Web server | Embedded Apache Tomcat | 11.0 | Runs inside the app, no separate server needed |
| Container | Docker | - | Packages the app for Vercel |
| Hosting | Vercel (Container runtime) | Hobby plan | Free hosting that builds the Dockerfile |
| Tests | JUnit 5, AssertJ, Spring MockMvc | - | 27 automatic tests |

---

## 2. Architecture

The app follows the usual **layered** Spring structure. Each layer only talks to the layer
below it:

```
Browser
   │  HTTP request (GET /donors, POST /donors ...)
   ▼
Controller  ─────────────►  JSP page (HTML sent back to the browser)
   │  calls
   ▼
Service       business rules: search, eligibility, statistics
   │  calls
   ▼
Repository    Spring Data JPA: turns method names into SQL
   │  JDBC
   ▼
Database      H2 (your computer) or PostgreSQL on Neon (live)
```

| Layer | Package | Job |
|---|---|---|
| Controller | `controller` | Reads the request, calls the service, picks the JSP page |
| Service | `service` | All business logic, wrapped in database transactions |
| Repository | `repository` | Reads and writes the `donors` table |
| Model | `model` | The `Donor` entity and the `BloodGroup` / `Gender` enums |
| DTO | `dto` | Objects that carry form data and page data |
| View | `src/main/webapp/WEB-INF` | JSP pages and tag files that produce HTML |

See the [class diagrams](DIAGRAMS.md#2-class-diagrams) for all classes and their links.

---

## 3. Folder structure

```
BloodDonationSystem/
├── pom.xml                          Maven: libraries, WAR packaging, build steps
├── mvnw, mvnw.cmd, .mvn/            Maven Wrapper (Mac/Linux and Windows)
├── package.json, package-lock.json  Tailwind CSS version and build scripts
├── Dockerfile                       How Vercel builds and runs the app
├── vercel.json                      Tells Vercel: build the Dockerfile, run in Singapore
├── .gitignore, .dockerignore, .gitattributes
├── README.md
├── docs/                            This documentation, diagrams and screenshots
│
└── src/
    ├── main/
    │   ├── java/com/group2/blooddonation/
    │   │   ├── BloodDonationApplication.java    Starts the app
    │   │   ├── config/
    │   │   │   ├── SampleDataLoader.java        Adds 12 made-up donors to an empty database
    │   │   │   └── WebConfig.java               Cache-safe CSS/JS file names
    │   │   ├── controller/
    │   │   │   ├── HomeController.java          /
    │   │   │   ├── DonorController.java         /donors/**
    │   │   │   └── SearchController.java        /search
    │   │   ├── dto/
    │   │   │   ├── DonorForm.java               Register/Edit form + validation rules
    │   │   │   ├── DonorSearchCriteria.java     Search filters
    │   │   │   └── DashboardStats.java          Home page numbers (Java record)
    │   │   ├── exception/
    │   │   │   ├── DonorNotFoundException.java
    │   │   │   └── GlobalExceptionHandler.java  Turns errors into the 404 page
    │   │   ├── model/
    │   │   │   ├── Donor.java                   The donors table as a Java class
    │   │   │   ├── BloodGroup.java              8 groups + compatibility rules
    │   │   │   ├── BloodGroupConverter.java     Saves "A+" instead of "A_POSITIVE"
    │   │   │   └── Gender.java                  Male/Female/Other + donation gap
    │   │   ├── repository/
    │   │   │   └── DonorRepository.java         Database queries
    │   │   └── service/
    │   │       └── DonorService.java            Business logic
    │   │
    │   ├── resources/
    │   │   ├── application.properties           Shared settings
    │   │   ├── application-local.properties     Your computer: H2 database
    │   │   ├── application-prod.properties      Live site: Neon PostgreSQL
    │   │   ├── messages.properties              Friendly form error messages
    │   │   ├── db/migration/
    │   │   │   └── V1__create_donors_table.sql  Creates the donors table (Flyway)
    │   │   └── static/
    │   │       ├── images/logo.svg              Logo and browser-tab icon
    │   │       └── js/app.js                    "Are you sure?" pop-ups
    │   │
    │   ├── frontend/styles/                     Tailwind CSS source files
    │   │   ├── main.css                         Entry file: imports all the others
    │   │   ├── base/        theme.css (colours, fonts), base.css (plain HTML elements)
    │   │   ├── layout/      page.css, header.css, footer.css
    │   │   ├── components/  buttons, cards, forms, tables, badges, chips, alerts
    │   │   └── pages/       home.css, donors.css, search.css, error.css
    │   │
    │   └── webapp/WEB-INF/
    │       ├── jsp/
    │       │   ├── home.jsp
    │       │   ├── donors/  list.jsp, details.jsp, form.jsp (register + edit)
    │       │   ├── search/  search.jsp
    │       │   ├── error/   404.jsp, 500.jsp
    │       │   └── layout/  header.jspf, footer.jspf
    │       └── tags/
    │           ├── page.tag                 Shared page layout
    │           ├── blood-group-badge.tag    "A+" pill
    │           ├── availability-badge.tag   "Can donate now" / "Not available" ...
    │           ├── date.tag                 Shows dates as "10 Aug 2026"
    │           └── pagination.tag           "Showing 1-10 of 12", Previous / Next, page numbers
    │
    └── test/
        ├── java/com/group2/blooddonation/
        │   ├── model/BloodGroupTest.java
        │   ├── service/DonorServiceTest.java
        │   └── controller/DonorControllerTest.java
        └── resources/application-test.properties
```

Files that are **made by the build** and never edited by hand (all ignored by Git):
`target/` (compiled app, `app.war`, the built `app.css`), `node/` and `node_modules/`
(Node.js and Tailwind), `data/` (the local H2 database).

---

## 4. How a request travels through the code

**Example 1: page 2 of the O- donors** (`GET /donors?bloodGroup=O_NEGATIVE&page=2`)

1. **Tomcat** (inside the app) receives the request and gives it to Spring's `DispatcherServlet`.
2. Spring finds the method mapped to `GET /donors`: `DonorController.list(...)`. It converts
   the text `O_NEGATIVE` into the enum value `BloodGroup.O_NEGATIVE`, and `"2"` into the int `2`.
3. `list` calls `donorService.findPage(BloodGroup.O_NEGATIVE, 2)`.
4. The service builds `PageRequest.of(1, 10, sort by name)` (Spring Data counts pages from 0)
   and calls `donorRepository.findByBloodGroup(...)`. Spring Data JPA turns that into two
   SQL queries:
   ```sql
   SELECT * FROM donors WHERE blood_group = 'O-' ORDER BY lower(full_name), id LIMIT 10 OFFSET 10
   SELECT count(*) FROM donors WHERE blood_group = 'O-'
   ```
   (`BloodGroupConverter` turned `O_NEGATIVE` into `'O-'`.)
5. Hibernate turns each row into a `Donor` object. The result is a `Page<Donor>`: the 10
   donors plus the page number, total pages and total donors.
6. The controller puts the page in the **model** and returns the view name `"donors/list"`.
7. Spring adds the prefix and suffix from `application.properties` and runs
   `/WEB-INF/jsp/donors/list.jsp`. The JSP loops over the donors with `<c:forEach>`, and
   `<ui:pagination>` writes the "Showing 11–20 of 25" line and the page links.
8. The browser shows the page and loads `/css/app-<hash>.css` and `/js/app-<hash>.js`.

**Example 2: registering a donor** (`POST /donors`) follows the
**Post / Redirect / Get** pattern:

1. Spring copies the form fields into a `DonorForm` object and runs its validation rules (`@Valid`).
2. The controller also checks that the mobile number isn't already used.
3. **If there are errors:** the same form page is shown again, with each message next to its field.
4. **If everything is fine:** the service saves the donor, and the controller answers with a
   **redirect** to `/donors/{id}`, plus a one-time "flash" success message.
5. The browser loads the donor's page. Refreshing it won't submit the form twice.

The [sequence diagrams](DIAGRAMS.md#3-sequence-diagrams) show these steps in detail.

---

## 5. The Java code, class by class

### `BloodDonationApplication`

The `main` method starts Spring Boot. `@SpringBootApplication` switches on
auto-configuration and scans the packages for controllers, services and so on. It extends
`SpringBootServletInitializer`, so the same `app.war` could also be put into an external
Tomcat server.

### Package `model`

**`Donor`** is a JPA **entity**: one object = one row in the `donors` table.

- `@Entity`, `@Table(name = "donors")`, `@Id` and `@GeneratedValue(strategy = IDENTITY)` map
  the class to the table and let the database number the rows.
- `@Column` sets column names, lengths and NOT NULL.
- `@Enumerated(EnumType.STRING)` saves `gender` as text (`MALE`, `FEMALE`, `OTHER`).
- `@Convert(converter = BloodGroupConverter.class)` saves `bloodGroup` as `A+`, `O-` ...
- `@CreationTimestamp` and `@UpdateTimestamp` fill `createdAt` and `updatedAt` automatically.
- Three methods work out the donation status. They are not stored in the database:

```java
public LocalDate getNextEligibleDate() {          // last donation + 90 or 120 days
    if (lastDonationDate == null) return null;
    return lastDonationDate.plusDays(gender.getDonationGapDays());
}
public boolean isEligibleToDonate() {             // never donated, or the date has passed
    LocalDate next = getNextEligibleDate();
    return next == null || !LocalDate.now().isBefore(next);
}
public boolean isReadyToDonate() {                // willing AND allowed
    return available && isEligibleToDonate();
}
```

**`Gender`** is an enum with a label and the minimum gap between donations: `MALE` 90 days,
`FEMALE` 120 days, `OTHER` 120 days.

**`BloodGroup`** is an enum of the 8 groups. It holds the compatibility rules in a
**Collections** map:

```java
private static final Map<BloodGroup, Set<BloodGroup>> CAN_RECEIVE_FROM = new EnumMap<>(BloodGroup.class);
static {
    CAN_RECEIVE_FROM.put(O_NEGATIVE, EnumSet.of(O_NEGATIVE));
    CAN_RECEIVE_FROM.put(A_POSITIVE, EnumSet.of(O_NEGATIVE, O_POSITIVE, A_NEGATIVE, A_POSITIVE));
    CAN_RECEIVE_FROM.put(AB_POSITIVE, EnumSet.allOf(BloodGroup.class));
    // ... one line for each of the 8 groups
}
```

- `getCompatibleDonors()`: the donor groups a patient of this group can receive from.
- `getCanDonateTo()`: the reverse, the patient groups this donor can give to.
- `fromLabel("O-")`: finds the enum from its label.

**`BloodGroupConverter`** implements JPA's `AttributeConverter<BloodGroup, String>`, so the
database column holds readable values like `AB+` instead of `AB_POSITIVE`.

### Package `repository`

**`DonorRepository`** is an **interface** that extends `JpaRepository<Donor, Long>`. Spring
Data JPA writes the code at run time. From `JpaRepository` you get `save`, `findById`,
`findAll`, `findAll(Pageable)`, `delete` and `count` for free. The other methods are
**derived queries**: the SQL comes from the method name.

| Method | SQL it runs |
|---|---|
| `findAll(pageable)` (built in) | `SELECT * FROM donors ORDER BY ... LIMIT 10 OFFSET ?` + a `count(*)` |
| `findByBloodGroup(g, pageable)` | `... WHERE blood_group = ? ORDER BY ... LIMIT 10 OFFSET ?` + a `count(*)` |
| `findByBloodGroupIn(groups)` | `... WHERE blood_group IN (?, ?, ...)` |
| `findTop5ByOrderByCreatedAtDesc()` | `... ORDER BY created_at DESC LIMIT 5` |
| `existsByPhone(phone)` | is there a row `WHERE phone = ?` |
| `existsByPhoneAndIdNot(phone, id)` | same, ignoring the donor being edited |

### Package `service`

**`DonorService`** holds all business logic. The class has `@Transactional(readOnly = true)`
and every method that changes data has `@Transactional`, so each action either fully
succeeds or is fully undone.

- **Changing data:** `register`, `update`, `changeAvailability`, `recordDonation`, `delete`.
  For updates there is no explicit `save()` call: Hibernate notices the changed fields of a
  loaded entity ("dirty checking") and runs `UPDATE` when the transaction ends.
- **`getById`:** throws `DonorNotFoundException` if the id doesn't exist.
- **`findPage`** gives one page of the donors list (10 per page), using Spring Data's
  `Pageable`, so the database only sends those 10 rows:

```java
public static final int PAGE_SIZE = 10;
private static final Sort BY_NAME = Sort.by(Sort.Order.asc("fullName").ignoreCase(), Sort.Order.asc("id"));

public Page<Donor> findPage(BloodGroup bloodGroup, int pageNumber) {
    // people count pages from 1, Spring Data from 0; keep the number in a safe range
    int pageIndex = Math.min(Math.max(pageNumber, 1), Integer.MAX_VALUE / PAGE_SIZE) - 1;
    Page<Donor> page = fetchPage(bloodGroup, pageIndex);
    if (page.getTotalPages() > 0 && page.getNumber() >= page.getTotalPages()) {
        page = fetchPage(bloodGroup, page.getTotalPages() - 1);   // past the end: show the last page
    }
    return page;
}

private Page<Donor> fetchPage(BloodGroup bloodGroup, int pageIndex) {
    Pageable pageable = PageRequest.of(pageIndex, PAGE_SIZE, BY_NAME);
    return bloodGroup == null
            ? donorRepository.findAll(pageable)
            : donorRepository.findByBloodGroup(bloodGroup, pageable);
}
```

  Sorting by `id` after the name keeps the order fixed when two donors share a name, so
  no donor appears on two pages or is skipped.
- **`findSuitableDonors`** is the search, built with Java Streams:

```java
Set<BloodGroup> donorGroups = criteria.isIncludeCompatible()
        ? patientGroup.getCompatibleDonors()      // e.g. A+ -> {A+, A-, O+, O-}
        : EnumSet.of(patientGroup);               // only the same group

Comparator<Donor> bestMatchFirst = Comparator
        .comparing((Donor d) -> d.getBloodGroup() != patientGroup)   // same group first
        .thenComparing(d -> !d.isReadyToDonate())                    // ready today first
        .thenComparing(Donor::getFullName, String.CASE_INSENSITIVE_ORDER);

return donorRepository.findByBloodGroupIn(donorGroups).stream()
        .filter(d -> city == null || d.getCity().toLowerCase().contains(city))
        .filter(d -> !criteria.isOnlyReady() || d.isReadyToDonate())
        .sorted(bestMatchFirst)
        .toList();
```

- **`getDashboardStats`** counts donors per blood group in an `EnumMap`, starting every group
  at 0 so groups with no donors still appear:

```java
Map<BloodGroup, Long> perGroup = new EnumMap<>(BloodGroup.class);
for (BloodGroup g : BloodGroup.values()) perGroup.put(g, 0L);
for (Donor d : donors) perGroup.merge(d.getBloodGroup(), 1L, Long::sum);
```

### Package `dto`

DTO stands for Data Transfer Object: a plain object that carries data between the page and
the code.

- **`DonorForm`** holds the Register/Edit form fields and their **validation rules**:

  | Field | Annotations |
  |---|---|
  | `fullName` | `@NotBlank`, `@Size(max = 100)` |
  | `age` | `@NotNull`, `@Min(18)`, `@Max(65)` |
  | `gender`, `bloodGroup` | `@NotNull` |
  | `phone` | `@NotBlank`, `@Pattern(regexp = "[6-9]\\d{9}")` |
  | `email` | `@Email`, `@Size(max = 120)` |
  | `city` | `@NotBlank`, `@Size(max = 60)` |
  | `lastDonationDate` | `@PastOrPresent`, `@DateTimeFormat(iso = DATE)` |

  Its setters tidy the input (trim spaces, remove `+91`, lowercase the email).
  `DonorForm.from(donor)` fills the Edit form, and `form.applyTo(donor)` copies the values
  back. The entity is never bound straight from the request, so a crafted request can't
  change fields like `id` or `createdAt`.
- **`DonorSearchCriteria`**: the search filters. `includeCompatible` and `onlyReady` start as `true`.
- **`DashboardStats`**: a Java **record** (a short immutable data class) with the home page numbers.

### Package `controller`

Each controller method returns the **name of a JSP page** (for example `"donors/list"`) or
a **redirect** (`"redirect:/donors/5"`).

- **`HomeController`**: `GET /`, the dashboard.
- **`SearchController`**: `GET /search`. A broken link such as `?bloodGroup=XYZ` just shows
  the empty search form.
- **`DonorController`**: everything under `/donors` (see [Pages and URLs](#6-pages-and-urls)).
  - `list` takes `@RequestParam(defaultValue = "1") int page` and puts both the
    `Page<Donor>` (`donorPage`) and its 10 donors (`donors`) in the model.
  - `@ModelAttribute` methods give every donor page the lists of blood groups and genders,
    and today's date (the date picker can't go past today).
  - `@Valid @ModelAttribute("donorForm") DonorForm form, BindingResult result` runs the
    validation, and `rejectIfPhoneTaken` adds the "number already used" error.
  - `RedirectAttributes.addFlashAttribute("successMessage", ...)` passes the green message
    across the redirect.

### Package `exception`

- **`DonorNotFoundException`** extends `RuntimeException`.
- **`GlobalExceptionHandler`** is a `@ControllerAdvice`. Its `@ExceptionHandler` catches
  `DonorNotFoundException` and `MethodArgumentTypeMismatchException` (for example `/donors/abc`)
  from any controller, sets status 404 and shows `error/404.jsp`.
- Any other error goes to Spring Boot's error handling, which shows `error/404.jsp` or
  `error/500.jsp` without stack traces.

### Package `config`

- **`SampleDataLoader`** is an `ApplicationRunner`, so it runs once after start-up. It only
  exists when `app.sample-data.enabled=true` (`@ConditionalOnBooleanProperty`), and only
  adds donors if the table is empty.
- **`WebConfig`** registers `ResourceUrlEncodingFilter`. Together with
  `spring.web.resources.chain.strategy.content.enabled=true`, it changes `/css/app.css` in
  the pages into `/css/app-<content-hash>.css`. Browsers can cache the file for a year, and
  any change creates a new name, so visitors always get the latest CSS.

---

## 6. Pages and URLs

| Method | URL | Controller method | Page / result |
|---|---|---|---|
| GET | `/` | `HomeController.home` | `home.jsp` |
| GET | `/donors` | `DonorController.list` | `donors/list.jsp`, page 1 (10 donors) |
| GET | `/donors?page=2` | `DonorController.list` | `donors/list.jsp`, page 2 |
| GET | `/donors?bloodGroup=A_POSITIVE` | `DonorController.list` | `donors/list.jsp`, filtered |
| GET | `/donors/new` | `showRegisterForm` | `donors/form.jsp` (empty) |
| POST | `/donors` | `register` | redirect to `/donors/{id}`, or the form with errors |
| GET | `/donors/{id}` | `details` | `donors/details.jsp` |
| GET | `/donors/{id}/edit` | `showEditForm` | `donors/form.jsp` (filled in) |
| POST | `/donors/{id}` | `update` | redirect to `/donors/{id}`, or the form with errors |
| POST | `/donors/{id}/availability` | `changeAvailability` | redirect to `/donors/{id}` |
| POST | `/donors/{id}/donations` | `recordDonation` | redirect to `/donors/{id}` |
| POST | `/donors/{id}/delete` | `delete` | redirect to `/donors` |
| GET | `/search?bloodGroup=...&city=...` | `SearchController.search` | `search/search.jsp` |
| GET | `/h2-console` | (H2) | Database browser, **only on your computer** |

Changes use **POST**, never GET, so a link or a search engine can't delete a donor by accident.

---

## 7. Database

### The `donors` table

Created by `src/main/resources/db/migration/V1__create_donors_table.sql`:

| Column | Type | Rules |
|---|---|---|
| `id` | BIGINT | Primary key, numbered automatically (`GENERATED BY DEFAULT AS IDENTITY`) |
| `full_name` | VARCHAR(100) | NOT NULL |
| `age` | INTEGER | NOT NULL, CHECK 18 to 65 |
| `gender` | VARCHAR(10) | NOT NULL (`MALE`, `FEMALE`, `OTHER`) |
| `blood_group` | VARCHAR(3) | NOT NULL, CHECK one of the 8 labels (`A+` ...) |
| `phone` | VARCHAR(10) | NOT NULL, **UNIQUE** |
| `email` | VARCHAR(120) | may be empty |
| `city` | VARCHAR(60) | NOT NULL |
| `last_donation_date` | DATE | may be empty (never donated) |
| `available` | BOOLEAN | NOT NULL, default TRUE |
| `created_at`, `updated_at` | TIMESTAMP | NOT NULL, set by Hibernate |

There is also an **index** on `blood_group`, because every search filters by it.

### Flyway

On start-up, Flyway looks at `db/migration`, runs any SQL file that hasn't run on this
database yet, and records it in its own table, `flyway_schema_history`. To change the table
later, add a new file (`V2__add_weight_column.sql`). Never edit `V1`.

Hibernate is set to `ddl-auto=validate`: it doesn't create tables, it only checks at
start-up that the `Donor` class matches the table Flyway made.

### Two databases, same code

| | Your computer | Live site |
|---|---|---|
| Database | H2, saved in `./data/blood-donation.mv.db` | PostgreSQL on Neon |
| Settings file | `application-local.properties` | `application-prod.properties` |
| Driver | `org.h2.Driver` | `org.postgresql.Driver` |

H2 runs in **PostgreSQL mode**, so the same SQL file works on both. Spring Boot picks the
driver from the JDBC URL. **Database connectivity** works like this:
Spring Data JPA → Hibernate → JDBC → driver → database, and HikariCP keeps a small pool of
open connections.

You can look at the local database at http://localhost:8080/h2-console. Use the JDBC URL from
`application-local.properties`, user `sa`, no password, then for example:

```sql
SELECT full_name, blood_group, city, last_donation_date, available FROM donors;
```

---

## 8. Settings, profiles and environment variables

A **profile** is a named group of settings. `application.properties` says
`spring.profiles.default=local`, so on your computer the `local` profile is used. The
Dockerfile sets `SPRING_PROFILES_ACTIVE=prod` for the live site.

| File | Important settings |
|---|---|
| `application.properties` | `server.port=${PORT:8080}`, JSP prefix/suffix, cache-safe file names, `ddl-auto=validate`, error pages without stack traces |
| `application-local.properties` | H2 file database, H2 console on, sample data on, no CSS caching |
| `application-prod.properties` | PostgreSQL from environment variables, 3 database connections, lazy start, https-aware links, CSS cached for a year |
| `src/test/resources/application-test.properties` | In-memory H2 that disappears after the tests |

**Environment variables on the live site** (set by Vercel and its Neon integration):

| Variable | Set by | Used for |
|---|---|---|
| `PGHOST`, `PGHOST_UNPOOLED` | Neon integration | Database address (the direct one is preferred) |
| `PGDATABASE`, `PGUSER`, `PGPASSWORD` | Neon integration | Database name and login |
| `APP_SAMPLE_DATA` | You (optional) | `true` adds 12 made-up donors to an empty database |
| `PORT` | Dockerfile (80) | The port the web server listens on |
| `SPRING_PROFILES_ACTIVE` | Dockerfile (`prod`) | Chooses `application-prod.properties` |

No password is ever written in the code or on GitHub.

**Lazy start on the live site:** `spring.main.lazy-initialization=true` makes Spring create
each part only when it is first needed. The web server opens its port quickly (Vercel only
waits about 28 seconds), and the database set-up happens during the first visit.

---

## 9. Front end: JSP pages and Tailwind CSS

### JSP pages

**JSP** (Jakarta Server Pages) is HTML with special tags that run on the server.

- **JSTL core tags** (`<%@ taglib prefix="c" uri="jakarta.tags.core" %>`):
  `<c:forEach>` loops, `<c:if>` / `<c:choose>` decide, `<c:url>` builds links,
  and `<c:out>` prints text **safely**: it escapes `<` and `>` so a name like
  `<script>` can't run as code (protects against XSS).
- **EL** (Expression Language) reads values: `${donor.fullName}` calls `donor.getFullName()`,
  and `${donor.eligibleToDonate}` calls `isEligibleToDonate()`.
- **Spring form tags** (`<form:form>`, `<form:input>`, `<form:errors>`) fill the fields from
  `DonorForm`, keep what the user typed after an error, and show error messages.

**Tag files** in `WEB-INF/tags` are small reusable components, used like
`<ui:blood-group-badge group="${donor.bloodGroup}"/>`:

| Tag file | Purpose |
|---|---|
| `page.tag` | The shared layout: `<head>`, header, green success message, footer. Every page is wrapped in `<ui:page title="...">` |
| `blood-group-badge.tag` | The red "A+" pill |
| `availability-badge.tag` | "Can donate now" / "Can donate from ..." / "Not available" |
| `date.tag` | Formats a date as `10 Aug 2026` |
| `pagination.tag` | "Showing 1–10 of 12", Previous / Next and page numbers for any Spring Data `Page`. Keeps a filter (like `bloodGroup`) in every link, and hides itself when there is only one page |

`header.jspf` and `footer.jspf` are fragments included by `page.tag`.

### Tailwind CSS

Tailwind is a CSS framework of small **utility classes** (`flex`, `p-4`, `text-red-700` ...).
Here the utilities are combined into readable **component classes** with `@apply`, so the
JSP stays clean:

```css
/* src/main/frontend/styles/components/buttons.css */
@layer components {
    .btn         { @apply inline-flex items-center rounded-lg px-4 py-2.5 text-sm font-semibold; }
    .btn-primary { @apply bg-brand-600 text-white hover:bg-brand-700; }
}
```

```html
<a class="btn btn-primary" href="/donors/new">Register donor</a>
```

| Folder | What's inside |
|---|---|
| `base/theme.css` | Design tokens: the blood-red brand colours, fonts, shadows. Change a colour here and the whole site follows |
| `base/base.css` | Defaults for `body`, headings, links, focus outline |
| `layout/` | Page width, header, footer |
| `components/` | Buttons, cards, form fields, tables, badges, chips, alerts, pagination |
| `pages/` | Styles used by one page only |

**How the CSS is built:** `main.css` imports every file. During `mvn package` (or
`spring-boot:run`), the `frontend-maven-plugin` downloads Node.js into `./node`, runs
`npm ci`, then `npm run build:css`. Tailwind reads the JSP files to see which classes are
used and writes **one minified file**, `target/classes/static/css/app.css` (about 7 KB
gzipped). The browser downloads one small file instead of 17 separate ones.

While changing styles, run `npm run watch:css` in a second terminal to rebuild on every save.

**Phones:** the layout uses Tailwind breakpoints (`sm:`, `lg:`) and CSS Grid/Flexbox. Below
1024 px the donors table becomes cards: each cell shows its column name from a `data-label`
attribute.

### JavaScript

`static/js/app.js` is 10 lines. Any form with `data-confirm="..."` asks for confirmation
before submitting (used for Delete and Record donation).

---

## 10. Build: Maven, WAR and the Maven Wrapper

**Maven** reads `pom.xml`, downloads the libraries, compiles, runs the tests and packages the app.

| Dependency | Why |
|---|---|
| `spring-boot-starter-webmvc` | Spring MVC + embedded Tomcat |
| `spring-boot-starter-validation` | Form validation annotations |
| `tomcat-embed-jasper`, `jakarta.servlet.jsp.jstl` | JSP and JSTL support |
| `spring-boot-starter-data-jpa` | Spring Data JPA + Hibernate + HikariCP |
| `spring-boot-starter-flyway`, `flyway-database-postgresql` | Database migrations |
| `postgresql`, `h2` | The two database drivers |
| `spring-boot-h2console` | The H2 web console |
| `spring-boot-devtools` | Auto-restart while developing (left out of the final WAR) |
| `spring-boot-starter-test`, `spring-boot-starter-webmvc-test` | JUnit 5, AssertJ, MockMvc |

**Why a WAR file:** Spring Boot only supports JSP pages when the app is packaged as a
**WAR** (Web Application aRchive). `app.war` still starts on its own with `java -jar`.

**Maven Wrapper:** `mvnw` (Mac/Linux) and `mvnw.cmd` (Windows) download the right Maven version
the first time, so a PC only needs Java.

| Command | What it does |
|---|---|
| `./mvnw spring-boot:run` | Build the CSS and run the app at http://localhost:8080 |
| `./mvnw test` | Run the 27 tests |
| `./mvnw package` | Test and build `target/app.war` |
| `java -jar target/app.war` | Run the built app |

---

## 11. Docker and Vercel

**`Dockerfile`** has two stages:

1. **Build stage** (`eclipse-temurin:21-jdk`): downloads Maven, the libraries, Node.js and
   Tailwind, then runs `./mvnw package` to make `app.war`.
2. **Run stage** (`eclipse-temurin:21-jre`, smaller): copies only `app.war` and starts it with:

   | Setting | Purpose |
   |---|---|
   | `SPRING_PROFILES_ACTIVE=prod`, `PORT=80` | Live settings, and Vercel's default port |
   | `-XX:MaxRAMPercentage=75` | Use up to 75% of the memory Vercel gives |
   | `-XX:TieredStopAtLevel=1`, `-XX:+UseSerialGC` | Start faster on one small CPU |
   | `-Duser.timezone=Asia/Kolkata` | "Today" is the Indian date |

**`vercel.json`**:

```json
{ "framework": "container", "regions": ["sin1"] }
```

- `"framework": "container"`: Vercel builds the `Dockerfile` instead of guessing the framework.
- `"regions": ["sin1"]`: the app runs in Singapore, next to the Neon database. When they were
  far apart, start-up took too long and Vercel stopped the app.

On Vercel's free plan the app **sleeps after about 5 minutes without visitors**. The next
visitor waits a few seconds while Java starts again.

---

## 12. Tests

Run them with `./mvnw test`. All 27 pass.

| Test class | Type | Tests | What it checks |
|---|---|---|---|
| `BloodGroupTest` | Plain unit test | 6 | Compatibility rules (AB+ receives from all, O- gives to all ...) and labels |
| `DonorServiceTest` | Spring Boot + in-memory H2 | 10 | Search filters and order, 90/120-day rule, dashboard counts, blood group stored as label, 10 per page sorted by name, page filter, out-of-range page numbers |
| `DonorControllerTest` | Spring MockMvc | 11 | Pages load, valid form redirects, invalid form shows errors, duplicate mobile rejected, 404 for unknown donors, availability, delete, search, page 2 of the list, `?page=abc` gives 404 |

Each service and controller test runs inside a transaction that is **rolled back** afterwards,
so tests never affect each other. The Flyway migration runs at the start, so the tests also
prove the SQL file and the `Donor` class match.

---

## 13. Where the syllabus topics are used

### Collections

| Where | Collection | Purpose |
|---|---|---|
| `BloodGroup.CAN_RECEIVE_FROM` | `EnumMap<BloodGroup, Set<BloodGroup>>` + `EnumSet` | Compatibility rules |
| `BloodGroup.getCanDonateTo()` | `EnumSet`, `Map.forEach` | Reverse lookup |
| `DonorService.findSuitableDonors()` | `List`, `Set`, Streams, `Comparator` | Filter and sort donors |
| `DonorService.getDashboardStats()` | `EnumMap`, `Map.merge` | Count donors per group |
| `DashboardStats` | `Map<BloodGroup, Long>` | Pass counts to the page |
| Repository methods | `List<Donor>`, `Collection<BloodGroup>`, `Page<Donor>` | Query results and parameters |
| `SampleDataLoader` | `List.of(...)` | Immutable list of sample donors |

### Spring Boot

Auto-configuration, embedded Tomcat, dependency injection through constructors,
`@Controller` / `@Service` / repository layers, Bean Validation, profiles,
`@ControllerAdvice`, `ApplicationRunner`, `@ConditionalOnBooleanProperty`.

### Database connectivity

JDBC drivers for H2 and PostgreSQL, the HikariCP connection pool, JPA/Hibernate mapping
(`@Entity`, `@Column`, `@Convert`), Spring Data JPA queries, `@Transactional`, and Flyway
SQL migrations.

---

## 14. Good practices followed

- **Layered design:** controllers never touch the database directly.
- **Form object separate from the entity:** `DonorForm` vs `Donor` protects fields like `id`.
- **Validation on the server**, plus a database UNIQUE and CHECK constraint as a last guard.
- **Post / Redirect / Get:** refreshing a page never re-submits a form.
- **Changes only through POST:** links can't change data.
- **Safe output:** `<c:out>` and Spring form tags escape user text (XSS protection).
- **Secrets in environment variables**, never in the code.
- **Schema in versioned SQL** (Flyway), checked by Hibernate.
- **No stack traces** shown to visitors.
- **Cache-safe CSS/JS** with content-hash file names.
- **Automatic tests** for the rules, the search and every page.

---

## 15. Viva questions and answers

**Why Spring Boot instead of plain Servlets?**
It sets up Tomcat, Spring MVC, the database connection, validation and JSP with almost no
configuration, so the code is mostly business logic.

**Why is the app a WAR file and not a JAR?**
Spring Boot supports JSP pages only in a WAR. It still runs with `java -jar app.war`.

**What does `@Entity` do?**
It tells JPA/Hibernate that the class is stored in a database table. Each object is one row.

**How does `findByBloodGroupIn` work without any SQL?**
Spring Data JPA reads the method name (`findBy` + `BloodGroup` + `In`) and builds
`SELECT ... WHERE blood_group IN (...)` at run time.

**Where are Collections used?**
The compatibility rules are an `EnumMap` of `EnumSet`s. The search filters and sorts a
`List` with Streams and a `Comparator`. The dashboard counts in an `EnumMap`.

**What is a DTO and why use `DonorForm`?**
A Data Transfer Object carries data between layers. Binding the form to `DonorForm`
instead of the `Donor` entity means a user can only change the fields the form shows.

**What is Flyway for?**
It creates and updates the tables from versioned SQL files, so every database (H2 on the
laptop, PostgreSQL online) ends up with exactly the same structure.

**How does the app use two different databases?**
Through Spring profiles. `local` points to H2 and `prod` points to PostgreSQL. The Java
code is the same for both.

**How is a donor's eligibility worked out?**
`next date = last donation + 90 days (men) or 120 days (women)`. They are eligible if they
never donated or today is on/after that date. "Ready" means eligible and available.

**What is `@Transactional`?**
It runs the method in one database transaction: either every change is saved, or none is.

**What is Post/Redirect/Get?**
After a form POST, the server sends a redirect to a GET page. Refreshing that page
doesn't submit the form again.

**How does pagination work?**
The controller reads `?page=2`, the service builds `PageRequest.of(1, 10, sort by name)`,
and Spring Data JPA adds `LIMIT 10 OFFSET 10` to the SQL plus a `count(*)` query. The result
is a `Page<Donor>` with the 10 donors, the page number and the totals, which `pagination.tag`
uses to draw "Showing 11–20 of 25" and the links.

**How is the site protected from script injection (XSS)?**
User text is printed with `<c:out>` or Spring form tags, which escape HTML characters.
