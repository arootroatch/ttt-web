(ns html.play
  (:require [hiccup2.core :as h]
            [render-html :refer :all]
            [tic-tac-toe.gui.play :as play]))

(def button-styles (str "display: inline-block;"
                        "height: 100px;"
                        "width: 100px;"))

(defn- set-label [board n]
  (let [value (nth board n)]
    (when (not (number? value))
      (if (= :o value) "O" "X"))))

(defmulti render-board (fn [state] (count (:board state))))

(defmethod render-board 9 [{:keys [board player game-state human?]}]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (play/play-heading player game-state human?)]
                 [:form {:method "POST" :action "/ttt"}
                  [:div
                   [:button {:type "submit" :name "move" :value 0 :style button-styles} (set-label board 0)]
                   [:button {:type "submit" :name "move" :value 1 :style button-styles} (set-label board 1)]
                   [:button {:type "submit" :name "move" :value 2 :style button-styles} (set-label board 2)]]
                  [:div
                   [:button {:type "submit" :name "move" :value 3 :style button-styles} (set-label board 3)]
                   [:button {:type "submit" :name "move" :value 4 :style button-styles} (set-label board 4)]
                   [:button {:type "submit" :name "move" :value 5 :style button-styles} (set-label board 5)]]
                  [:div
                   [:button {:type "submit" :name "move" :value 6 :style button-styles} (set-label board 6)]
                   [:button {:type "submit" :name "move" :value 7 :style button-styles} (set-label board 7)]
                   [:button {:type "submit" :name "move" :value 8 :style button-styles} (set-label board 8)]]]]])))

(defmethod render-board 16 [{:keys [board player game-state human?]}]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (play/play-heading player game-state human?)]
                 [:form {:method "POST" :action "/ttt"}
                  [:div
                   [:button {:type "submit" :name "move" :value 0 :style button-styles} (set-label board 0)]
                   [:button {:type "submit" :name "move" :value 1 :style button-styles} (set-label board 1)]
                   [:button {:type "submit" :name "move" :value 2 :style button-styles} (set-label board 2)]
                   [:button {:type "submit" :name "move" :value 3 :style button-styles} (set-label board 3)]]
                  [:div
                   [:button {:type "submit" :name "move" :value 4 :style button-styles} (set-label board 4)]
                   [:button {:type "submit" :name "move" :value 5 :style button-styles} (set-label board 5)]
                   [:button {:type "submit" :name "move" :value 6 :style button-styles} (set-label board 6)]
                   [:button {:type "submit" :name "move" :value 7 :style button-styles} (set-label board 7)]]
                  [:div
                   [:button {:type "submit" :name "move" :value 8 :style button-styles} (set-label board 8)]
                   [:button {:type "submit" :name "move" :value 9 :style button-styles} (set-label board 9)]
                   [:button {:type "submit" :name "move" :value 10 :style button-styles} (set-label board 10)]
                   [:button {:type "submit" :name "move" :value 11 :style button-styles} (set-label board 11)]]
                  [:div
                   [:button {:type "submit" :name "move" :value 12 :style button-styles} (set-label board 12)]
                   [:button {:type "submit" :name "move" :value 13 :style button-styles} (set-label board 13)]
                   [:button {:type "submit" :name "move" :value 14 :style button-styles} (set-label board 14)]
                   [:button {:type "submit" :name "move" :value 15 :style button-styles} (set-label board 15)]]]]])))

(defmethod render-html :play [state]
    (render-board state))
