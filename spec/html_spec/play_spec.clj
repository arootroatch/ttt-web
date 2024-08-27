(ns html-spec.play-spec
  (:require [html.play :refer :all]
            [speclj.core :refer :all]
            [tic-tac-toe.tui.get-selection :as selection]))

(describe "play screen"
  (before (.reset ttt-spec/out))
  (it "renders 3x3 board"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/board-post-1-mode-1) ttt-spec/out)
    (prn (str ttt-spec/out))
    (should-contain (str ":board " selection/initial-3x3-board) (str ttt-spec/out))
    (should-contain "<div><button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"0\"></button>"
                    (str ttt-spec/out))
    (should-contain "<button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"1\"></button>"
                    (str ttt-spec/out))
    (should-contain "<button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"2\"></button></div>"
                    (str ttt-spec/out))
    (should-contain "<div><button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"3\"></button>"
                    (str ttt-spec/out))
    (should-contain "<button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"4\"></button>"
                    (str ttt-spec/out))
    (should-contain "<button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"5\"></button></div>"
                    (str ttt-spec/out))
    (should-contain "<div><button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"6\"></button>"
                    (str ttt-spec/out))
    (should-contain "<button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"7\"></button>"
                    (str ttt-spec/out))
    (should-contain "<button name=\"move\" style=\"display: inline-block;height: 100px;width: 100px;\" type=\"submit\" value=\"8\"></button></div>"
                    (str ttt-spec/out))))

