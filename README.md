# Wishlist API

This is a simple API for creating and managing wishlists and the items on them. It is built with Clojure and uses a SQLite database for storage.

## Dependencies

This project uses the following main dependencies:

- `org.clojure/clojure`: The Clojure language
- `ring/ring-jetty-adapter`: For the web server
- `metosin/reitit-ring`: For routing
- `seancorfield/next.jdbc`: For database interactions
- `org.xerial/sqlite-jdbc`: The SQLite database driver
- `cheshire/cheshire`: For JSON parsing

## Running the Application

There are two ways to run this application: locally using the Clojure CLI or with Docker.

### Running Locally

**Prerequisites:**
- [Clojure CLI](https://clojure.org/guides/getting_started) installed.
- Java 17+ installed.

1.  **Start the server:**
    ```bash
    clojure -M:run-m
    ```
    The server will start on `http://localhost:3000`.

2.  **Run the tests:**
    ```bash
    clojure -X:test:runner
    ```

### Running with Docker

**Prerequisites:**
- [Docker](https://docs.docker.com/get-docker/) installed and running.

1.  **Build the Docker image:**
    ```bash
    docker build -t wishlist-app .
    ```

2.  **Run the Docker container:**
    ```bash
    docker run -p 3000:3000 wishlist-app
    ```
    The application will be accessible at `http://localhost:3000`.

## API Routes

### Wishlists

- `POST /wishlist`
  - Creates a new wishlist.
  - **Body:** `{"name": "My Wishlist"}`
  - **Example:**
    ```bash
    curl -X POST http://localhost:3000/wishlist \
    -H "Content-Type: application/json" \
    -d '{"name": "Holiday Gifts"}'
    ```

- `GET /wishlist`
  - Returns a list of all wishlists.

- `GET /wishlist/:id`
  - Returns a specific wishlist by its ID.

- `PUT /wishlist/:id`
  - Updates a wishlist's name.
  - **Body:** `{"name": "New Wishlist Name"}`

- `DELETE /wishlist/:id`
  - Deletes a wishlist by its ID.

### Items

- `POST /items`
  - Adds a new item to a wishlist.
  - **Body:** `{"name": "My Item", "target_price": 100, "wishlist_id": 1}`
  - **Example:**
    ```bash
    curl -X POST http://localhost:3000/items \
    -H "Content-Type: application/json" \
    -d '{"name": "A new book", "target_price": 25, "wishlist_id": 1}'
    ```

- `GET /items`
  - Returns a list of all items on a specific wishlist.
  - **Query Parameter:** `wishlist_id`
  - **Example:** `GET /items?wishlist_id=1`

- `PUT /items/:item-id`
  - Updates an item's details.
  - **Body:** `{"name": "New Name", "target_price": 150, "bought": true, "price_paid": 145}`

- `DELETE /items/:item-id`
  - Deletes an item by its ID.