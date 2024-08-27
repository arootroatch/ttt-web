(ns ttt
  (:require [clojure.string :as string]
            [html.board-selection]
            [html.db-selection]
            [html.level-selection]
            [html.mode-selection]
            [html.play]
            [html.resume-selection]
            [render-html :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [tic-tac-toe.gui.resume-selection :as resume]
            [tic-tac-toe.tui.get-selection :as selection])
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

(defn- parse-value [request key]
  (let [value (parse-submission request :value)]
    (if (= :board key)
      (case value
        1 selection/initial-3x3-board
        2 selection/initial-4x4-board)
      value)))

(defn- resume-or-mode [db]
  (if (some? (when db (game-logs/get-last-in-progress-game {:db db :ds sql/ds :dir-path edn/in-progress-dir-path})))
    :resume-selection
    :mode-selection))

(defn- set-screen [{:keys [current-screen db resume-selection mode second-ai-level]}]
  (case current-screen
    :db-selection (resume-or-mode db)
    :resume-selection (if (= 2 resume-selection) :mode-selection :play)
    :mode-selection :board-selection
    :board-selection (if (= 1 mode) :play :level-selection)
    :level-selection (if (and (nil? second-ai-level) (= 4 mode)) :level-selection :play)
    current-screen))

(defn- get-state-from-cookie [request]
  (let [cookie (first (filter #(string/includes? % "Cookie: ") (string/split request #"\r\n")))]
    (read-string (last (string/split cookie #"=")))))

(defn- get-resumed-game-board [db old-game]
  (when old-game
    (case db
      :edn (edn/get-resumed-game-board old-game)
      :sql (read-string (:games/board old-game)))))

(defmulti handle-resume (fn [_ _ state] (:db state)))

(defmethod handle-resume :sql [post-value outputStream state]
  (let [old-game (game-logs/get-last-in-progress-game {:db :sql :ds sql/ds})
        old-board (get-resumed-game-board (:db state) old-game)
        new-state (when (some? old-game) (resume/handle-resume {:n post-value :state (assoc state :game-log old-game)}))]
    (if (and (some? new-state) (< (count old-board) 17))
      (write-output outputStream new-state)
      (write-output outputStream (assoc state :current-screen :mode-selection)))))

(defmethod handle-resume :edn [post-value outputStream state]
  (let [old-game (game-logs/get-last-in-progress-game {:db :edn :dir-path edn/in-progress-dir-path})
        old-board (get-resumed-game-board (:db state) old-game)
        new-state (resume/handle-resume {:n post-value :state (assoc state :filepath old-game)})]
    (if (< (count old-board) 17)
      (write-output outputStream new-state)
      (write-output outputStream (assoc state :current-screen :mode-selection)))))

(defn- set-game-id [state]
  (if (and (:db state) (nil? (:game-id state)))
    (game-logs/get-new-game-id {:db (:db state) :ds sql/ds :path edn/game-id-path})
    (:game-id state)))

(deftype TTT []
  Route
  (serve [this connData outputStream]
    (if (string/includes? (get connData "request") "POST")
      (let [state (get-state-from-cookie (get connData "request"))
            game-id (set-game-id state)
            post-key (parse-submission (get connData "request") :key)
            post-value (parse-value (get connData "request") post-key)
            post-state (assoc state post-key post-value)
            new-state (assoc post-state :current-screen (set-screen post-state) :game-id game-id)]
        (cond (= :resume-selection post-key) (handle-resume post-value outputStream new-state)
              (and (= :mode post-key) (< post-value 3)) (write-output outputStream (assoc new-state :human? true))
              (and (= :mode post-key) (> post-value 2)) (write-output outputStream (assoc new-state :human? false))
              :else (write-output outputStream new-state)))
      (write-output outputStream initial-state)))
  )
