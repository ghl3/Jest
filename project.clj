
(defproject jest "0.1.0-SNAPSHOT"
  :description "A staticly typed language on the JVM"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-typed "0.3.5"]
            [lein-antlr "0.2.0"]]

  ;;:hooks [leiningen.antlr]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.typed "0.2.77"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]

                 ;; https://github.com/alexhall/lein-antlr
                 [org.antlr/antlr "3.5.2"]
                 [org.antlr/antlr-runtime "3.5.2"]]
  :main jest.jest
  :aot [jest.jest]

  ;; Typed configs
  :core.typed {:check [jest.jest parser.parse]}

  ;; ANTLR configs
  :antlr-src-dir "grammar"
  :antlr-dest-dir "src/java/grammar"
  :antlr-options {:verbose true :report true})
