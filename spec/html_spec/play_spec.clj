(ns html-spec.play-spec
  (:require [html.play :refer :all]
            [speclj.core :refer :all]
            [tic-tac-toe.game-logs.game-logs :as game-logs]
            [tic-tac-toe.game-logs.sql :as sql]
            [tic-tac-toe.game_logs.edn-logs :as edn]
            [tic-tac-toe.tui.get-selection :as selection]))

(def state-with-moves {:game-id         23
                       :current-screen  :play
                       :db              :sql
                       :mode            1
                       :board           [1 :o 3 4 :x 6 7 8 9]
                       :first-ai-level  3
                       :second-ai-level nil
                       :player          :x
                       :human?          true
                       :game-state      :in-progress
                       :ui              :web})

(def request-with-moves (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state="
                             state-with-moves "\r\n\r\nmove=6"))
(def request-with-terminal-state (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state="
                                      (assoc state-with-moves :board [:x :x 3 :o :o 6 7 8 9]) "\r\n\r\nmove=2"))
(def request-with-moves-mode-2 (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state="
                                    (assoc state-with-moves :mode 2) "\r\n\r\nmove=6"))
(def request-with-winning-ai (str "POST /ttt HTTP/1.1\r\nContent-Length: 6\r\nCookie: state="
                                    (assoc state-with-moves :mode 2 :board [:o :o 3 4 :x 6 7 8 :x]) "\r\n\r\nmove=6"))

(describe "play screen"
  (with-stubs)
  (redefs-around [game-logs/log-move (stub :log-move)
                  sql/log-game-state (stub :log-state)
                  edn/create-in-progress-game-file (stub :create-file)
                  edn/log-game-id (stub :log-id)
                  game-logs/log-completed-game (stub :log-completed)])
  (before (.reset ttt-spec/out))

  (it "renders 3x3 board"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-1-mode-1) ttt-spec/out)
    (should-contain (str ":board " selection/initial-3x3-board) (str ttt-spec/out))
    (should-contain (str "<div style=\"" button-container-styles "\">"
                         "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"0\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"1\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"2\"></button></div>")
                    (str ttt-spec/out))
    (should-contain (str "<div style=\"" button-container-styles "\">"
                         "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"0\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"4\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"5\"></button></div>")
                    (str ttt-spec/out))
    (should-contain (str "<div style=\"" button-container-styles "\">"
                         "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"0\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"7\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"8\"></button></div>")
                    (str ttt-spec/out)))

  (it "renders 4x4 board"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-2-mode-1-sql) ttt-spec/out)
    (should-contain (str ":board " selection/initial-4x4-board) (str ttt-spec/out))
    (should-contain (str "<div style=\"" button-container-styles "\">"
                         "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"0\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"1\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"2\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"3\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<div style=\"" button-container-styles "\">"
                         "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"4\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"5\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"6\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"7\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<div style=\"" button-container-styles "\">"
                         "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"8\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"9\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"10\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"11\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<div style=\"" button-container-styles "\">"
                         "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"12\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"13\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"14\"></button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"15\"></button>")
                    (str ttt-spec/out)))

  (it "renders Xs and Os"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-moves) ttt-spec/out)
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"1\">O</button>")
                    (str ttt-spec/out))
    (should-contain (str "<button name=\"move\" style=\"" button-styles "\" type=\"submit\" value=\"4\">X</button>")
                    (str ttt-spec/out)))

  (it "post request for move does not add post request to state"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-moves) ttt-spec/out)
    (should-not-contain ":move 6" (str ttt-spec/out)))

  (it "post request for move plays move to board"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-moves) ttt-spec/out)
    (should-contain ":board [1 :o 3 4 :x 6 :x 8 9]" (str ttt-spec/out)))

  (it "post request for move switches player"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-moves) ttt-spec/out)
    (should-contain ":player :o" (str ttt-spec/out)))

  (it "post request for move triggers ai move when not mode 1"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-moves-mode-2) ttt-spec/out)
    (should-contain ":human? true" (str ttt-spec/out)))

  (it "logs completed game when ai move wins"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-winning-ai) ttt-spec/out)
    (should-contain ":board [:o :o :o 4 :x 6 :x 8 :x]" (str ttt-spec/out))
    (should-have-invoked :log-completed))

  (it "post request for move does not set human to false when mode 1"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-moves) ttt-spec/out)
    (should-contain ":human? true" (str ttt-spec/out)))

  (it "post request for move updates state when terminal"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-terminal-state) ttt-spec/out)
    (should-contain ":game-state \"X wins!\"" (str ttt-spec/out)))

  (it "post request for move logs completed game when terminal"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-terminal-state) ttt-spec/out)
    (should-have-invoked :log-completed))

  (it "post request for move disables buttons when game over"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-terminal-state) ttt-spec/out)
    (should-contain (str "<button disabled=\"disabled\" name=\"move\" style=\"" button-styles
                         "\" type=\"submit\" value=\"1\">X</button>")
                    (str ttt-spec/out)))

  (it "post request for move logs move to db"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" request-with-terminal-state) ttt-spec/out)
    (should-have-invoked :log-move [{:with {:ds sql/ds :state (assoc state-with-moves
                                                                :board [:x :x :x :o :o 6 7 8 9]
                                                                :player :o
                                                                :human? true
                                                                :game-state "X wins!")}}]))
  )

