# Use an official Clojure image as a parent image
FROM clojure:temurin-17-tools-deps

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the project file
COPY deps.edn .

# Download dependencies
RUN clojure -P

# Copy the rest of the application code
COPY . .

# Expose the port the app runs on
EXPOSE 3000

# Specify the command to run on container start
CMD ["clojure", "-M:run-m"]
