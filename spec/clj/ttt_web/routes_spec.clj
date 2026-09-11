(ns ttt-web.routes-spec
  (:require [clojure.string :as str]
            [ring.middleware.anti-forgery :as af :refer [wrap-anti-forgery]]
            [ring.mock.request :as mock]
            [speclj.core :refer [describe it should-contain should-have-invoked should= stub with-stubs]]
            [ttt-web.handlers :as handlers]
            [ttt-web.routes :as sut]))

(defmacro it-routes [method route handler]
  `(it (str ~method " " ~route " -> " '~handler)
     (with-redefs [~handler (stub :handler)]
       (sut/routes (mock/request ~method ~route))
       (should-have-invoked :handler))))

(describe "routes"
  (with-stubs)

  (it-routes :get "/" handlers/home)
  (it-routes :post "/move/0" handlers/move)
  (it-routes :post "/restart" handlers/restart)
  (it-routes :post "/ai-move" handlers/ai-move)
  (it-routes :get "/mode" handlers/mode-screen)
  (it-routes :get "/board" handlers/board-screen)
  (it-routes :get "/level" handlers/level-screen)
  (it-routes :get "/play" handlers/play-screen)
  (it-routes :post "/mode/2" handlers/select-mode)
  (it-routes :post "/board/1" handlers/select-board)
  (it-routes :post "/level/3" handlers/select-level)

  (it "responds 404 for unknown route"
    (should= 404 (:status (sut/routes (mock/request :get "/nope")))))

  (it "serves the stylesheet"
    (should= 200 (:status (sut/routes (mock/request :get "/css/app.css")))))

  (it "persists state in a session cookie"
    (should-contain "ring-session"
                    (first (get-in (sut/app (mock/request :get "/mode")) [:headers "Set-Cookie"]))))

  (it "rejects a state-changing request without a csrf token"
    (should= 403 (:status (sut/app (mock/request :post "/restart")))))

  (it "accepts a request whose token matches the session"
    (let [af-routes (wrap-anti-forgery sut/routes)]
      (should= 200 (:status (af-routes (-> (mock/request :post "/restart")
                                           (assoc :session {::af/anti-forgery-token "t"
                                                            :board [1 2 3 4 5 6 7 8 9]})
                                           (mock/header "X-CSRF-Token" "t")))))))

  (it "keeps the session valid all the way through the selection flow"
    (let [page   (sut/app (mock/request :get "/mode"))
          cookie (first (str/split (first (get-in page [:headers "Set-Cookie"])) #";"))
          token  (second (re-find #"X-CSRF-Token&quot;:[ ]*&quot;([A-Za-z0-9+/=]+)&quot;" (:body page)))
          send   (fn [uri] (sut/app (-> (mock/request :post uri)
                                        (mock/header "Cookie" cookie)
                                        (mock/header "X-CSRF-Token" token))))]
      (should= 200 (:status (send "/mode/2")))
      (should= 200 (:status (send "/board/1")))
      (should= 200 (:status (send "/level/3")))
      (should= 200 (:status (send "/move/0")))
      (should= 200 (:status (send "/ai-move")))))

  (it "accepts a state-changing request carrying the page's token"
    (let [page (sut/app (mock/request :get "/mode"))
          cookie (first (str/split (first (get-in page [:headers "Set-Cookie"])) #";"))
          token (second (re-find #"X-CSRF-Token&quot;:[ ]*&quot;([A-Za-z0-9+/=]+)&quot;" (:body page)))]
      (should= 200 (:status (sut/app (-> (mock/request :post "/restart")
                                         (mock/header "Cookie" cookie)
                                         (mock/header "X-CSRF-Token" token))))))))
