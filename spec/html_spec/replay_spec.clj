(ns html-spec.replay-spec
  (:require [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [ttt :refer :all]))

(def replay-state-3x3 {:game-id         2,
                       :current-screen  :replay,
                       :db              :sql,
                       :moves           [[1 2 3 4 :x 6 7 8 9] [:o 2 3 4 :x 6 7 8 9] [:o :x 3 4 :x 6 7 8 9]
                                         [:o :x 3 4 :x 6 7 :o 9] [:o :x :x 4 :x 6 7 :o 9] [:o :x :x 4 :x 6 :o :o 9]
                                         [:o :x :x 4 :x :x :o :o 9] [:o :x :x 4 :x :x :o :o :o]],
                       :second-ai-level 0,
                       :mode            2,
                       :first-ai-level  3,
                       :game-state      :in-progress,
                       :human?          true,
                       :ui              :tui,
                       :player          :x,
                       :board           [1 2 3 4 5 6 7 8 9],
                       :replay?         true})

(def win-state-3x3 {:game-id         2,
                    :current-screen  :replay,
                    :db              :sql,
                    :moves           [[:o :x :x 4 :x :x :o :o :o]],
                    :second-ai-level 0,
                    :mode            2,
                    :first-ai-level  3,
                    :game-state      :in-progress,
                    :human?          true,
                    :ui              :tui,
                    :player          :x,
                    :board           [:o :x :x 4 :x :x :o :o 9],
                    :replay?         true})

(def replay-post-sql (str "POST /ttt HTTP/1.1\r\nContent-Length: 8\r\nCookie: state="
                         (assoc ttt-spec/mode-state :human? false :player :o) "\r\n\r\nreplay=2"))

(def next-move-post-sql (str "POST /ttt HTTP/1.1\r\nContent-Length: 8\r\nCookie: state="
                             replay-state-3x3 "\r\n\r\nnext-move=0"))

(def win-post-sql (str "POST /ttt HTTP/1.1\r\nContent-Length: 8\r\nCookie: state="
                       win-state-3x3 "\r\n\r\nnext-move=0"))

(def game-over-post-sql (str "POST /ttt HTTP/1.1\r\nContent-Length: 8\r\nCookie: state="
                             (assoc win-state-3x3
                               :game-state "O wins!"
                               :moves []
                               :board [:o :x :x 4 :x :x :o :o :o]) "\r\n\r\nnext-move=0"))

(describe "replay games"
  (with-stubs)
  (redefs-around [game-logs/get-new-game-id (stub :game-id {:return 23})
                  edn/create-in-progress-game-file (stub :create-file)
                  edn/log-game-id (stub :log-id)
                  sql/log-game-state (stub :log-game)
                  game-logs/get-game-log (stub :game-log {:return (assoc replay-state-3x3
                                                                    :human? false
                                                                    :player :o
                                                                    :game-state "O wins!") })])

  (before (.reset ttt-spec/out))

  (it "sets current screen to replay if received replay selection"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" replay-post-sql) ttt-spec/out)
    (should-contain ":current-screen :replay" (str ttt-spec/out)))

  (it "sets game state to in-progress"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" replay-post-sql) ttt-spec/out)
    (should-contain ":game-state :in-progress" (str ttt-spec/out)))

  (it "sets replay? to true"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" replay-post-sql) ttt-spec/out)
    (should-contain ":replay? true" (str ttt-spec/out)))

  (it "sets human? to true"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" replay-post-sql) ttt-spec/out)
    (should-contain ":human? true" (str ttt-spec/out)))

  (it "sets player to x"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" replay-post-sql) ttt-spec/out)
    (should-contain ":player :x" (str ttt-spec/out)))

  (it "provides next move button when replay"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" replay-post-sql) ttt-spec/out)
    (should-contain "<button name=\"next-move\" type=\"submit\" value=\"0\">Next Move</button>" (str ttt-spec/out)))

  (it "updates board when next move is clicked"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" next-move-post-sql) ttt-spec/out)
    (should-contain ":board [1 2 3 4 :x 6 7 8 9]" (str ttt-spec/out)))

  (it "updates board when next move is clicked"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" next-move-post-sql) ttt-spec/out)
    (should-contain ":board [1 2 3 4 :x 6 7 8 9]" (str ttt-spec/out)))

  (it "updates moves vector when next move is clicked"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" next-move-post-sql) ttt-spec/out)
    (should-not-contain ":moves [[1 2 3 4 :x 6 7 8 9]" (str ttt-spec/out)))

  (it "toggles player"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" next-move-post-sql) ttt-spec/out)
    (should-contain ":player :o" (str ttt-spec/out)))

  (it "updates state with each move played"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" win-post-sql) ttt-spec/out)
    (should-contain ":game-state \"O wins!\"" (str ttt-spec/out)))


  (it "next move button does nothing if game is over"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" game-over-post-sql) ttt-spec/out)
    (should-contain ":current-screen :replay" (str ttt-spec/out)))
  )