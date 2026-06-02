FROM clojure:temurin-17-tools-deps AS build

WORKDIR /app
COPY deps.edn .
RUN clojure -P

COPY src ./src

# Runtime más liviano
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /usr/local/lib/clojure /usr/local/lib/clojure
COPY --from=build /app /app

EXPOSE 3000

CMD ["clojure", "-M", "-m", "app.core"]