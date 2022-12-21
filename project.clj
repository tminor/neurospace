(defproject neurospace "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [net.bytebuddy/byte-buddy "LATEST"]
                 [net.bytebuddy/byte-buddy-agent "LATEST"]
                 [org.ow2.asm/asm-tree "9.3"]
                 [org.ow2.asm/asm-analysis "9.3"]
                 [org.ow2.asm/asm-commons "9.3"]
                 [com.google.guava/guava "31.1-jre"]
                 [insn "0.5.4"]
                 [com.clojure-goes-fast/clj-async-profiler "1.0.0"]
                 [criterium "0.4.6"]
                 [ubergraph "0.8.2"]]
  ;; :plugins [[lein-virgil "0.1.9"]]
  :global-vars {*warn-on-reflection* true}
  :main ^:skip-aot neurospace.core
  :java-source-paths ["src/neurospace" "test/neurospace"])
