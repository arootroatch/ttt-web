(ns html.play
  (:require [hiccup2.core :as h]
            [render-html :refer :all]
            [tic-tac-toe.gui.play :as play]))

(def button-styles (str "display: inline-block;"
                        "height: 100px;"
                        "width: 100px;"
                        "font-size: 100px;"
                        "line-height: 100px;"
                        "box-sizing: border-box;"))

(def button-container-styles (str "height: 100px;"
                                  "display: flex;"
                                  "width: fit-content;"
                                  "margin: 0 auto"))

(defn- set-label [board n]
  (let [value (nth board n)]
    (when (not (number? value))
      (if (= :o value) "O" "X"))))

(defn render-button [n board]
  [:button {:type "submit" :name "move" :value n :style button-styles} (set-label board n)])

(defmulti render-board (fn [state] (count (:board state))))

(defmethod render-board 9 [{:keys [board player game-state human? replay?]}]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (play/play-heading player game-state human?)]
                 [:form {:method "POST" :action "/ttt"}
                  [:div {:style button-container-styles} (map #(render-button % board) [0 1 2])]
                  [:div {:style button-container-styles} (map #(render-button % board) [3 4 5])]
                  [:div {:style button-container-styles} (map #(render-button % board) [6 7 8])]
                  (when replay? [:button {:type "submit" :name "next-move" :value 0} "Next Move"])]]])))

(defmethod render-board 16 [{:keys [board player game-state human? replay?]}]
  (str (h/html [:html
                [:div {:style "margin:0 auto;width: 400px;text-align: center;"}
                 [:h1 "Tic-Tac-Toe"]
                 [:p (play/play-heading player game-state human?)]
                 [:form {:method "POST" :action "/ttt"}
                  [:div {:style button-container-styles} (map #(render-button % board) [0 1 2 3])]
                  [:div {:style button-container-styles} (map #(render-button % board) [4 5 6 7])]
                  [:div {:style button-container-styles} (map #(render-button % board) [8 9 10 11])]
                  [:div {:style button-container-styles} (map #(render-button % board) [12 13 14 15])]
                  (when replay? [:button {:type "submit" :name "next-move"} "Next Move"])]]])))

(defmethod render-html :play [state]
    (render-board state))

(defmethod render-html :replay [state]
  (render-board (assoc state :replay? true)))
