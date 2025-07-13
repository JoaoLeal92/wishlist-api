(ns wishlist.handler
  (:require [clojure.pprint]
            [cheshire.core :as json]
            [wishlist.db :as db]))

(defn ping
  [request]
  (clojure.pprint/pprint request)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn create-wishlist
  [request]
  (let [raw-body (slurp (:body request))
        payload (json/parse-string raw-body true)
        name (:name payload)]
    (try
      (let [wishlist (db/add-wishlist name)]
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (json/generate-string wishlist)})
      (catch Exception e
        {:status 200
         :headers {"Content-Type" "text/html"}
         :body (str  "Error creating wishlist: " e)}))))

(defn get-wishlist-by-id
  [{:keys [path-params]}]
  (let [id (:id path-params)]
    (try
      (let [wishlist (db/get-wishlist-by-id id)]
        (if wishlist
          {:status 200
           :headers {"Content-Type" "text/html"}
           :body (json/generate-string wishlist)}
          {:status 404
           :headers {"Content-Type" "text/html"}
           :body "Wishlist not found"}))
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (str  "Error retrieving wishlist: " e)}))))

(defn update-wishlist
  [{:keys [body path-params]}]
  (let [id (:id path-params)
        raw-body (slurp body)
        payload (json/parse-string raw-body true)
        name (:name payload)]
    (try
      (db/update-wishlist id name)
      {:status 204
       :headers {"Content-Type" "text/html"}}
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (str  "Error retrieving wishlist: " e)}))))

(defn delete-wishlist
  [{:keys [path-params]}]
  (let [id (:id path-params)]
    (try
      (db/delete-wishlist-by-id id)
      {:status 204}
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (str  "Error retrieving wishlist: " e)}))))

(defn list-wishlists
  [_]
  (try
    (let [wishlists (db/list-wishlists)]
      {:status 200
       :body (json/generate-string wishlists true)})
    (catch Exception e
      {:status 500
       :body (str "Error listing wishlists: ", e)})))

(defn add-item-to-wishlist
  [{:keys [body]}]
  (let [raw-body (slurp body)
        payload (json/parse-string raw-body true)]
    (try
      (let [item (db/add-item-to-wishlist {:name (:name payload) :target-price (:target-price payload) :wishlist-id (:wishlist-id payload)})]
        {:status 201
         :body (json/generate-string item)})
      (catch Exception e
        {:status 500
         :body (str "Error adding item to wishlist: ", e)}))))

(defn get-items-on-wishlist
  [{:keys [query-params]}]
  (let [wishlist-id (get query-params "wishlist_id")]
    (if-not wishlist-id
      {:status 403
       :body "Must inform wishlist_id"}
      (try
        (let [wishlist (db/get-items-on-wishlist wishlist-id)]
          {:status 200
           :body (json/generate-string wishlist true)})
        (catch Exception e
          {:status 500
           :body (str "Error getting items from wishlist: ", e)})))))

(defn delete-item-from-wishlist
  [{:keys [path-params]}]
  (let [item-id (:item-id path-params)]
    (try
      (db/delete-item-by-id item-id)
      {:status 201}
      (catch Exception e
        {:status 500
         :body (str "Error deleting item: ", e)}))))

(defn update-item
  [{:keys [body path-params]}]
  (let [raw-body (slurp body)
        item (json/parse-string raw-body true)
        item-id (:item-id path-params)]
    (try
      (db/update-item item-id {:name (:name item) :target-price (:target-price item) :price-paid (:price-paid item) :bought (:bought item)})
      {:status 200
       :body "Item updated succesfully"}
      (catch Exception e
        {:status 500
         :body (str "Error updating item: ", e)}))))
