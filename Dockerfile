# Builds and runs the Blood Donation Management System.
# Vercel uses this file to put the site online.
#
# Try it on your computer (needs a PostgreSQL database, see README.md):
#   docker build -t blood-donation .
#   docker run -p 8080:80 -e PGHOST=... -e PGDATABASE=... -e PGUSER=... -e PGPASSWORD=... blood-donation

# ---------- Stage 1: build app.war with Maven (this also builds the CSS) ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Download Maven, the Java libraries, Node.js and Tailwind first.
# Docker keeps this step cached, so rebuilding after a code change is quick.
COPY mvnw pom.xml package.json package-lock.json ./
COPY .mvn .mvn
RUN chmod +x mvnw \
    && ./mvnw -B -q dependency:go-offline \
    && ./mvnw -B -q frontend:install-node-and-npm@install-node-and-npm frontend:npm@npm-ci

# Now copy the source code and build target/app.war (tests run on your computer, not here)
COPY src src
RUN ./mvnw -B -q -DskipTests package

# ---------- Stage 2: a smaller image that only runs the app ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/app.war app.war

# Live-site settings: use application-prod.properties, and Vercel's default port 80
ENV SPRING_PROFILES_ACTIVE=prod \
    PORT=80
EXPOSE 80

# Java settings:
#   MaxRAMPercentage=75   use up to 75% of the memory Vercel gives the app
#   TieredStopAtLevel=1   start faster (best for a small app that sleeps when idle)
#   UseSerialGC           the simplest garbage collector, ideal for one small CPU
#   user.timezone         Indian time, so "today" is correct
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "-Duser.timezone=Asia/Kolkata", "-jar", "app.war"]
