(ns ttt-web.routes
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :as route]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.session :refer [wrap-session]]
            [ttt-web.handlers :as handlers]))

(defroutes routes
  (GET "/" [] handlers/home)
  (GET "/mode" [] handlers/mode-screen)
  (GET "/board" [] handlers/board-screen)
  (GET "/level" [] handlers/level-screen)
  (GET "/play" [] handlers/play-screen)
  (POST "/mode/:n" [] handlers/select-mode)
  (POST "/board/:n" [] handlers/select-board)
  (POST "/level/:n" [] handlers/select-level)
  (POST "/move/:n" [] handlers/move)
  (POST "/restart" [] handlers/restart)
  (POST "/ai-move" [] handlers/ai-move)
  (route/resources "/")
  (route/not-found "Not Found"))

(def app (-> routes
             wrap-anti-forgery
             wrap-session))
