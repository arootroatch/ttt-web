(ns submission-handlers.mode
  (:require [handle-submission :refer :all]
            [helpers :as helpers]))

(defmethod handle-submission :mode [post-key post-value outputStream state]
  (let [post-state (assoc state post-key post-value)
        new-state (assoc post-state :current-screen (helpers/set-screen post-state))]
    (if (> post-value 2)
      (helpers/write-output outputStream (assoc new-state :human? false))
      (helpers/write-output outputStream (assoc new-state :human? true)))))
