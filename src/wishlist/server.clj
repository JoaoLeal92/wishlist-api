(ns wishlist.server
  (:require [ring.adapter.jetty :as jetty]
            [wishlist.handler :as handler]
            [clojure.pprint]
            [reitit.ring :as ring]
            [wishlist.db :as db]
            [ring.middleware.params :refer [wrap-params]])
  (:gen-class))

(defonce server (atom nil))

(def app
  (wrap-params
   (ring/ring-handler
    (ring/router
     [["/ping" {:get handler/ping}]
      ["/wishlist" {:post handler/create-wishlist
                    :get handler/list-wishlists}]
      ["/wishlist/:id" {:get handler/get-wishlist-by-id
                        :delete handler/delete-wishlist
                        :put handler/update-wishlist}]
      ["/items" {:post handler/add-item-to-wishlist
                 :get handler/get-items-on-wishlist}]
      ["/items/:item-id" {:delete handler/delete-item-from-wishlist
                          :put handler/update-item}]])
    (ring/create-default-handler))))

(defn -main
  [& args]
  (if (db/db-exists?)
    (println "Db already created, starting server...")
    (do
      (println "initializing database...")
      (db/init-db)
      (println "database created!")))
  (jetty/run-jetty app
                   {:port 3000
                    :join? true}))

(defn start-server []
  (if (db/db-exists?)
    (println "Db already created, starting server...")
    (do
      (println "initializing database...")
      (db/init-db)
      (println "database created!")))
  (reset! server
          (jetty/run-jetty app {:port 3000 :join? false})))

(defn stop-server []
  (when @server
    (.stop @server)
    (reset! server nil)))
