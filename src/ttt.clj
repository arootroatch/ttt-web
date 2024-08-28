(ns ttt
  (:require [clojure.string :as string]
            [html.board-selection]
            [html.db-selection]
            [html.level-selection]
            [html.mode-selection]
            [html.play]
            [html.resume-selection]
            [render-html :refer :all]
            [tic-tac-toe.tui.get-selection :as selection]
            [handle-submission :refer :all]
            [helpers :as helpers]
            [submission-handlers.db]
            [submission-handlers.resume-selection]
            [submission-handlers.mode]
            [submission-handlers.replay]
            [submission-handlers.first-ai-level]
            [submission-handlers.move]
            [submission-handlers.next-move]
            [submission-handlers.default]
            [submission-handlers.second-ai-level])
  (:import MyServer.Route))

(def initial-state {:game-id         nil
                    :current-screen  :db-selection
                    :mode            nil
                    :board           nil
                    :first-ai-level  nil
                    :second-ai-level nil
                    :player          :x
                    :human?          nil
                    :game-state      :in-progress
                    :ui              :web})

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

(defn- parse-value [request key]
  (let [value (parse-submission request :value)]
    (if (= :board key)
      (case value
        1 selection/initial-3x3-board
        2 selection/initial-4x4-board)
      value)))

(defn- get-state-from-cookie [request]
  (let [cookie (first (filter #(string/includes? % "Cookie: ") (string/split request #"\r\n")))]
    (read-string (last (string/split cookie #"=")))))

(deftype TTT []
  Route
  (serve [this connData outputStream]
    (if (string/includes? (get connData "request") "POST")
      (let [state (get-state-from-cookie (get connData "request"))
            post-key (parse-submission (get connData "request") :key)
            post-value (parse-value (get connData "request") post-key)]
        (handle-submission post-key post-value outputStream state))
      (helpers/write-output outputStream initial-state))))
