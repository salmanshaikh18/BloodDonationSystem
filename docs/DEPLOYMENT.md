# Deployment Guide

Step-by-step instructions to run the Blood Donation Management System on your own computer
and put your own copy online, **for free**, exactly the way the original was deployed.

**What you'll end up with:** a live website like `https://your-project.vercel.app`,
running on **Vercel** (free Hobby plan) with a free **Neon PostgreSQL** database.

**Cost:** ₹0. No credit card is needed anywhere. Don't click "Upgrade" and don't add a card.

**Time:** about 30 minutes the first time.

## Contents

- [Part A: Run it on your computer](#part-a-run-it-on-your-computer) (optional, but recommended)
- [Part B: Put your own copy on GitHub](#part-b-put-your-own-copy-on-github)
- [Part C: Create the project on Vercel](#part-c-create-the-project-on-vercel)
- [Part D: Add the free Neon database](#part-d-add-the-free-neon-database)
- [Part E: Redeploy and open your site](#part-e-redeploy-and-open-your-site)
- [Part F: Updating the site later](#part-f-updating-the-site-later)
- [Troubleshooting](#troubleshooting)
- [Good to know](#good-to-know)

---

## Part A: Run it on your computer

### A1. Install Java 17 or newer

You only need Java. Maven, Node.js and Tailwind are downloaded automatically.

**Windows**

1. Download **Temurin 21 (LTS)** from https://adoptium.net and run the installer.
2. On the "Custom Setup" screen, turn on **Set JAVA_HOME variable** and **Add to PATH**.
3. Open a new Command Prompt and check:
   ```bat
   java -version
   ```

**Mac**

Either install Temurin 21 from https://adoptium.net (simplest), or use Homebrew:

```bash
brew install openjdk@21
```

```bash
echo 'export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home' >> ~/.zshrc && source ~/.zshrc
```

Check with `java -version` or `$JAVA_HOME/bin/java -version`.

### A2. Get the code

With Git:

```bash
git clone https://github.com/salmanshaikh18/BloodDonationSystem.git
```

```bash
cd BloodDonationSystem
```

Or on the GitHub page click **Code → Download ZIP** and unzip it.

> If you will deploy your own copy, fork it first (Part B) and clone **your fork** instead.

### A3. Start the app

Mac / Linux:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

- The **first run takes a few minutes** while Maven, the libraries and Node.js download.
- When you see `Started BloodDonationApplication`, open **http://localhost:8080**.
- On your computer the app uses an **H2 database** saved in the `data` folder, filled with
  12 made-up donors. Delete the `data` folder to start fresh.
- Stop the app with **Ctrl + C**.

### A4. Run the tests (optional)

```bash
./mvnw test
```

You should see `Tests run: 27, Failures: 0, Errors: 0`.

---

## Part B: Put your own copy on GitHub

Vercel deploys from a GitHub repository, so you need your own copy.

1. Create a free account at https://github.com if you don't have one.
2. Open https://github.com/salmanshaikh18/BloodDonationSystem
3. Click **Fork** (top right), then **Create fork**.

You now have `https://github.com/YOUR-USERNAME/BloodDonationSystem`.

<details>
<summary>Other way: push the code to a new empty repository</summary>

1. On GitHub click **New repository**, give it a name, and **don't** add a README.
2. In the project folder run (replace the URL with yours):

```bash
git init -b main
```

```bash
git add . && git commit -m "Blood Donation Management System"
```

```bash
git remote add origin https://github.com/YOUR-USERNAME/BloodDonationSystem.git
```

```bash
git push -u origin main
```

</details>

---

## Part C: Create the project on Vercel

### C1. Sign up

Go to https://vercel.com/signup, choose **Continue with GitHub**, and pick the **Hobby**
(free) plan. No card is needed.

### C2. Import the repository

1. Go to https://vercel.com/new
2. Under **Import Git Repository**, click **GitHub**. If asked, install the Vercel GitHub app:
   choose **Only select repositories → BloodDonationSystem → Install**.
3. Click **Import** next to **BloodDonationSystem**.

### C3. Check the settings

| Setting | What you should see |
|---|---|
| Vercel Team | Your name, **Hobby** |
| Project Name | Anything, e.g. `blood-donation-system`. This becomes part of the web address |
| Root Directory | `./` |
| Application Preset | **Container**, with the note **"Set by vercel.json"**. Don't change it |

**Optional:** to have the 12 made-up donors on the live site, open
**Environment Variables** and add:

| Key | Value |
|---|---|
| `APP_SAMPLE_DATA` | `true` |

Without it the live site starts with no donors.

### C4. Deploy

Click **Deploy**.

- The build takes **3–6 minutes** (Maven, the libraries and Node.js download the first time).
- **The first deployment won't work yet.** There is no database, so the app stops with this
  in the logs. That is expected:
  ```
  Caused by: java.net.UnknownHostException: ${PGHOST}
  ```

---

## Part D: Add the free Neon database

### D1. Create the database

1. Open your project on Vercel, then the **Storage** tab.
2. Click **Create Database**, choose **Neon** (Serverless Postgres), click **Continue**.
3. If asked, choose **Create New Neon Account** (linked to your Vercel login, no new password).
4. Fill in:

| Setting | Choose |
|---|---|
| Region | **AWS Asia Pacific (Singapore)** - must be Singapore, see the note below |
| Plan | **Free** |
| Extra options (Auth, Realtime) | Leave off |
| Database name | e.g. `blood-donation-db` |

Accept Neon's terms and click **Create**.

> **Why Singapore?** `vercel.json` runs the app in Singapore (`"regions": ["sin1"]`). The app
> and database must be close together. When they were in different countries, start-up took
> too long and Vercel stopped the app. If you pick another region for the database, change
> `sin1` in `vercel.json` to the matching Vercel region.

### D2. Connect it to your project

The **Connect a Project** screen appears (or open **Storage → your database → Connect Project**):

| Setting | Choose |
|---|---|
| Project | Your project (`blood-donation-system`) |
| Environments | **Production** and **Preview** (Development is optional) |
| Create database branch for deployment | Leave **both unticked** |
| **Custom Prefix** | **Leave it empty.** Only grey example text like `STORAGE` should show |
| Sensitive | Leave on |

Click **Connect**.

> **The prefix must be empty.** The app looks for variables named exactly `PGHOST`,
> `PGUSER`, `PGPASSWORD` and `PGDATABASE`. A prefix would rename them (`STORAGE_PGHOST`)
> and the app couldn't find the database.

### D3. Check the variables

Open **Settings → Environment Variables**. You should see `PGHOST`, `PGHOST_UNPOOLED`,
`PGUSER`, `PGPASSWORD`, `PGDATABASE`, `DATABASE_URL` and a few more. With *Sensitive* on, the
values are hidden; only the names matter.

---

## Part E: Redeploy and open your site

### E1. Redeploy

Environment variables only reach **new** deployments, so the first one will never work.

1. Open the **Deployments** tab.
2. On the top deployment click **⋯ → Redeploy**, then **Redeploy** again.
3. Wait until it says **Ready** (about 2–5 minutes).

(Pushing any commit to GitHub also starts a new deployment.)

### E2. Open the site

On the project's **Overview** page, click the address under **Domains**, for example
`https://blood-donation-system-xyz.vercel.app`.

- The first visit after a deployment, or after the site has been asleep, can take
  **10–20 seconds** while Java starts. Wait rather than refreshing.
- Open each page once: the first view of each page is a little slower (about 2 seconds)
  while its JSP is compiled.

On the first start the app creates the `donors` table in Neon automatically (Flyway). If you
set `APP_SAMPLE_DATA=true`, you'll see 12 donors on the home page.

### E3. (Optional) Choose a nicer address

**Settings → Domains → Add**, type a free address such as
`yourname-blood-donation.vercel.app`. If nobody else has taken it, it's yours for free.

---

## Part F: Updating the site later

Every `git push` to the `main` branch builds and deploys the new version automatically:

```bash
git add .
```

```bash
git commit -m "Describe the change"
```

```bash
git push
```

Watch progress in the **Deployments** tab. If a build fails, the site keeps showing the
previous version.

---

## Troubleshooting

Find problems in **Project → Logs**. Use the search box (for example `Caused by` or `ERROR`)
instead of reading every line.

| What you see | Why | Fix |
|---|---|---|
| `UnknownHostException: ${PGHOST}` | The database isn't connected, or you're looking at a deployment made **before** connecting it | Do Part D, then **Redeploy** (Part E). Open the address under **Domains**, not an old deployment's address |
| `Error: could not connect to $PORT=80 ... startup timeout` | The app took longer than about 28 seconds to start | Check `vercel.json` has `"regions": ["sin1"]` and the Neon database is in **Singapore**. Then redeploy |
| Variables are named `STORAGE_PGHOST` (or other prefix) | A Custom Prefix was set when connecting | **Storage → database → Projects**: disconnect the project, then connect it again with an **empty** prefix. Redeploy |
| `password authentication failed` | Old or wrong database login | Disconnect and reconnect the database in **Storage**, then redeploy |
| Vercel's own **500: INTERNAL_SERVER_ERROR** page | The app crashed or hasn't started | Read **Logs** for the `Caused by` line and match it in this table |
| Application Preset isn't **Container** | `vercel.json` is missing from the repository root | Make sure `vercel.json` is committed in the top folder |
| Build fails in the Maven or npm step | A download failed, or the code doesn't compile | Read the build log. Try **Redeploy**. Run `./mvnw package` on your computer to check |
| `./mvnw: Permission denied` (Mac/Linux) | The file lost its "executable" flag | Run `chmod +x mvnw` |
| `JAVA_HOME is not set` / `Unable to locate a Java Runtime` | Java isn't installed or not found | Install Java 17+ (Part A1) and open a new terminal |
| Port 8080 already in use (on your computer) | Another program uses 8080 | Stop it, or start with `PORT=8081 ./mvnw spring-boot:run` |

---

## Good to know

- **The site sleeps.** After about 5 minutes without visitors, Vercel stops the app. The
  next visitor waits while it starts again. **Before a demo, open the site once** and click
  through the pages.
- **Free limits.** Vercel Hobby and Neon Free have monthly limits far above what a college
  project uses. If a limit is ever reached, the site pauses; nothing is charged.
- **The site is public.** Anyone with the address can add, edit or delete donors (there is
  no login). Don't enter real people's phone numbers.
- **Vercel's container hosting is in Beta.** If it ever stops working, the same `Dockerfile`
  runs on Render's free plan, with the same Neon database settings.
- **How the database is reached:** `application-prod.properties` builds the connection from
  the variables Vercel adds:
  ```properties
  spring.datasource.url=jdbc:postgresql://${PGHOST_UNPOOLED:${PGHOST}}/${PGDATABASE}?sslmode=require
  spring.datasource.username=${PGUSER}
  spring.datasource.password=${PGPASSWORD}
  ```
