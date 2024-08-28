(ns handle-submission)

(defmulti handle-submission (fn [post-key _ _ _] post-key))