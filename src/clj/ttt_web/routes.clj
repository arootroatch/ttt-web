(ns ttt-web.routes
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [ring.middleware.session :refer [wrap-session]]
            [ttt-web.handlers :as handlers]))

(defroutes routes
  (GET "/" [] handlers/home)
  (POST "/move/:n" [] handlers/move)
  (POST "/restart" [] handlers/restart)
  (POST "/ai-move" [] handlers/ai-move)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (-> routes wrap-session))
