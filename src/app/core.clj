(ns app.core
  (:require
   [compojure.core :refer :all]
   [compojure.route :as route]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [cheshire.core :as json]))

;; "Base de datos" en memoria
(defonce db (atom {}))
(defonce id-counter (atom 0))

;; CREATE
(defn create-item [item]
  (let [id (swap! id-counter inc)
        new-item (assoc item :id id)]
    (swap! db assoc id new-item)
    new-item))

;; READ ALL
(defn get-items []
  (vec (vals @db)))

;; READ ONE
(defn get-item [id]
  (get @db id))

;; UPDATE
(defn update-item [id data]
  (if-let [existing (get @db id)]
    (let [updated (merge existing data)]
      (swap! db assoc id updated)
      updated)
    nil))

;; DELETE
(defn delete-item [id]
  (swap! db dissoc id))

;; RUTAS
(defroutes app-routes
  (POST "/items" req
    {:status 201
     :body (create-item (:body req))})

  (GET "/items" []
    {:status 200
     :body (get-items)})

  (GET "/items/:id" [id]
    (if-let [item (get-item (Integer/parseInt id))]
      {:status 200 :body item}
      {:status 404 :body {:error "Not found"}}))

  (PUT "/items/:id" req
    (let [id (Integer/parseInt (get-in req [:params :id]))]
      (if-let [updated (update-item id (:body req))]
        {:status 200 :body updated}
        {:status 404 :body {:error "Not found"}})))

  (DELETE "/items/:id" [id]
    (delete-item (Integer/parseInt id))
    {:status 204})

  (route/not-found {:status 404 :body {:error "Route not found"}}))

;; APP
(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-json-response))

;; MAIN
(defn -main []
  (run-jetty app {:port 3000 :join? false}))