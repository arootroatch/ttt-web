(ns ttt-web.routes-spec
  (:require [ring.mock.request :as mock]
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

  (it "responds 404 for unknown route"
    (should= 404 (:status (sut/routes (mock/request :get "/nope")))))

  (it "serves the stylesheet"
    (should= 200 (:status (sut/routes (mock/request :get "/css/app.css")))))

  (it "persists state in a session cookie"
    (should-contain "ring-session"
                    (first (get-in (sut/app (mock/request :get "/")) [:headers "Set-Cookie"])))))
