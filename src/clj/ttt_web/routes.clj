(ns ttt-web.routes
  (:require [c3kit.apron.corec :as ccc]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ttt-web.handlers :as handlers]))

(defroutes routes
  (GET "/" [] handlers/home)
  (POST "/move/:n" [] handlers/move)
  (POST "/restart" [] handlers/restart)
  (POST "/ai-move" [] handlers/ai-move)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (-> routes
             wrap-anti-forgery
             wrap-session))
