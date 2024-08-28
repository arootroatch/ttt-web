(ns ttt
  (:require [clojure.string :as string]
            [html.board-selection]
            [html.db-selection]
            [html.level-selection]
            [html.mode-selection]
            [html.play]
            [html.resume-selection]
            [render-html :refer :all]
            [tic-tac-toe.eval-board :as eval]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [tic-tac-toe.gui.play :as play]
            [tic-tac-toe.gui.resume-selection :as resume]
            [tic-tac-toe.gui.utils :as utils]
            [tic-tac-toe.tui.get-selection :as selection])
  (:import MyServer.Route))

(def edn-in-progress-dir "src/game_logs/in_progress")
(def edn-logs-path "src/game_logs/game-logs.edn")
(def edn-game-id-path "src/game_logs/game-ids.edn")

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

(defn- get-resumed-game-board [db old-game]
  (when old-game
    (case db
      :edn (edn/get-resumed-game-board old-game)
      :sql (read-string (:games/board old-game)))))

(defn- resume-or-mode-screen [db]
  (let [game-log (when db
                   (game-logs/get-last-in-progress-game {:db db :ds sql/ds :dir-path edn-in-progress-dir}))
        board (when game-log (get-resumed-game-board db game-log))]
    (if (and (some? game-log) (< (count board) 17))
      :resume-selection
      :mode-selection)))

(defmulti log-and-play (fn [state] (:db state)))

(defmethod log-and-play :sql [state]
  (sql/log-game-state sql/ds (assoc state :current-screen :play))
  :play)

(defmethod log-and-play :edn [state]
  (edn/create-in-progress-game-file (:filepath state) (assoc state :current-screen :play))
  (edn/log-game-id edn-game-id-path (:game-id state))
  :play)

(defn- set-screen [state]
  (let [{:keys [current-screen db resume-selection mode second-ai-level]} state]
    (case current-screen
      :db-selection (resume-or-mode-screen db)
      :resume-selection (if (= 2 resume-selection) :mode-selection :play)
      :mode-selection :board-selection
      :board-selection (if (= 1 mode) (log-and-play state) :level-selection)
      :level-selection (if (and (nil? second-ai-level) (= 4 mode)) :level-selection (log-and-play state))
      current-screen)))

(defn- get-state-from-cookie [request]
  (let [cookie (first (filter #(string/includes? % "Cookie: ") (string/split request #"\r\n")))]
    (read-string (last (string/split cookie #"=")))))

(defn- set-resume-state [state old-game]
  (if (= :sql (:db state)) (assoc state :game-log old-game) (assoc state :filepath old-game)))

(defn- handle-resume [post-value outputStream state]
  (let [old-game (game-logs/get-last-in-progress-game {:db (:db state) :ds sql/ds :dir-path edn-in-progress-dir})
        old-state (resume/handle-resume {:n post-value :state (set-resume-state state old-game)})
        new-state (assoc old-state :ui :web)]
    (if (false? (:human? new-state))
      (write-output outputStream (play/ai-turn new-state))
      (write-output outputStream new-state))))

(defn- ai-move [outputStream state]
  (let [new-state (play/ai-turn state)]
    (when (not= :in-progress (:game-state new-state))
      (game-logs/log-completed-game {:ds sql/ds :log-file edn-logs-path :state new-state}))
    (write-output outputStream new-state)))

(defn- handle-move [post-value outputStream state]
  (if (= :in-progress (:game-state state))
    (let [{:keys [board player]} state
          new-board (assoc board post-value player)
          new-state (assoc state :board new-board
                                 :player (utils/switch-player player)
                                 :human? (if (not= 1 (:mode state)) false true)
                                 :game-state (eval/score new-board))]
      (game-logs/log-move {:ds sql/ds :state new-state})
      (if (not= :in-progress (:game-state new-state))
        (do (game-logs/log-completed-game {:ds sql/ds :log-file edn-logs-path :state new-state})
            (write-output outputStream new-state))
        (if (not= 1 (:mode state))
          (ai-move outputStream new-state)
          (write-output outputStream new-state))))
    (write-output outputStream state)))

(defn handle-db [post-value outputStream state]
  (let [game-id (game-logs/get-new-game-id {:db (:db state) :path edn-game-id-path :ds sql/ds})
        filepath (edn/create-new-filepath edn-in-progress-dir game-id)]
    (if (= :edn post-value)
      (write-output outputStream (assoc state :game-id game-id :filepath filepath))
      (write-output outputStream (assoc state :game-id game-id)))))

(defn- ai-game [state]
  (if (not= :in-progress (:game-state state))
    (do (game-logs/log-completed-game {:ds sql/ds :log-file edn-logs-path :state state}) state)
    (recur (play/ai-turn state))))

(defn handle-replay [post-value outputStream state]
  (let [game-log (game-logs/get-game-log {:db (:db state) :filepath edn-logs-path :ds sql/ds :id post-value})
        new-state (assoc game-log :current-screen :replay :game-state :in-progress :replay? true :human? true :player :x)]
    (write-output outputStream new-state)))

(defn handle-next-move [outputStream state]
  (if (= :in-progress (:game-state state))
    (write-output outputStream (assoc state :board (first (:moves state)) :moves (rest (:moves state))
                                            :game-state (eval/score (first (:moves state)))
                                            :player (utils/switch-player (:player state))))
    (write-output outputStream state)))

(deftype TTT []
  Route
  (serve [this connData outputStream]
    (if (string/includes? (get connData "request") "POST")
      (let [state (get-state-from-cookie (get connData "request"))
            post-key (parse-submission (get connData "request") :key)
            post-value (parse-value (get connData "request") post-key)
            post-state (if (not= :move post-key) (assoc state post-key post-value) state)
            new-state (assoc post-state :current-screen (set-screen post-state))]
        (cond (= :db post-key) (handle-db post-value outputStream new-state)
              (= :resume-selection post-key) (handle-resume post-value outputStream new-state)
              (and (= :mode post-key) (< post-value 3)) (write-output outputStream (assoc new-state :human? true))
              (and (= :mode post-key) (> post-value 2)) (write-output outputStream (assoc new-state :human? false))
              (= :replay post-key) (handle-replay post-value outputStream state)
              (and (= :first-ai-level post-key) (= 3 (:mode state))) (write-output outputStream (play/ai-turn new-state))
              (= :second-ai-level post-key) (write-output outputStream (ai-game new-state))
              (= :move post-key) (handle-move post-value outputStream state)
              (= :next-move post-key) (handle-next-move outputStream state)
              :else (write-output outputStream new-state)))
      (write-output outputStream initial-state))))
