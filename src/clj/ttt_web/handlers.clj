(ns ttt-web.handlers
  (:require [hiccup.page :as page]
            [hiccup2.core :as hiccup]
            [ring.util.response  :as  response]
            [ttt-web.game :as game]
            [ttt-web.http :as http]
            [ttt-web.view :as view]))

(defn- maybe-invalid-move [n board]
  (when-not (and n (< -1 n (count board)))
    (response/bad-request "Invalid move")))

(defn new-board-response [new-state]
  (-> (http/html-ok (view/board-wrapper new-state))
      (assoc :session new-state)))

(defn move [{:keys [params session]}]
  (let [n (parse-long (:n params))
        {:keys [board]} session
        new-state (delay (game/play session n))]
    (or (maybe-invalid-move n board)
        (new-board-response @new-state))))

(defn home [{:keys [session]}]
  (let [session (if (empty? session) game/new-game session)]
    (-> (http/html-page-ok
          (hiccup/raw "<!DOCTYPE html>")
          [:html
           [:head (page/include-css "/css/app.css")]
           [:body
            [:h1 "Tic-Tac-Toe"]
            (view/board-wrapper session)
            [:script {:src "https://unpkg.com/htmx.org@2.0.4"}]]])
        (assoc :session session))))

(defn restart [_]
  (new-board-response game/new-game))

(defn ai-move [{:keys [session]}]
  (let [new-state (game/ai-move session)]
    (new-board-response new-state)))
