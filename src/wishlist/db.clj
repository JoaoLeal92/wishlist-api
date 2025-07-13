(ns wishlist.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [clojure.string]
            [clojure.java.io :as io]))

(def db-spec {:dbtype "sqlite" :dbname "database.db"})

(def ds (jdbc/get-datasource db-spec))

(defn init-db []
  (doseq [statement ["CREATE TABLE IF NOT EXISTS wishlists (
                       id INTEGER PRIMARY KEY,
                       name TEXT NOT NULL
                     );"
                     "CREATE TABLE IF NOT EXISTS items (
                       id INTEGER PRIMARY KEY,
                       name TEXT NOT NULL,
                       wishlist_id INTEGER NOT NULL,
                       target_price INTEGER,
                       bought BOOLEAN DEFAULT 0,
                       price_paid INTEGER,
                       FOREIGN KEY (wishlist_id) REFERENCES wishlists(id)
                     );"]]
    (jdbc/execute! ds [statement])))

(defn db-exists? []
  (let [file (io/file "database.db")]
    (.exists file)))

(defn add-wishlist [name]
  (jdbc/execute! ds
                 ["INSERT INTO wishlists (name) VALUES (?) RETURNING *" name]
                 {:builder-fn rs/as-unqualified-lower-maps}))

(defn get-wishlist-by-id [id]
  (jdbc/execute-one! ds
                     ["SELECT * FROM wishlists WHERE id = (?)", id]
                     {:builder-fn rs/as-unqualified-lower-maps}))

(defn delete-wishlist-by-id [id]
  (jdbc/execute-one! ds ["DELETE FROM wishlists WHERE id = (?)", id]))

(defn update-wishlist [id name]
  (jdbc/execute-one! ds ["UPDATE wishlists SET name = (?) WHERE id = (?)" name id]))

(defn list-wishlists []
  (jdbc/execute! ds 
                 ["SELECT * FROM wishlists"]
                 {:builder-fn rs/as-unqualified-lower-maps}))

(defn add-item-to-wishlist [{:keys [name target-price wishlist-id]}]
  (jdbc/execute-one! ds
                     ["INSERT INTO items (name, target_price, wishlist_id) VALUES (?, ?, ?) RETURNING *", name, target-price, wishlist-id]
                     {:builder-fn rs/as-unqualified-lower-maps}))

(defn get-items-on-wishlist [wishlist-id]
  (jdbc/execute! ds 
                     ["SELECT * FROM items WHERE wishlist_id = (?)", wishlist-id]
                     {:builder-fn rs/as-unqualified-lower-maps}))

(defn delete-item-by-id [item-id]
  (jdbc/execute-one! ds ["DELETE FROM items WHERE id = (?)" item-id]))

(defn update-item [id {:keys [name target-price bought price-paid]}]
  (let [[fields params] (cond-> [[] []]
                          name (-> (update 0 conj "name = ?")
                                   (update 1 conj name))
                          target-price (-> (update 0 conj "target_price = ?")
                                           (update 1 conj target-price))
                          bought (-> (update 0 conj "bought = ?")
                                     (update 1 conj bought))
                          price-paid (-> (update 0 conj "price_paid = ?")
                                         (update 1 conj price-paid)))
        sql (str "UPDATE items SET " (clojure.string/join ", " fields) " WHERE id = ?")
        final-params (conj params id)]
    (jdbc/execute-one! ds (into [sql] final-params))))
