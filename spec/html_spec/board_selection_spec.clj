(ns html-spec.board-selection-spec
  (:require [speclj.core :refer :all]
            [ttt-spec :as ttt-spec]))

(describe "board-selection"
  (it "asks for board selection after receiving mode selection"
    (.serve ttt-spec/route (assoc ttt-spec/connData "request" ttt-spec/mode-post) ttt-spec/out)
    (should-contain (str "Set-Cookie: state=" (assoc ttt-spec/mode-state :current-screen :board-selection :mode 2))
                    (str ttt-spec/out))
    (should-contain "<p>Please select board:</p>" (str ttt-spec/out))
    (should-contain "<form action=\"/ttt\" method=\"POST\">" (str ttt-spec/out))
    (should-contain "<select name=\"board\"><option value=\"1\">3x3</option>" (str ttt-spec/out))
    (should-contain "<option value=\"2\">4x4</option>" (str ttt-spec/out))
    (should-contain "<input type=\"submit\" /></form></div>" (str ttt-spec/out))
    (should-contain #"<html>.*</html>" (str ttt-spec/out))
    (should-contain "<div style=\"margin:0 auto;width: 400px;text-align: center;\">" (str ttt-spec/out))
    (should-contain "<h1>Tic-Tac-Toe</h1>" (str ttt-spec/out))))

