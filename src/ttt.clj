(ns ttt
  (:require [clojure.string :as string]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [render-html :refer :all]
            [html.db-selection]
            [html.board-selection]
            [html.resume-selection]
            [html.mode-selection])
  (:import MyServer.Route))

(def initial-state {:current-screen  :db-selection
                    :mode            nil
                    :board           nil
                    :first-ai-level  nil
                    :second-ai-level nil
                    :player          :x
                    :human?          nil
                    :game-state      :in-progress
                    :ui              :web})

(defn- write-output [outputStream state]
  (let [response ["HTTP/1.1 200 OK\n"
                  "Content-Type: text/html\n"
                  (str "Content-Length: " (count (render-html state)) "\n")
                  "Server: My MacBook Pro\n"
                  (str "Set-Cookie: state=" state "\n\n")
                  (render-html state)]]
    (run! #(.write outputStream (.getBytes %)) response)
    (.flush outputStream)))

(defn- get-key-or-value [post-data kv]
  (read-string
    (case kv
      :key (first post-data)
      :value (second post-data))))

(defn- parse-submission [request kv]
  (let [post-data (-> request
                      (string/split #"\r\n\r\n")
                      (second)
                      (string/split #"="))
        key-value (get-key-or-value post-data kv)]
    (if (number? key-value) key-value (keyword key-value))))

(defn- set-screen [{:keys [current-screen db resume-selection]}]
  (let [game (when db (game-logs/get-last-in-progress-game {:db db :ds sql/ds :dir-path edn/in-progress-dir-path}))]
    (case current-screen
      :db-selection (if (some? game) :resume-selection :mode-selection)
      :resume-selection (if (= 2 resume-selection) :mode-selection :play)
      :mode-selection :board-selection)))

(defn- get-state-from-cookie [request]
  (let [cookie (first (filter #(string/includes? % "Cookie: ") (string/split request #"\r\n")))]
    (read-string (last (string/split cookie #"="))))
  )

(deftype TTT []
  Route
  (serve [this connData outputStream]
    ;(prn (get connData "request"))
    (if (string/includes? (get connData "request") "POST")
      (let [state (get-state-from-cookie (get connData "request"))
            post-key (parse-submission (get connData "request") :key)
            post-value (parse-submission (get connData "request") :value)
            post-state  (assoc state post-key post-value)
            new-state (assoc post-state :current-screen (set-screen post-state))]
        (write-output outputStream new-state))
      (write-output outputStream initial-state)))
  )