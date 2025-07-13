(ns wishlist.wishlist-app-test
  (:require [clojure.test :refer :all]
            [wishlist.db :as db]
            [next.jdbc :as jdbc]
            [clojure.java.io :as io]))

(def test-db-spec {:dbtype "sqlite" :dbname "test-database.db"})

(defn setup-test-db []
  (jdbc/execute! (jdbc/get-datasource test-db-spec)
                 ["CREATE TABLE IF NOT EXISTS wishlists (
                       id INTEGER PRIMARY KEY,
                       name TEXT NOT NULL
                     );"])
  (jdbc/execute! (jdbc/get-datasource test-db-spec)
                 ["CREATE TABLE IF NOT EXISTS items (
                       id INTEGER PRIMARY KEY,
                       name TEXT NOT NULL,
                       wishlist_id INTEGER NOT NULL,
                       target_price INTEGER,
                       bought BOOLEAN DEFAULT 0,
                       price_paid INTEGER,
                       FOREIGN KEY (wishlist_id) REFERENCES wishlists(id)
                     );"]))

(defn teardown-test-db []
  (let [file (io/file "test-database.db")]
    (when (.exists file)
      (.delete file))))

(defn test-db-fixture [f]
  (with-redefs [db/ds (jdbc/get-datasource test-db-spec)]
    (setup-test-db)
    (f)
    (teardown-test-db)))

(use-fixtures :each test-db-fixture)

(deftest add-wishlist-test
  (testing "Adds a wishlist to the database"
    (let [wishlist (db/add-wishlist "test-wishlist")]
      (is (= {:id 1, :name "test-wishlist"} (first wishlist))))))

(deftest get-wishlist-by-id-test
  (testing "Gets a wishlist by its ID"
    (db/add-wishlist "test-wishlist")
    (let [wishlist (db/get-wishlist-by-id 1)]
      (is (= {:id 1, :name "test-wishlist"} wishlist)))))

(deftest delete-wishlist-by-id-test
  (testing "Deletes a wishlist by its ID"
    (db/add-wishlist "test-wishlist")
    (db/delete-wishlist-by-id 1)
    (let [wishlist (db/get-wishlist-by-id 1)]
      (is (nil? wishlist)))))

(deftest update-wishlist-test
  (testing "Updates a wishlist's name"
    (db/add-wishlist "test-wishlist")
    (db/update-wishlist 1 "new-name")
    (let [wishlist (db/get-wishlist-by-id 1)]
      (is (= {:id 1, :name "new-name"} wishlist)))))

(deftest list-wishlists-test
  (testing "Lists all wishlists"
    (db/add-wishlist "wishlist-1")
    (db/add-wishlist "wishlist-2")
    (let [wishlists (db/list-wishlists)]
      (is (= 2 (count wishlists)))
      (is (= #{{:id 1, :name "wishlist-1"} {:id 2, :name "wishlist-2"}} (set wishlists))))))

(deftest add-item-to-wishlist-test
  (testing "Adds an item to a wishlist"
    (db/add-wishlist "test-wishlist")
    (let [item (db/add-item-to-wishlist {:name "test-item" :target-price 100 :wishlist-id 1})]
      (is (= {:id 1, :name "test-item", :wishlist_id 1, :target_price 100, :bought 0, :price_paid nil} item)))))

(deftest get-items-on-wishlist-test
  (testing "Gets all items on a wishlist"
    (db/add-wishlist "test-wishlist")
    (db/add-item-to-wishlist {:name "item-1" :target-price 100 :wishlist-id 1})
    (db/add-item-to-wishlist {:name "item-2" :target-price 200 :wishlist-id 1})
    (let [items (db/get-items-on-wishlist 1)]
      (is (= 2 (count items)))
      (is (= #{{:id 1, :name "item-1", :wishlist_id 1, :target_price 100, :bought 0, :price_paid nil}
               {:id 2, :name "item-2", :wishlist_id 1, :target_price 200, :bought 0, :price_paid nil}}
             (set items))))))

(deftest delete-item-by-id-test
  (testing "Deletes an item by its ID"
    (db/add-wishlist "test-wishlist")
    (db/add-item-to-wishlist {:name "test-item" :target-price 100 :wishlist-id 1})
    (db/delete-item-by-id 1)
    (let [items (db/get-items-on-wishlist 1)]
      (is (empty? items)))))

(deftest update-item-test
  (testing "Updates an item's attributes"
    (db/add-wishlist "test-wishlist")
    (db/add-item-to-wishlist {:name "test-item" :target-price 100 :wishlist-id 1})
    (db/update-item 1 {:name "new-item-name" :target-price 150 :bought true :price-paid 140})
    (let [item (first (db/get-items-on-wishlist 1))]
      (is (= {:id 1, :name "new-item-name", :wishlist_id 1, :target_price 150, :bought 1, :price_paid 140}
             item)))))
