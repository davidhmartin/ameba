(defproject ameba "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.datomic/datomic-free "0.8.3538"]
                 [commons-codec "1.6"]
                ]
  :dev-dependencies [[swank-clojure "1.4.0"]]
  :jvm-opts ["-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"]
  :main ameba.core)

