
(defproject jest "0.1.0-SNAPSHOT"
  :description "A staticly typed language on the JVM"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-typed "0.3.5"]]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.typed "0.2.77"]
                 [org.clojure/tools.cli "0.3.1"]]
   :main jest.jest
   :core.typed {:check [jest.jest parser.parse]})



