(ns forecaster.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

(defn simple-body-page [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World"})

(defn request-example [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (->>
             (pp/pprint req)
             (str "Request Object: " req))})

(defn hello-name
  [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (->
          (pp/pprint req)
          (str "Hello " (:name (:params req))))})

(def people-collection (atom []))

(defn addperson [firstname surname]
  (swap! people-collection conj {:firstname (str/capitalize firstname)
                                 :surname (str/capitalize surname)}))

(addperson "Functional" "Human")
(addperson "Micky" "Mouse")

(defn people-handler [req]
  {:status 200
   :headers {"Content-Type" "text/json"}
   :body (str (json/write-str @people-collection))})

(defn getparameter [req pname] (get (:params req) pname))

(defn addperson-handler [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (-> (let [p (partial getparameter req)]
                  (str (json/write-str (addperson (p :firstname) (p :surname))))))})

(defroutes app-routes
  (GET "/" [] simple-body-page)
  (GET "/request" [] request-example)
  (GET "/hello" [] hello-name)
  (GET "/people" [] people-handler)
  (POST "/people/add" [] addperson-handler)
  (route/not-found "Error, page not found!"))

(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
                                        ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
                                        ; Run the server without ring defaults
                                        ;(server/run-server #'app-routes {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
