(ns submission-handlers.first-ai-level
  (:require [tic-tac-toe.gui.play :as play]
            [handle-submission :refer :all]
            [helpers :as helpers]))

(defmethod handle-submission :first-ai-level [post-key post-value outputStream state]
  (let [post-state (assoc state post-key post-value)
        new-state (assoc post-state :current-screen (helpers/set-screen post-state))]
    (if (= 3 (:mode new-state))
      (helpers/write-output outputStream (play/ai-turn new-state))
      (helpers/write-output outputStream new-state))))
