
(defproject jest "0.1.0-SNAPSHOT"
  :description "A staticly typed language on the JVM"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-typed "0.3.5"]
            [lein-antlr "0.2.0"]
            [lein-environ "1.0.0"]]

  ;;:hooks [leiningen.antlr]
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.incubator "0.1.3"]
                 [org.clojure/core.typed "0.2.77"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/core.match "0.3.0-alpha4"]

                 [environ "1.0.0"]

                 [com.google.guava/guava "18.0"]
                 [org.apache.commons/commons-lang3 "3.1"]
                 
                 ;; https://github.com/alexhall/lein-antlr
                 [org.antlr/antlr4 "4.5"]
                 [org.antlr/antlr4-runtime "4.5"]
                 ;;[org.antlr/antlr "3.5.2"]
                 ;;[org.antlr/antlr-runtime "3.5.2"]
                 [lein-cljfmt "0.1.4"]]

  :main jest.jest
  :aot [jest.compiler.JestToClojureTranslator jest.jest]

  ;; Typed configs
  :core.typed {:check [jest.test]}

  ;; ANTLR configs
  :antlr-src-dir "grammar"
  :antlr-dest-dir "src/java/grammar"
  :antlr-options {:verbose true :report true}

  :profiles {:dev     {:env {:verbose false}}
             :verbose {:env {:verbose true}}}

  )
