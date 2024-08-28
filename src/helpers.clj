(ns helpers
  (:require [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [render-html :refer :all]))

(def edn-game-id-path "src/game_logs/game-ids.edn")
(def edn-in-progress-dir "src/game_logs/in_progress")
(def edn-logs-path "src/game_logs/game-logs.edn")

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

(defn set-screen [state]
  (let [{:keys [current-screen db resume-selection mode second-ai-level]} state]
    (case current-screen
      :db-selection (resume-or-mode-screen db)
      :resume-selection (if (= 2 resume-selection) :mode-selection :play)
      :mode-selection :board-selection
      :board-selection (if (= 1 mode) (log-and-play state) :level-selection)
      :level-selection (if (and (nil? second-ai-level) (= 4 mode)) :level-selection (log-and-play state))
      current-screen)))

(defn write-output [outputStream state]
  (let [response ["HTTP/1.1 200 OK\n"
                  "Content-Type: text/html\n"
                  (str "Content-Length: " (count (render-html state)) "\n")
                  "Server: My MacBook Pro\n"
                  (str "Set-Cookie: state=" state "\n\n")
                  (render-html state)]]
    (run! #(.write outputStream (.getBytes %)) response)
    (.flush outputStream)))
