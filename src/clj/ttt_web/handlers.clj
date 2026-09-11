(ns ttt-web.handlers
  (:require [clojure.data.json :as json]
            [hiccup.page :as page]
            [hiccup2.core :as hiccup]
            [ring.middleware.anti-forgery :as af]
            [ring.util.response :as response]
            [tic-tac-toe.board-options :as board-options]
            [ttt-web.game :as game]
            [ttt-web.http :as http]
            [ttt-web.view :as view]))

;region Responses

(defn- body-attrs []
  {:hx-headers (json/write-str {"X-CSRF-Token" af/*anti-forgery-token*} :escape-slash false)
   :hx-history "false"})

(defn page [content]
  (http/html-page-ok
    (hiccup/raw "<!DOCTYPE html>")
    [:html
     [:head (page/include-css "/css/app.css")]
     [:body (body-attrs)
      [:h1 "Tic-Tac-Toe"]
      content
      [:script {:src "https://unpkg.com/htmx.org@2.0.4"}]]]))

(defn- push-url [response url]
  (response/header response "HX-Push-Url" url))

(defn new-board-response [new-state]
  (-> (http/html-ok (view/board-wrapper new-state))
      (assoc :session new-state)))

(defn- start-game-response [session]
  (-> (new-board-response session)
      (push-url "/play")))

(defn- board-selection-response [session]
  (-> (http/html-ok (view/board-selection))
      (assoc :session session)
      (push-url "/board")))

(defn- level-selection-response [session]
  (-> (http/html-ok (view/level-selection session))
      (assoc :session session)
      (push-url "/level")))

;endregion

;region Screens

(def ^:private screen-paths
  {:mode "/mode" :board "/board" :level "/level" :play "/play"})

(defn- htmx-request? [request]
  (= "true" (get-in request [:headers "hx-request"])))

(defn- screen [request content]
  (if (htmx-request? request)
    (http/html-ok content)
    (page content)))

(defn- maybe-wrong-screen
  "Redirects unless the session's choices entitle it to be on `this`."
  [session this]
  (let [belongs (game/current-screen session)]
    (when-not (= this belongs)
      (response/redirect (screen-paths belongs)))))

(defn home [{:keys [session]}]
  (response/redirect (screen-paths (game/current-screen session))))

(defn mode-screen [{:keys [session] :as request}]
  (-> (screen request (view/mode-selection))
      (assoc :session (apply dissoc session game/state-keys))))

(defn board-screen [{:keys [session] :as request}]
  (or (maybe-wrong-screen session :board)
      (screen request (view/board-selection))))

(defn level-screen [{:keys [session] :as request}]
  (or (maybe-wrong-screen session :level)
      (screen request (view/level-selection session))))

(defn play-screen [{:keys [session] :as request}]
  (or (maybe-wrong-screen session :play)
      (screen request (view/board-wrapper session))))

;endregion

;region Selections

(defn select-mode [{:keys [params session]}]
  (board-selection-response (assoc session :mode (parse-long (:n params)))))

(defn- ->board [n]
  (if (= 2 n) board-options/initial-4x4-board board-options/initial-3x3-board))

(defn select-board [{:keys [session params]}]
  (or (maybe-wrong-screen session :board)
      (let [board (->board (parse-long (:n params)))
            {:keys [mode]} session]
        (if (= 1 mode)
          (start-game-response (merge session (game/new-game board mode)))
          (level-selection-response (assoc session :board board))))))

(defn- record-level [session level]
  (assoc session (game/next-level-key session) level))

(defn select-level [{:keys [params session]}]
  (or (maybe-wrong-screen session :level)
      (let [chosen (record-level session (parse-long (:n params)))
            new-state (merge chosen (game/new-game chosen))
            {:keys [mode second-ai-level]} new-state]
        (if (and (= 4 mode) (nil? second-ai-level))
          (level-selection-response new-state)
          (start-game-response new-state)))))

;endregion

;region Play

(defn- maybe-invalid-move [n board]
  (when-not (and n (< -1 n (count board)))
    (response/bad-request "Invalid move")))

(defn move [{:keys [params session]}]
  (let [n (parse-long (:n params))
        {:keys [board]} session
        new-state (delay (game/play session n))]
    (or (maybe-invalid-move n board)
        (new-board-response @new-state))))

(defn restart [{:keys [session]}]
  (new-board-response (merge session (game/new-game session))))

(defn- maybe-not-ai-turn [session]
  (when-not (game/ai-turn? session)
    (response/bad-request "It's the human's turn!")))

(defn ai-move [{:keys [session]}]
  (let [new-state (delay (game/ai-move session))]
    (or (maybe-not-ai-turn session)
        (new-board-response @new-state))))

;endregion
